package io.github.xpakx.alingo.game;

import io.github.xpakx.alingo.game.dto.AnswerRequest;
import io.github.xpakx.alingo.security.JwtUtils;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpStatus.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GameControllerTest {
    @LocalServerPort
    private int port;
    private String baseUrl;
    private Long userId;

    private final static int FULL_STACK = 64;

    @Autowired
    JwtUtils jwt;
    @Autowired
    ExerciseRepository exerciseRepository;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost".concat(":").concat(port + "");
        userId = 1L;
    }

    @AfterEach
    void tearDown() {
        exerciseRepository.deleteAll();
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
        Exercise exercise = new Exercise();
        exercise.setCorrectAnswer(correct);
        exercise.setWrongAnswer(wrong);
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
}