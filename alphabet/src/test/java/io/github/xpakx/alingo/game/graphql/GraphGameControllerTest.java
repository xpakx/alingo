package io.github.xpakx.alingo.game.graphql;

import io.github.xpakx.alingo.clients.GuessPublisher;
import io.github.xpakx.alingo.game.Course;
import io.github.xpakx.alingo.game.CourseRepository;
import io.github.xpakx.alingo.game.Exercise;
import io.github.xpakx.alingo.game.ExerciseRepository;
import io.github.xpakx.alingo.game.dto.AnswerRequest;
import io.github.xpakx.alingo.security.JwtUtils;
import io.github.xpakx.alingo.utils.GraphAnswer;
import io.github.xpakx.alingo.utils.GraphExercises;
import io.github.xpakx.alingo.utils.GraphQuery;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.springframework.http.HttpStatus.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GraphGameControllerTest {
    @LocalServerPort
    private int port;
    private String baseUrl;

    @Autowired
    JwtUtils jwt;
    @Autowired
    ExerciseRepository exerciseRepository;
    @Autowired
    CourseRepository courseRepository;
    @MockBean
    GuessPublisher publisher;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost".concat(":").concat(port + "");
    }

    @AfterEach
    void tearDown() {
        exerciseRepository.deleteAll();
        courseRepository.deleteAll();
    }

    private AnswerRequest getAnswerRequest(String answer) {
        return new AnswerRequest(answer);
    }

    private String tokenFor(String username) {
        return tokenFor(username, new ArrayList<>());
    }

    private String tokenFor(String username, List<GrantedAuthority> authorities) {
        return jwt.generateToken(new User(username, "", authorities));
    }

    private Long addExercise(String correct, String wrong) {
        return addExercise(correct, wrong, null);
    }

    private Long addExercise(String correct, String wrong, Long courseId) {
        Exercise exercise = new Exercise();
        exercise.setCorrectAnswer(correct);
        exercise.setWrongAnswer(wrong);
        if(courseId != null) {
            exercise.setCourse(courseRepository.getReferenceById(courseId));
        }
        return exerciseRepository.save(exercise).getId();
    }

    private void addExercises(Long courseId, int amount) {
        for(int i=0; i<amount; i++) {
            addExercise("correct", "wrong", courseId);
        }
    }

    private Long createCourse(String courseName) {
        return createCourse(courseName, false);
    }

    private Long createCourse(String courseName, boolean premium) {
        Course course = new Course();
        course.setName(courseName);
        course.setPremium(premium);
        return courseRepository.save(course).getId();
    }

    @Test
    public void shouldAcceptGuessWithGraphQL() {
        Long exerciseId = addExercise("correct", "wrong");
        GraphQuery query = getGraphQueryForAnswer(getVariablesForAnswer(exerciseId, "correct"));
        given()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(query)
                .log().all()
        .when()
                .post(baseUrl + "/graphql")
        .then()
                .log().all()
                .statusCode(OK.value())
                .body("data.answer.correct", equalTo(true));
    }

    private GraphQuery getGraphQueryForAnswer(GraphAnswer answer) {
        GraphQuery query = new GraphQuery();
        query.setQuery("""
                    mutation answer($id: Int, $guess: String){
                        answer(exercise: $id, guess: $guess)
                        {
                            correct
                            correctAnswer
                        }
                    }""");
        query.setVariables(answer);
        return query;
    }

    private GraphAnswer getVariablesForAnswer(Long exerciseId, String guess) {
        GraphAnswer answer = new GraphAnswer();
        answer.setId(exerciseId);
        answer.setGuess(guess);
        return answer;
    }

    @Test
    void shouldRespondWith401ToCheckAnswerIfNotAuthenticated() {
        GraphQuery query = getGraphQueryForAnswer(getVariablesForAnswer(1L, "correct"));
        given()
                .contentType(ContentType.JSON)
                .body(query)
        .when()
                .post(baseUrl + "/graphql")
        .then()
                .statusCode(OK.value())
                .body("data", nullValue())
                .body("errors", not(nullValue()))
                .body("errors.message", hasItem(containsStringIgnoringCase("access denied")));
    }

    @Test
    void shouldRespondWith401ToCheckAnswerIfTokenIsWrong() {
        GraphQuery query = getGraphQueryForAnswer(getVariablesForAnswer(1L, "correct"));
        given()
                .auth()
                .oauth2("204230990324")
                .contentType(ContentType.JSON)
                .body(query)
        .when()
                .post(baseUrl + "/graphql")
        .then()
                .statusCode(OK.value())
                .body("data", nullValue())
                .body("errors", not(nullValue()))
                .body("errors.message", hasItem(containsStringIgnoringCase("access denied")));
    }

    @Test
    void shouldRespondWith404ToCheckAnswerIfExerciseDoesNotExist() {
        GraphQuery query = getGraphQueryForAnswer(getVariablesForAnswer(1L, "correct"));
        given()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(query)
        .when()
                .post(baseUrl + "/graphql")
        .then()
                .statusCode(OK.value())
                .body("data", nullValue())
                .body("errors", not(nullValue()))
                .body("errors.message", hasItem(containsStringIgnoringCase("not found")));
    }

    @Test
    void shouldRespondToWrongGuess() {
        Long exerciseId = addExercise("correct", "wrong");
        GraphQuery query = getGraphQueryForAnswer(getVariablesForAnswer(exerciseId, "wrong"));
        given()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(query)
        .when()
                .post(baseUrl + "/graphql")
        .then()
                .statusCode(OK.value())
                .body("data.answer.correct", equalTo(false))
                .body("data.answer.correctAnswer", equalTo("correct"));
    }

    @Test
    void shouldNotAcceptEmptyGuess() {
        GraphQuery query = getGraphQueryForAnswer(getVariablesForAnswer(1L, ""));
        given()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(query)
        .when()
                .post(baseUrl + "/graphql")
        .then()
                .statusCode(OK.value())
                .body("data", nullValue())
                .body("errors", not(nullValue()))
                .body("errors.message", hasItem(both(containsStringIgnoringCase("guess")).and(containsStringIgnoringCase("empty"))));
    }

    @Test
    void shouldNotAcceptNullGuess() {
        GraphQuery query = getGraphQueryForAnswer(getVariablesForAnswer(1L, null));
        given()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(query)
        .when()
                .post(baseUrl + "/graphql")
        .then()
                .statusCode(OK.value())
                .body("data", nullValue())
                .body("errors", not(nullValue()))
                .body("errors.message", hasItem(both(containsStringIgnoringCase("guess")).and(containsStringIgnoringCase("empty"))));
    }

    @Test
    void shouldNotAcceptNullExerciseId() {
        GraphQuery query = getGraphQueryForAnswer(getVariablesForAnswer(null, "correct"));
        given()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(query)
        .when()
                .post(baseUrl + "/graphql")
        .then()
                .statusCode(OK.value())
                .body("data", nullValue())
                .body("errors", not(nullValue()))

                .body("errors.message", hasItem(both(containsStringIgnoringCase("id")).and(containsStringIgnoringCase("provided"))));
    }

    private GraphQuery getGraphQueryForExercises(GraphExercises answer) {
        GraphQuery query = new GraphQuery();
        query.setQuery("""
                    query courseExercises($id: Int, $page: Int, $amount: Int){
                        courseExercises(course: $id, page: $page, amount: $amount)
                        {
                            page
                            size
                            totalSize
                            exercises {
                                id
                                options
                            }
                        }
                    }""");
        query.setVariables(answer);
        return query;
    }

    private GraphExercises getVariablesForExercises(Long courseId, Integer page, Integer amount) {
        GraphExercises variables = new GraphExercises();
        variables.setId(courseId);
        variables.setPage(page);
        variables.setAmount(amount);
        return variables;
    }

    @Test
    void shouldRespondWith401ToCGetExercisesIfNotAuthenticated() {
        GraphQuery query = getGraphQueryForExercises(getVariablesForExercises(1L, 1, 10));
        given()
                .contentType(ContentType.JSON)
                .body(query)
        .when()
                .post(baseUrl + "/graphql")
        .then()
                .statusCode(OK.value())
                .body("data", nullValue())
                .body("errors", not(nullValue()))
                .body("errors.message", hasItem(containsStringIgnoringCase("access denied")));
    }

    @Test
    void shouldRespondWith401ToGetExercisesIfTokenIsWrong() {
        GraphQuery query = getGraphQueryForExercises(getVariablesForExercises(1L, 1, 10));
        given()
                .auth()
                .oauth2("3040234")
                .contentType(ContentType.JSON)
                .body(query)
        .when()
                .post(baseUrl + "/graphql")
        .then()
                .statusCode(OK.value())
                .body("data", nullValue())
                .body("errors", not(nullValue()))
                .body("errors.message", hasItem(containsStringIgnoringCase("access denied")));
    }

    @Test
    void shouldRespondWithEmptyListToGetExercisesIfCourseDoesNotExist() {
        GraphQuery query = getGraphQueryForExercises(getVariablesForExercises(1L, 1, 10));
        given()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(query)
        .when()
                .post(baseUrl + "/graphql")
        .then()
                .statusCode(OK.value())
                .body("data.courseExercises.exercises", hasSize(0));
    }

    @ParameterizedTest
    @ValueSource(ints = {-10, -1, 0})
    void shouldNotAcceptRequestWithNonPositivePages(int page) {
        GraphQuery query = getGraphQueryForExercises(getVariablesForExercises(1L, page, 10));
        given()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(query)
        .when()
                .post(baseUrl + "/graphql")
        .then()
                .statusCode(OK.value())
                .body("data", nullValue())
                .body("errors", not((nullValue())))
                .body("errors.message", hasItem(both(containsStringIgnoringCase("page")).and(containsStringIgnoringCase("positive"))));
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0, 21, 50})
    void shouldNotAcceptRequestWithAmountOutsideBounds(int amount) {
        GraphQuery query = getGraphQueryForExercises(getVariablesForExercises(1L, 1, amount));
        given()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(query)
        .when()
                .post(baseUrl + "/graphql")
        .then()
                .statusCode(OK.value())
                .body("data", nullValue())
                .body("errors", not((nullValue())))
                .body("errors.message", hasItem(both(containsStringIgnoringCase("amount")).and(containsStringIgnoringCase("between"))));
    }

    @Test
    void shouldRespondWithListOfExercises() {
        Long courseId = createCourse("course");
        addExercises(courseId, 5);
        GraphQuery query = getGraphQueryForExercises(getVariablesForExercises(courseId, 1, 3));
        given()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(query)
        .when()
                .post(baseUrl + "/graphql")
        .then()
                .log().body()
                .statusCode(OK.value())
                .body("data.courseExercises.exercises", hasSize(3))
                .body("data.courseExercises.size", equalTo(3))
                .body("data.courseExercises.totalSize", equalTo(5));
    }

    @Test
    void shouldRespondWith403ToGetExercisesFromPremiumCourseIfNotSubscribed() {
        Long courseId = createCourse("course", true);
        GraphQuery query = getGraphQueryForExercises(getVariablesForExercises(courseId, 1, 10));
        given()
                .auth()
                .oauth2(tokenFor("non-subscriber"))
                .contentType(ContentType.JSON)
                .body(query)
        .when()
                .post(baseUrl + "/graphql")
        .then()
                .statusCode(OK.value())
                .body("data", nullValue())
                .body("errors", not((nullValue())))
                .body("errors.message", hasItem(equalTo("Access Denied")));
    }

    @Test
    void shouldRespondWithExercisesFromPremiumCourseIfSubscribed() {
        Long courseId = createCourse("course", true);
        GraphQuery query = getGraphQueryForExercises(getVariablesForExercises(courseId, 1, 10));
        given()
                .auth()
                .oauth2(tokenFor("user1", List.of(new SimpleGrantedAuthority("SUBSCRIBER"))))
                .contentType(ContentType.JSON)
                .body(query)
        .when()
                .post(baseUrl + "/graphql")
        .then()
                .statusCode(OK.value())
                .body("data", not(nullValue()))
                .body("errors", (nullValue()));
    }
}