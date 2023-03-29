package io.github.xpakx.alingo.game.graphql;

import io.github.xpakx.alingo.game.Course;
import io.github.xpakx.alingo.game.CourseRepository;
import io.github.xpakx.alingo.game.Exercise;
import io.github.xpakx.alingo.game.ExerciseRepository;
import io.github.xpakx.alingo.security.JwtUtils;
import io.github.xpakx.alingo.utils.*;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GraphExerciseControllerTest {
    @LocalServerPort
    private int port;
    private String baseUrl;

    @Autowired
    JwtUtils jwt;
    @Autowired
    CourseRepository courseRepository;
    @Autowired
    ExerciseRepository exerciseRepository;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost".concat(":").concat(port + "");
    }

    @AfterEach
    void tearDown() {
        exerciseRepository.deleteAll();
        courseRepository.deleteAll();
    }

    private GraphQuery getNewExerciseGraphQuery(GraphExercise answer) {
        GraphQuery query = new GraphQuery();
        query.setQuery("""
                    mutation addExercise($letter: String, $wrongAnswer: String, $correctAnswer: String, $courseId: Int, $sound: String){
                        addExercise(letter: $letter, wrongAnswer: $wrongAnswer, correctAnswer: $correctAnswer, courseId: $courseId, sound: $sound)
                        {
                            id
                            letter
                            wrongAnswer
                            correctAnswer
                            order
                        }
                    }""");
        query.setVariables(answer);
        return query;
    }

    private GraphExercise getNewExerciseVariables(String letter, String wrong, String correct, Long courseId) {
        return new GraphExercise(letter, wrong, correct, courseId, null);
    }

    private GraphQuery getUpdateExerciseGraphQuery(GraphUpdateExercise answer) {
        GraphQuery query = new GraphQuery();
        query.setQuery("""
                    mutation editExercise($id: ID, $letter: String, $wrongAnswer: String, $correctAnswer: String, $courseId: Int, $sound: String){
                        editExercise(exerciseId: $id, letter: $letter, wrongAnswer: $wrongAnswer, correctAnswer: $correctAnswer, courseId: $courseId, sound: $sound)
                        {
                            id
                            letter
                            wrongAnswer
                            correctAnswer
                            order
                        }
                    }""");
        query.setVariables(answer);
        return query;
    }

    private GraphUpdateExercise getUpdateExerciseVariables(Long exerciseId, String name, String wrong, String correct, Long courseId) {
        return new GraphUpdateExercise(exerciseId, name, wrong, correct, courseId, null);
    }

    @Test
    void shouldRespondWith401ToAddExerciseIfNotAuthenticated() {
        GraphQuery query = getNewExerciseGraphQuery(getNewExerciseVariables("a", "wrong", "correct", 1L));
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
    void shouldRespondWith401ToAddExerciseIfTokenIsWrong() {
        GraphQuery query = getNewExerciseGraphQuery(getNewExerciseVariables("a", "wrong", "correct", 1L));
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
    void shouldRespondWith403ToAddExerciseIfUserIsNotModerator() {
        GraphQuery query = getNewExerciseGraphQuery(getNewExerciseVariables("a", "wrong", "correct", 1L));
        given()
                .auth()
                .oauth2(tokenFor())
                .contentType(ContentType.JSON)
                .body(query)
        .when()
                .post(baseUrl + "/graphql")
        .then()
                .statusCode(OK.value())
                .body("data", nullValue())
                .body("errors", not(nullValue()))
                .body("errors.message", hasItem(equalTo("Access Denied")));
    }

    private String tokenFor() {
        return tokenFor(new ArrayList<>());
    }

    private String tokenFor(List<GrantedAuthority> authorities) {
        return jwt.generateToken(new User("user1", "", authorities));
    }

    @Test
    void shouldAddExercise() {
        GraphQuery query = getNewExerciseGraphQuery(getNewExerciseVariables("a", "wrong", "correct", addCourse()));
        given()
                .auth()
                .oauth2(tokenFor(List.of(new SimpleGrantedAuthority("MODERATOR"))))
                .contentType(ContentType.JSON)
                .body(query)
        .when()
                .post(baseUrl + "/graphql")
        .then()
                .statusCode(OK.value())
                .body("data.addExercise.letter", equalTo("a"));
    }

    private Long addCourse() {
        Course course = new Course();
        course.setName("course");
        return courseRepository.save(course).getId();
    }


    @Test
    void shouldAddNewCourseToDb() {
        GraphQuery query = getNewExerciseGraphQuery(getNewExerciseVariables("a", "wrong", "correct", addCourse()));
        given()
                .auth()
                .oauth2(tokenFor(List.of(new SimpleGrantedAuthority("MODERATOR"))))
                .contentType(ContentType.JSON)
                .body(query)
        .when()
                .post(baseUrl + "/graphql");
        List<Exercise> exercises = exerciseRepository.findAll();
        assertThat(exercises, hasItem(hasProperty("letter", equalTo("a"))));
    }

    @Test
    void shouldNotAcceptEmptyWrongOption() {
        GraphQuery query = getNewExerciseGraphQuery(getNewExerciseVariables("a", "", "correct", addCourse()));
        given()
                .auth()
                .oauth2(tokenFor(List.of(new SimpleGrantedAuthority("MODERATOR"))))
                .contentType(ContentType.JSON)
                .body(query)
        .when()
                .post(baseUrl + "/graphql")
        .then()
                .statusCode(OK.value())
                .body("data", nullValue())
                .body("errors", not(nullValue()))
                .body("errors.message", hasItem(both(containsStringIgnoringCase("wrong")).and(containsStringIgnoringCase("provided"))));

    }

    @Test
    void shouldNotAcceptNullWrongOption() {
        GraphQuery query = getNewExerciseGraphQuery(getNewExerciseVariables("a", null, "correct", addCourse()));
        given()
                .auth()
                .oauth2(tokenFor(List.of(new SimpleGrantedAuthority("MODERATOR"))))
                .contentType(ContentType.JSON)
                .body(query)
        .when()
                .post(baseUrl + "/graphql")
        .then()
                .statusCode(OK.value())
                .body("data", nullValue())
                .body("errors", not(nullValue()))
                .body("errors.message", hasItem(both(containsStringIgnoringCase("wrong")).and(containsStringIgnoringCase("provided"))));
    }

    @Test
    void shouldNotAcceptEmptyCorrectOption() {
        GraphQuery query = getNewExerciseGraphQuery(getNewExerciseVariables("a", "wrong", "", addCourse()));
        given()
                .auth()
                .oauth2(tokenFor(List.of(new SimpleGrantedAuthority("MODERATOR"))))
                .contentType(ContentType.JSON)
                .body(query)
        .when()
                .post(baseUrl + "/graphql")
        .then()
                .statusCode(OK.value())
                .body("data", nullValue())
                .body("errors", not(nullValue()))
                .body("errors.message", hasItem(both(containsStringIgnoringCase("correct")).and(containsStringIgnoringCase("provided"))));
    }

    @Test
    void shouldNotAcceptNullCorrectOption() {
        GraphQuery query = getNewExerciseGraphQuery(getNewExerciseVariables("a", "wrong", null, addCourse()));
        given()
                .auth()
                .oauth2(tokenFor(List.of(new SimpleGrantedAuthority("MODERATOR"))))
                .contentType(ContentType.JSON)
                .body(query)
        .when()
                .post(baseUrl + "/graphql")
        .then()
                .statusCode(OK.value())
                .body("data", nullValue())
                .body("errors", not(nullValue()))
                .body("errors.message", hasItem(both(containsStringIgnoringCase("correct")).and(containsStringIgnoringCase("provided"))));
    }

    @Test
    void shouldNotAcceptNullCourseId() {
        GraphQuery query = getNewExerciseGraphQuery(getNewExerciseVariables("a", "wrong", "correct", null));
        given()
                .auth()
                .oauth2(tokenFor(List.of(new SimpleGrantedAuthority("MODERATOR"))))
                .contentType(ContentType.JSON)
                .body(query)
        .when()
                .post(baseUrl + "/graphql")
        .then()
                .statusCode(OK.value())
                .body("data", nullValue())
                .body("errors", not(nullValue()))
                .body("errors.message", hasItem(both(containsStringIgnoringCase("belong")).and(containsStringIgnoringCase("course"))));
    }

    @Test
    void shouldRespondWith401ToUpdateExerciseIfNotAuthenticated() {
        GraphQuery query = getUpdateExerciseGraphQuery(getUpdateExerciseVariables(1L, "a", "wrong", "correct", 1L));
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
    void shouldRespondWith401ToUpdateExerciseIfTokenIsWrong() {
        GraphQuery query = getUpdateExerciseGraphQuery(getUpdateExerciseVariables(1L, "a", "wrong", "correct", 1L));
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
    void shouldRespondWith403ToUpdateExerciseIfUserIsNotModerator() {
        GraphQuery query = getUpdateExerciseGraphQuery(getUpdateExerciseVariables(1L, "a", "wrong", "correct", 1L));
        given()
                .auth()
                .oauth2(tokenFor())
                .contentType(ContentType.JSON)
                .body(query)
        .when()
                .post(baseUrl + "/graphql")
        .then()
                .statusCode(OK.value())
                .body("data", nullValue())
                .body("errors", not(nullValue()))
                .body("errors.message", hasItem(equalTo("Access Denied")));
    }

    @Test
    void shouldRespondWith404ToUpdateExerciseIfExerciseDoesNotExist() {
        GraphQuery query = getUpdateExerciseGraphQuery(getUpdateExerciseVariables(1L, "a", "wrong", "correct", 1L));
        given()
                .auth()
                .oauth2(tokenFor(List.of(new SimpleGrantedAuthority("MODERATOR"))))
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
    void shouldUpdateExercise() {
        Long courseId = addCourse();
        Long exerciseId = addExercise("a", courseId);
        GraphQuery query = getUpdateExerciseGraphQuery(getUpdateExerciseVariables(exerciseId, "g", "wrong", "correct", courseId));
        given()
                .auth()
                .oauth2(tokenFor(List.of(new SimpleGrantedAuthority("MODERATOR"))))
                .contentType(ContentType.JSON)
                .body(query)
        .when()
                .post(baseUrl + "/graphql")
        .then()
                .log().body()
                .statusCode(OK.value())
                .body("data.editExercise.letter", equalTo("g"));
    }

    private Long addExercise(String letter, Long courseId) {
        Exercise exercise = new Exercise();
        exercise.setLetter(letter);
        exercise.setWrongAnswer("wrong");
        exercise.setCorrectAnswer("correct");
        exercise.setCourse(courseId != null ? courseRepository.getReferenceById(courseId) : null);
        exercise.setOrder(0);
        return exerciseRepository.save(exercise).getId();
    }

    @Test
    void shouldUpdateExerciseIndDb() {
        Long courseId = addCourse();
        Long exerciseId = addExercise("a", courseId);
        GraphQuery query = getUpdateExerciseGraphQuery(getUpdateExerciseVariables(exerciseId, "g", "wrong", "correct", courseId));
        given()
                .auth()
                .oauth2(tokenFor(List.of(new SimpleGrantedAuthority("MODERATOR"))))
                .contentType(ContentType.JSON)
                .body(query)
        .when()
                .post(baseUrl + "/graphql");
        Optional<Exercise> exercise = exerciseRepository.findById(exerciseId);
        assertTrue(exercise.isPresent());
        assertThat(exercise.get(), hasProperty("letter", equalTo("g")));
    }

    @Test
    void shouldNotAcceptEmptyWrongAnswerWhileUpdating() {
        Long courseId = addCourse();
        Long exerciseId = addExercise("a", courseId);
        GraphQuery query = getUpdateExerciseGraphQuery(getUpdateExerciseVariables(exerciseId, "g", "", "correct", courseId));
        given()
                .auth()
                .oauth2(tokenFor(List.of(new SimpleGrantedAuthority("MODERATOR"))))
                .contentType(ContentType.JSON)
                .body(query)
        .when()
                .post(baseUrl + "/graphql")
        .then()
                .statusCode(OK.value())
                .body("data", nullValue())
                .body("errors", not(nullValue()))
                .body("errors.message", hasItem(both(containsStringIgnoringCase("wrong")).and(containsStringIgnoringCase("provided"))));
    }

    @Test
    void shouldNotAcceptNullWrongAnswerWhileUpdating() {
        Long courseId = addCourse();
        Long exerciseId = addExercise("a", courseId);
        GraphQuery query = getUpdateExerciseGraphQuery(getUpdateExerciseVariables(exerciseId, "g", null, "correct", courseId));
        given()
                .auth()
                .oauth2(tokenFor(List.of(new SimpleGrantedAuthority("MODERATOR"))))
                .contentType(ContentType.JSON)
                .body(query)
        .when()
                .post(baseUrl + "/graphql")
        .then()
                .statusCode(OK.value())
                .body("data", nullValue())
                .body("errors", not(nullValue()))
                .body("errors.message", hasItem(both(containsStringIgnoringCase("wrong")).and(containsStringIgnoringCase("provided"))));
    }

    @Test
    void shouldNotAcceptEmptyCorrectAnswerWhileUpdating() {
        Long courseId = addCourse();
        Long exerciseId = addExercise("a", courseId);
        GraphQuery query = getUpdateExerciseGraphQuery(getUpdateExerciseVariables(exerciseId, "g", "wrong", "", courseId));
        given()
                .auth()
                .oauth2(tokenFor(List.of(new SimpleGrantedAuthority("MODERATOR"))))
                .contentType(ContentType.JSON)
                .body(query)
        .when()
                .post(baseUrl + "/graphql")
        .then()
                .statusCode(OK.value())
                .body("data", nullValue())
                .body("errors", not(nullValue()))
                .body("errors.message", hasItem(both(containsStringIgnoringCase("correct")).and(containsStringIgnoringCase("provided"))));
    }

    @Test
    void shouldNotAcceptNullCorrectAnswerWhileUpdating() {
        Long courseId = addCourse();
        Long exerciseId = addExercise("a", courseId);
        GraphQuery query = getUpdateExerciseGraphQuery(getUpdateExerciseVariables(exerciseId, "g", "wrong", null, courseId));
        given()
                .auth()
                .oauth2(tokenFor(List.of(new SimpleGrantedAuthority("MODERATOR"))))
                .contentType(ContentType.JSON)
                .body(query)
        .when()
                .post(baseUrl + "/graphql")
        .then()
                .statusCode(OK.value())
                .body("data", nullValue())
                .body("errors", not(nullValue()))
                .body("errors.message", hasItem(both(containsStringIgnoringCase("correct")).and(containsStringIgnoringCase("provided"))));
    }

    @Test
    void shouldNotAcceptNullCourseIdWhileUpdating() {
        Long courseId = addCourse();
        Long exerciseId = addExercise("a", courseId);
        GraphQuery query = getUpdateExerciseGraphQuery(getUpdateExerciseVariables(exerciseId, "g", "wrong", "correct", null));
        given()
                .auth()
                .oauth2(tokenFor(List.of(new SimpleGrantedAuthority("MODERATOR"))))
                .contentType(ContentType.JSON)
                .body(query)
        .when()
                .post(baseUrl + "/graphql")
        .then()
                .statusCode(OK.value())
                .body("data", nullValue())
                .body("errors", not(nullValue()))
                .body("errors.message", hasItem(both(containsStringIgnoringCase("belong")).and(containsStringIgnoringCase("course"))));
    }

    @Test
    void shouldRespondWith401ToReorderExerciseIfNotAuthenticated() {
        GraphQuery query = getOrderGraphQuery(getOrderVariables(0, 1L));
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

    private GraphQuery getOrderGraphQuery(GraphOrder variables) {
        GraphQuery query = new GraphQuery();
        query.setQuery("""
                    mutation reorderExercise($id: ID, $order: Int){
                        reorderExercise(exerciseId: $id, order: $order)
                        {
                            id
                            letter
                            wrongAnswer
                            correctAnswer
                            order
                        }
                    }""");
        query.setVariables(variables);
        return query;
    }

    private GraphOrder getOrderVariables(Integer order, Long exerciseId) {
        return new GraphOrder(order, exerciseId);
    }

    @Test
    void shouldRespondWith401ToReorderExerciseIfTokenIsWrong() {
        GraphQuery query = getOrderGraphQuery(getOrderVariables(0, 1L));
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
    void shouldRespondWith403ToReorderExerciseIfUserIsNotModerator() {
        GraphQuery query = getOrderGraphQuery(getOrderVariables(0, 1L));
        given()
                .auth()
                .oauth2(tokenFor())
                .contentType(ContentType.JSON)
                .body(query)
        .when()
                .post(baseUrl + "/graphql")
        .then()
                .statusCode(OK.value())
                .body("data", nullValue())
                .body("errors", not(nullValue()))
                .body("errors.message", hasItem(equalTo("Access Denied")));
    }

    @Test
    void shouldRespondWith404ToReorderExerciseIfExerciseDoesNotExist() {
        GraphQuery query = getOrderGraphQuery(getOrderVariables(0, 1L));
        given()
                .auth()
                .oauth2(tokenFor(List.of(new SimpleGrantedAuthority("MODERATOR"))))
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
    void shouldNotAcceptNullOrderWhileReordering() {
        Long exerciseId = addExercise("g", addCourse());
        GraphQuery query = getOrderGraphQuery(getOrderVariables(null, exerciseId));
        given()
                .auth()
                .oauth2(tokenFor(List.of(new SimpleGrantedAuthority("MODERATOR"))))
                .contentType(ContentType.JSON)
                .body(query)
        .when()
                .post(baseUrl + "/graphql")
        .then()
                .statusCode(OK.value())
                .body("data", nullValue())
                .body("errors", not(nullValue()))
                .body("errors.message", hasItem(both(containsStringIgnoringCase("order")).and(containsStringIgnoringCase("provided"))));
    }

    @Test
    void shouldNotAcceptNegativeOrderWhileReordering() {
        Long exerciseId = addExercise("g", addCourse());
        GraphQuery query = getOrderGraphQuery(getOrderVariables(-1, exerciseId));
        given()
                .auth()
                .oauth2(tokenFor(List.of(new SimpleGrantedAuthority("MODERATOR"))))
                .contentType(ContentType.JSON)
                .body(query)
         .when()
                .post(baseUrl + "/graphql")
         .then()
                .statusCode(OK.value())
                .body("data", nullValue())
                .body("errors", not(nullValue()))
                .body("errors.message", hasItem(both(containsStringIgnoringCase("order")).and(containsStringIgnoringCase("negative"))));
    }

    @Test
    void shouldReorder() {
        Long exerciseId = addOrderedExercise(addCourse(), 0);
        GraphQuery query = getOrderGraphQuery(getOrderVariables(0, exerciseId));
        given()
                .auth()
                .oauth2(tokenFor(List.of(new SimpleGrantedAuthority("MODERATOR"))))
                .contentType(ContentType.JSON)
                .body(query)
        .when()
                .post(baseUrl + "/graphql")
        .then()
                .statusCode(OK.value())
                .body("errors", nullValue())
                .body("data", not(nullValue()));
    }

    private Long addOrderedExercise(Long courseId, Integer order) {
        Exercise exercise = new Exercise();
        exercise.setLetter("a");
        exercise.setWrongAnswer("wrong");
        exercise.setCorrectAnswer("correct");
        exercise.setCourse(courseId != null ? courseRepository.getReferenceById(courseId) : null);
        exercise.setOrder(order);
        return exerciseRepository.save(exercise).getId();
    }

    @Test
    void shouldReorderUpwardWithoutGaps() {
        Long courseId = addCourse();
        Long ex1Id = addOrderedExercise(courseId, 0);
        Long ex2Id = addOrderedExercise(courseId, 1);
        Long ex3Id = addOrderedExercise(courseId, 2);
        Long ex4Id = addOrderedExercise(courseId, 3);
        Long ex5Id = addOrderedExercise(courseId, 4);
        Long ex6Id = addOrderedExercise(courseId, 5);
        Long ex7Id = addOrderedExercise(courseId, 6);
        GraphQuery query = getOrderGraphQuery(getOrderVariables(1, ex5Id));
        given()
                .auth()
                .oauth2(tokenFor(List.of(new SimpleGrantedAuthority("MODERATOR"))))
                .contentType(ContentType.JSON)
                .body(query)
        .when()
                .post(baseUrl + "/graphql")
        .then()
                .statusCode(OK.value())
                .body("errors", nullValue())
                .body("data", not(nullValue()));
        List<Exercise> exercises = exerciseRepository.findAll();
        assertThat(exercises, hasItem(both(hasProperty("id", equalTo(ex1Id))).and(hasProperty("order", equalTo(0)))));
        assertThat(exercises, hasItem(both(hasProperty("id", equalTo(ex5Id))).and(hasProperty("order", equalTo(1)))));
        assertThat(exercises, hasItem(both(hasProperty("id", equalTo(ex2Id))).and(hasProperty("order", equalTo(2)))));
        assertThat(exercises, hasItem(both(hasProperty("id", equalTo(ex3Id))).and(hasProperty("order", equalTo(3)))));
        assertThat(exercises, hasItem(both(hasProperty("id", equalTo(ex4Id))).and(hasProperty("order", equalTo(4)))));
        assertThat(exercises, hasItem(both(hasProperty("id", equalTo(ex6Id))).and(hasProperty("order", equalTo(5)))));
        assertThat(exercises, hasItem(both(hasProperty("id", equalTo(ex7Id))).and(hasProperty("order", equalTo(6)))));
    }

    @Test
    void shouldReorderDownwardWithoutGaps() {
        Long courseId = addCourse();
        Long ex1Id = addOrderedExercise(courseId, 0);
        Long ex2Id = addOrderedExercise(courseId, 1);
        Long ex3Id = addOrderedExercise(courseId, 2);
        Long ex4Id = addOrderedExercise(courseId, 3);
        Long ex5Id = addOrderedExercise(courseId, 4);
        Long ex6Id = addOrderedExercise(courseId, 5);
        Long ex7Id = addOrderedExercise(courseId, 6);
        GraphQuery query = getOrderGraphQuery(getOrderVariables(5, ex3Id));
        given()
                .auth()
                .oauth2(tokenFor(List.of(new SimpleGrantedAuthority("MODERATOR"))))
                .contentType(ContentType.JSON)
                .body(query)
        .when()
                .post(baseUrl + "/graphql")
        .then()
                .statusCode(OK.value())
                .body("errors", nullValue())
                .body("data", not(nullValue()));
        List<Exercise> exercises = exerciseRepository.findAll();
        assertThat(exercises, hasItem(both(hasProperty("id", equalTo(ex1Id))).and(hasProperty("order", equalTo(0)))));
        assertThat(exercises, hasItem(both(hasProperty("id", equalTo(ex2Id))).and(hasProperty("order", equalTo(1)))));
        assertThat(exercises, hasItem(both(hasProperty("id", equalTo(ex4Id))).and(hasProperty("order", equalTo(2)))));
        assertThat(exercises, hasItem(both(hasProperty("id", equalTo(ex5Id))).and(hasProperty("order", equalTo(3)))));
        assertThat(exercises, hasItem(both(hasProperty("id", equalTo(ex6Id))).and(hasProperty("order", equalTo(4)))));
        assertThat(exercises, hasItem(both(hasProperty("id", equalTo(ex3Id))).and(hasProperty("order", equalTo(5)))));
        assertThat(exercises, hasItem(both(hasProperty("id", equalTo(ex7Id))).and(hasProperty("order", equalTo(6)))));
    }

    @Test
    void shouldMoveAtTheFirstPlaceWithoutGaps() {
        Long courseId = addCourse();
        Long ex1Id = addOrderedExercise(courseId, 0);
        Long ex2Id = addOrderedExercise(courseId, 1);
        Long ex3Id = addOrderedExercise(courseId, 2);
        Long ex4Id = addOrderedExercise(courseId, 3);
        GraphQuery query = getOrderGraphQuery(getOrderVariables(0, ex3Id));
        given()
                .auth()
                .oauth2(tokenFor(List.of(new SimpleGrantedAuthority("MODERATOR"))))
                .contentType(ContentType.JSON)
                .body(query)
        .when()
                .post(baseUrl + "/graphql")
        .then()
                .statusCode(OK.value())
                .body("errors", nullValue())
                .body("data", not(nullValue()));
        List<Exercise> exercises = exerciseRepository.findAll();
        assertThat(exercises, hasItem(both(hasProperty("id", equalTo(ex3Id))).and(hasProperty("order", equalTo(0)))));
        assertThat(exercises, hasItem(both(hasProperty("id", equalTo(ex1Id))).and(hasProperty("order", equalTo(1)))));
        assertThat(exercises, hasItem(both(hasProperty("id", equalTo(ex2Id))).and(hasProperty("order", equalTo(2)))));
        assertThat(exercises, hasItem(both(hasProperty("id", equalTo(ex4Id))).and(hasProperty("order", equalTo(3)))));
    }

    @Test
    void shouldMoveAtTheLastPlaceWithoutGaps() {
        Long courseId = addCourse();
        Long ex1Id = addOrderedExercise(courseId, 0);
        Long ex2Id = addOrderedExercise(courseId, 1);
        Long ex3Id = addOrderedExercise(courseId, 2);
        Long ex4Id = addOrderedExercise(courseId, 3);
        GraphQuery query = getOrderGraphQuery(getOrderVariables(3, ex2Id));
        given()
                .auth()
                .oauth2(tokenFor(List.of(new SimpleGrantedAuthority("MODERATOR"))))
                .contentType(ContentType.JSON)
                .body(query)
        .when()
                .post(baseUrl + "/graphql")
        .then()
                .statusCode(OK.value())
                .body("errors", nullValue())
                .body("data", not(nullValue()));
        List<Exercise> exercises = exerciseRepository.findAll();
        assertThat(exercises, hasItem(both(hasProperty("id", equalTo(ex1Id))).and(hasProperty("order", equalTo(0)))));
        assertThat(exercises, hasItem(both(hasProperty("id", equalTo(ex3Id))).and(hasProperty("order", equalTo(1)))));
        assertThat(exercises, hasItem(both(hasProperty("id", equalTo(ex4Id))).and(hasProperty("order", equalTo(2)))));
        assertThat(exercises, hasItem(both(hasProperty("id", equalTo(ex2Id))).and(hasProperty("order", equalTo(3)))));
    }

    @Test
    void shouldNotAcceptOrderGreaterThanTheGreatestForTheCourse() {
        Long courseId = addCourse();
        addOrderedExercise(courseId, 0);
        Long exerciseId = addOrderedExercise(courseId, 1);
        addOrderedExercise(courseId, 2);
        GraphQuery query = getOrderGraphQuery(getOrderVariables(5, exerciseId));
        given()
                .auth()
                .oauth2(tokenFor(List.of(new SimpleGrantedAuthority("MODERATOR"))))
                .contentType(ContentType.JSON)
                .body(query)
        .when()
                .post(baseUrl + "/graphql")
        .then()
                .statusCode(OK.value())
                .body("data", nullValue())
                .body("errors", not(nullValue()))
                .body("errors.message", hasItem(both(containsStringIgnoringCase("order")).and(containsStringIgnoringCase("high"))));
    }

    private GraphQuery getGetExerciseGraphQuery(GetByIdVariables answer) {
        GraphQuery query = new GraphQuery();
        query.setQuery("""
                    query getExercise($id: ID){
                        getExercise(id: $id)
                        {
                            id
                            letter
                        }
                    }""");
        query.setVariables(answer);
        return query;
    }

    private GetByIdVariables getIdVariables(Long id) {
        return new GetByIdVariables(id);
    }

    @Test
    void shouldRespondWith401ToGetExerciseIfNotAuthenticated() {
        GraphQuery query = getGetExerciseGraphQuery(getIdVariables(1L));
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
    void shouldRespondWith401ToGetExerciseIfTokenIsWrong() {
        GraphQuery query = getGetExerciseGraphQuery(getIdVariables(1L));
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
    void shouldRespondWith403ToGetExerciseIfUserIsNotModerator() {
        GraphQuery query = getGetExerciseGraphQuery(getIdVariables(1L));
        given()
                .auth()
                .oauth2(tokenFor())
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
    void shouldRespondWith404ToGetExerciseIfLanguageDoesNotExist() {
        GraphQuery query = getGetExerciseGraphQuery(getIdVariables(1L));
        given()
                .auth()
                .oauth2(tokenFor(List.of(new SimpleGrantedAuthority("MODERATOR"))))
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
    void shouldReturnExercise() {
        GraphQuery query = getGetExerciseGraphQuery(getIdVariables(addExercise("a", addCourse())));
        given()
                .auth()
                .oauth2(tokenFor(List.of(new SimpleGrantedAuthority("MODERATOR"))))
                .contentType(ContentType.JSON)
                .body(query)
        .when()
                .post(baseUrl + "/graphql")
        .then()
                .statusCode(OK.value())
                .body("data.getExercise.letter", equalTo("a"));
    }
}