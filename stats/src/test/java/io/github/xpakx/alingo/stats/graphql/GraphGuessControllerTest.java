package io.github.xpakx.alingo.stats.graphql;

import io.github.xpakx.alingo.security.JwtUtils;
import io.github.xpakx.alingo.stats.Guess;
import io.github.xpakx.alingo.stats.GuessRepository;
import io.github.xpakx.alingo.utils.GraphGuess;
import io.github.xpakx.alingo.utils.GraphQuery;
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
import static org.hamcrest.Matchers.*;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.HttpStatus.FORBIDDEN;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GraphGuessControllerTest {
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

    private GraphQuery getGraphQueryForGuesses(GraphGuess answer) {
        GraphQuery query = new GraphQuery();
        query.setQuery("""
                query getGuesses($username: String, $page: Int, $amount: Int){
                    getGuesses(username: $username, page: $page, amount: $amount)
                    {
                        content {
                            id
                            username
                            correct
                            letter
                            exerciseId
                            courseId
                            courseName
                            language
                        }
                        totalPages
                        totalElements
                        last
                        size
                        number
                        numberOfElements
                        first
                        empty
                    }
                }""");
        query.setVariables(answer);
        return query;
    }

    private GraphGuess getVariablesForGuesses(String username, Integer page, Integer amount) {
        return new GraphGuess(username, page, amount);
    }

    @Test
    void shouldRespondWith401ToGetGuessesIfNotAuthenticated() {
        GraphQuery query = getGraphQueryForGuesses(getVariablesForGuesses("user1", 1, 10));
        given()
                .contentType(ContentType.JSON)
                .body(query)
        .when()
                .post(baseUrl + "/graphql")
        .then()
                .statusCode(UNAUTHORIZED.value());
    }

    @Test
    void shouldRespondWith401ToGetGuessesIfTokenIsWrong() {
        GraphQuery query = getGraphQueryForGuesses(getVariablesForGuesses("user1", 1, 10));
        given()
                .auth()
                .oauth2("3040234")
                .contentType(ContentType.JSON)
                .body(query)
        .when()
                .post(baseUrl + "/graphql")
        .then()
                .statusCode(UNAUTHORIZED.value());
    }

    @ParameterizedTest
    @ValueSource(ints = {-10, -1, 0})
    void shouldNotAcceptRequestWithNonPositivePages(int page) {
        GraphQuery query = getGraphQueryForGuesses(getVariablesForGuesses("user1", page, 10));
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
                .body("errors", not((nullValue())));
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0, 21, 50})
    void shouldNotAcceptRequestWithAmountOutsideBounds(int amount) {
        GraphQuery query = getGraphQueryForGuesses(getVariablesForGuesses("user1", 1, amount));
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
                .body("errors", not((nullValue())));
    }

    @Test
    void shouldRespondWithListOfGuessesForUser() {
        addGuesses("user1", 5);
        GraphQuery query = getGraphQueryForGuesses(getVariablesForGuesses("user1", 1, 10));
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
                .body("data.getGuesses.content", hasSize(5));
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
        GraphQuery query = getGraphQueryForGuesses(getVariablesForGuesses("user2", 1, 10));
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
                .body("errors", not((nullValue())));
    }

}