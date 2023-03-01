package io.github.xpakx.alingo.stats;

import io.github.xpakx.alingo.security.JwtUtils;
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
import static org.hamcrest.Matchers.*;
import static org.springframework.http.HttpStatus.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GuessControllerTest {
    @LocalServerPort
    private int port;
    private String baseUrl;

    @Autowired
    JwtUtils jwt;
    @Autowired
    GuessRepository guessRepository;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost".concat(":").concat(port + "");
    }

    @AfterEach
    void tearDown() {
        guessRepository.deleteAll();
    }

    private String tokenFor(String username) {
        return tokenFor(username, new ArrayList<>());
    }

    private String tokenFor(String username, List<GrantedAuthority> authorities) {
        return jwt.generateToken(new User(username, "", authorities));
    }

    @Test
    void shouldRespondWith401ToGetGuessesIfNotAuthenticated() {
        given()
                .param("page", 1)
                .param("amount", 10)
        .when()
                .get(baseUrl + "/stats/{username}/alphabet", "user1")
        .then()
                .statusCode(UNAUTHORIZED.value())
                .body("error", equalTo(UNAUTHORIZED.value()))
                .body("errors", nullValue());
    }

    @Test
    void shouldRespondWith401ToGetGuessesIfTokenIsWrong() {
        given()
                .auth()
                .oauth2("21090cjw")
                .param("page", 1)
                .param("amount", 10)
        .when()
                .get(baseUrl + "/stats/{username}/alphabet", "user1")
        .then()
                .statusCode(UNAUTHORIZED.value())
                .body("error", equalTo(UNAUTHORIZED.value()))
                .body("errors", nullValue());
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
                .get(baseUrl + "/stats/{username}/alphabet", "user1")
        .then()
                .statusCode(BAD_REQUEST.value())
                .body("error", equalTo(BAD_REQUEST.value()))
                .body("message", both(containsStringIgnoringCase("page")).and(containsStringIgnoringCase("positive")));
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
                .get(baseUrl + "/stats/{username}/alphabet", "user1")
        .then()
                .statusCode(BAD_REQUEST.value())
                .body("error", equalTo(BAD_REQUEST.value()))
                .body("message", both(containsStringIgnoringCase("amount")).and(containsStringIgnoringCase("between")));
    }

    @Test
    void shouldRespondWithListOfGuessesForUser() {
        addGuesses("user1", 5);
        given()
                .auth()
                .oauth2(tokenFor("user1"))
                .param("page", 1)
                .param("amount", 3)
        .when()
                .get(baseUrl + "/stats/{username}/alphabet", "user1")
        .then()
                .statusCode(OK.value())
                .body("content", hasSize(3));
    }

    private void addGuesses(String username, int amount) {
        for(int i=0; i<amount; i++) {
            addGuess(username);
        }
    }

    private void addGuess(String username) {
        Guess guess = new Guess();
        guess.setUsername(username);
        guessRepository.save(guess);
    }

    @Test
    void shouldRespondWith403ToGetOtherUsersGuesses() {
        addGuesses("user2", 5);
        given()
                .auth()
                .oauth2(tokenFor("user1"))
                .param("page", 1)
                .param("amount", 10)
        .when()
                .get(baseUrl + "/stats/{username}/alphabet", "user2")
        .then()
                .statusCode(FORBIDDEN.value());
    }
}