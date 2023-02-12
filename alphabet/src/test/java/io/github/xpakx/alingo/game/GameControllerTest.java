package io.github.xpakx.alingo.game;

import io.github.xpakx.alingo.game.dto.AnswerRequest;
import io.github.xpakx.alingo.security.JwtUtils;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpStatus.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GameControllerTest {
    @LocalServerPort
    private int port;
    private String baseUrl;

    @Autowired
    JwtUtils jwt;
    @Autowired
    ExerciseRepository exerciseRepository;
    @Autowired
    CourseRepository courseRepository;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost".concat(":").concat(port + "");
    }

    @AfterEach
    void tearDown() {
        exerciseRepository.deleteAll();
        courseRepository.deleteAll();
    }

    @Test
    void shouldRespondWith401ToCheckAnswerIfNotAuthenticated() {
        when()
                .post(baseUrl + "/exercise/{exerciseId}", 1L)
        .then()
                .statusCode(UNAUTHORIZED.value());
    }

    @Test
    void shouldRespondWith401ToCheckAnswerIfTokenIsWrong() {
        given()
                .auth()
                .oauth2("21090cjw")
                .contentType(ContentType.JSON)
                .body(getAnswerRequest("answer"))
        .when()
                .post(baseUrl + "/exercise/{exerciseId}", 1L)
        .then()
                .statusCode(UNAUTHORIZED.value());
    }

    @Test
    void shouldRespondWith404ToCheckAnswerIfExerciseDoesNotExist() {
        given()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(getAnswerRequest("answer"))
        .when()
                .post(baseUrl + "/exercise/{exerciseId}", 1L)
        .then()
                .statusCode(NOT_FOUND.value());
    }

    private AnswerRequest getAnswerRequest(String answer) {
        AnswerRequest request = new AnswerRequest();
        request.setAnswer(answer);
        return request;
    }

    private String tokenFor(String username) {
        return tokenFor(username, new ArrayList<>());
    }

    private String tokenFor(String username, List<GrantedAuthority> authorities) {
        return jwt.generateToken(new User(username, "", authorities));
    }

    @Test
    void shouldRespondToWrongGuess() {
        Long exerciseId = addExercise("correct", "wrong");
        given()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(getAnswerRequest("wrong"))
        .when()
                .post(baseUrl + "/exercise/{exerciseId}", exerciseId)
        .then()
                .statusCode(OK.value())
                .body("correct", is(false))
                .body("correctAnswer", is(equalTo("correct")));
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

    @Test
    void shouldRespondToCorrectGuess() {
        Long exerciseId = addExercise("correct", "wrong");
        given()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(getAnswerRequest("correct"))
        .when()
                .post(baseUrl + "/exercise/{exerciseId}", exerciseId)
        .then()
                .statusCode(OK.value())
                .body("correct", is(true))
                .body("correctAnswer", is(equalTo("correct")));
    }

    @Test
    void shouldNotAcceptEmptyGuess() {
        given()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(getAnswerRequest(""))
        .when()
                .post(baseUrl + "/exercise/{exerciseId}", 1L)
        .then()
                .statusCode(BAD_REQUEST.value());
    }

    @Test
    void shouldNotAcceptNullGuess() {
        given()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(getAnswerRequest(null))
        .when()
                .post(baseUrl + "/exercise/{exerciseId}", 1L)
        .then()
                .statusCode(BAD_REQUEST.value());
    }

    @Test
    void shouldRespondWith401ToCGetExercisesIfNotAuthenticated() {
        given()
                .param("page", 1)
                .param("amount", 10)
        .when()
                .get(baseUrl + "/course/{courseId}/exercise", 1L)
        .then()
                .statusCode(UNAUTHORIZED.value());
    }

    @Test
    void shouldRespondWith401ToGetExercisesIfTokenIsWrong() {
        given()
                .auth()
                .oauth2("21090cjw")
                .param("page", 1)
                .param("amount", 10)
        .when()
                .get(baseUrl + "/course/{courseId}/exercise", 1L)
        .then()
                .statusCode(UNAUTHORIZED.value());
    }

    @Test
    void shouldRespondWithEmptyListToGetExercisesIfCourseDoesNotExist() {
        given()
                .auth()
                .oauth2(tokenFor("user1"))
                .param("page", 1)
                .param("amount", 10)
        .when()
                .get(baseUrl + "/course/{courseId}/exercise", 1L)
        .then()
                .statusCode(OK.value())
                .body("exercises", hasSize(0))
                .body("size", is(equalTo(0)));
    }

    @ParameterizedTest
    @ValueSource(ints = {-10, -1, 0})
    void shouldNotAcceptRequestWithNonPositivePages(int page) {
        given()
                .auth()
                .oauth2(tokenFor("user1"))
                .param("page", page)
                .param("amount", 10)
        .when()
                .get(baseUrl + "/course/{courseId}/exercise", 1L)
        .then()
                .statusCode(BAD_REQUEST.value());
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0, 21, 50})
    void shouldNotAcceptRequestWithAmountOutsideBounds(int amount) {
        given()
                .auth()
                .oauth2(tokenFor("user1"))
                .param("page", 1)
                .param("amount", amount)
        .when()
                .get(baseUrl + "/course/{courseId}/exercise", 1L)
        .then()
                .statusCode(BAD_REQUEST.value());
    }

    @Test
    void shouldRespondWithListOfExercises() {
        Long courseId = createCourse("course");
        addExercises(courseId, 5);
        given()
                .auth()
                .oauth2(tokenFor("user1"))
                .param("page", 1)
                .param("amount", 3)
        .when()
                .get(baseUrl + "/course/{courseId}/exercise", courseId)
        .then()
                .statusCode(OK.value())
                .body("exercises", hasSize(3))
                .body("size", is(equalTo(3)))
                .body("totalSize", is(equalTo(5)));
    }

    private void addExercises(Long courseId, int amount) {
        for(int i=0; i<amount; i++) {
            addExercise("correct", "wrong", courseId);
        }
    }

    private Long createCourse(String courseName) {
        Course course = new Course();
        course.setName(courseName);
        return courseRepository.save(course).getId();
    }
}