package io.github.xpakx.alingo.user.graphql;

import io.github.xpakx.alingo.clients.AccountPublisher;
import io.github.xpakx.alingo.user.Account;
import io.github.xpakx.alingo.user.AccountRepository;
import io.github.xpakx.alingo.user.UserRoleRepository;
import io.github.xpakx.alingo.utils.GraphLogin;
import io.github.xpakx.alingo.utils.GraphQuery;
import io.github.xpakx.alingo.utils.GraphRegister;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpStatus.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GraphAuthControllerTest {
    @LocalServerPort
    private int port;
    private String baseUrl;

    @Autowired
    AccountRepository accountRepository;
    @Autowired
    UserRoleRepository roleRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @MockBean
    AccountPublisher publisher;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost".concat(":").concat(port + "");
        Account user = new Account();
        user.setPassword(passwordEncoder.encode("password"));
        user.setUsername("user1");
        accountRepository.save(user);
    }

    @AfterEach
    void tearDown() {
        roleRepository.deleteAll();
        accountRepository.deleteAll();
    }

    private GraphQuery getGraphQueryForRegistration(GraphRegister register) {
        GraphQuery query = new GraphQuery();
        query.setQuery("""
                    mutation register($username: String, $password: String, $passwordRe: String){
                        register(username: $username, password: $password, passwordRe: $passwordRe)
                        {
                            token
                            username
                        }
                    }""");
        query.setVariables(register);
        return query;
    }

    private GraphRegister getVariablesForRegistration(String username, String password, String passwordRe) {
        GraphRegister variables = new GraphRegister();
        variables.setUsername(username);
        variables.setPassword(password);
        variables.setPasswordRe(passwordRe);
        return variables;
    }

    @Test
    void shouldNotAcceptUnmatchedPasswords() {
        GraphQuery query = getGraphQueryForRegistration(getVariablesForRegistration("user2", "password1", "password2"));
        given()
                .contentType(ContentType.JSON)
                .body(query)
        .when()
                .post(baseUrl + "/graphql")
        .then()
                .statusCode(OK.value())
                .body("data", nullValue())
                .body("errors", not(nullValue()));
    }

    @Test
    void shouldNotRegisterUserWithAlreadyTakenUsername() {
        GraphQuery query = getGraphQueryForRegistration(getVariablesForRegistration("user1", "password", "password"));
        given()
                .contentType(ContentType.JSON)
                .body(query)
        .when()
                .post(baseUrl + "/graphql")
        .then()
                .statusCode(OK.value())
                .body("data", nullValue())
                .body("errors", not(nullValue()));
    }

    @Test
    void shouldRegisterUser() {
        GraphQuery query = getGraphQueryForRegistration(getVariablesForRegistration("user2", "password", "password"));
        given()
                .contentType(ContentType.JSON)
                .body(query)
        .when()
                .post(baseUrl + "/graphql")
        .then()
                .statusCode(OK.value())
                .body("data", not(nullValue()))
                .body("data.register.username", equalTo("user2"));
    }

    @Test
    void shouldAddNewUsernameToDb() {
        GraphQuery query = getGraphQueryForRegistration(getVariablesForRegistration("user2", "password", "password"));
        given()
                .contentType(ContentType.JSON)
                .body(query)
        .when()
                .post(baseUrl + "/graphql");
        List<Account> accounts = accountRepository.findAll();
        assertThat(accounts, hasItem(hasProperty("username", equalTo("user2"))));
    }

    @Test
    void shouldReturnTokenForRegisteredUser() {
        GraphQuery query = getGraphQueryForRegistration(getVariablesForRegistration("user3", "password", "password"));
        given()
                .contentType(ContentType.JSON)
                .body(query)
        .when()
                .post(baseUrl + "/graphql")
        .then()
                .log().body()
                .statusCode(OK.value())
                .body("data.register.token", not(nullValue()));
    }

    private GraphQuery getGraphQueryForLogin(GraphLogin register) {
        GraphQuery query = new GraphQuery();
        query.setQuery("""
                    mutation login($username: String, $password: String){
                        login(username: $username, password: $password)
                        {
                            token
                            username
                        }
                    }""");
        query.setVariables(register);
        return query;
    }

    private GraphLogin getVariablesForLogin(String username, String password) {
        GraphLogin variables = new GraphLogin();
        variables.setUsername(username);
        variables.setPassword(password);
        return variables;
    }

    @Test
    void shouldNotAuthenticateIfPasswordIsWrong() {
        GraphQuery query = getGraphQueryForLogin(getVariablesForLogin("user1", "password2"));
        given()
                .contentType(ContentType.JSON)
                .body(query)
        .when()
                .post(baseUrl + "/graphql")
        .then()
                .statusCode(OK.value())
                .body("data", nullValue())
                .body("errors", not(nullValue()));
    }

    @Test
    void shouldNotAuthenticateIfUserDoesNotExistInDb() {
        GraphQuery query = getGraphQueryForLogin(getVariablesForLogin("user2", "password"));
        given()
                .contentType(ContentType.JSON)
                .body(query)
        .when()
                .post(baseUrl + "/graphql")
        .then()
                .statusCode(OK.value())
                .body("data", nullValue())
                .body("errors", not(nullValue()));
    }

    @Test
    void shouldAuthenticate() {
        GraphQuery query = getGraphQueryForLogin(getVariablesForLogin("user1", "password"));
        given()
                .contentType(ContentType.JSON)
                .body(query)
        .when()
                .post(baseUrl + "/graphql")
        .then()
                .statusCode(OK.value())
                .body("data", not(nullValue()))
                .body("data.login.username", equalTo("user1"));
    }

    @Test
    void shouldReturnToken() {
        GraphQuery query = getGraphQueryForLogin(getVariablesForLogin("user1", "password"));
        given()
                .contentType(ContentType.JSON)
                .body(query)
        .when()
                .post(baseUrl + "/graphql")
        .then()
                .statusCode(OK.value())
                .body("data.login.token", not(nullValue()));
    }

    @Test
    void shouldNotValidateAuthRequestIfPasswordIsEmpty() {
        GraphQuery query = getGraphQueryForLogin(getVariablesForLogin("user1", ""));
        given()
                .contentType(ContentType.JSON)
                .body(query)
        .when()
                .post(baseUrl + "/graphql")
        .then()
                .statusCode(OK.value())
                .body("data", nullValue())
                .body("errors", not(nullValue()));
    }

    @Test
    void shouldNotValidateAuthRequestIfPasswordIsNull() {
        GraphQuery query = getGraphQueryForLogin(getVariablesForLogin("user1", null));
        given()
                .contentType(ContentType.JSON)
                .body(query)
        .when()
                .post(baseUrl + "/graphql")
        .then()
                .statusCode(OK.value())
                .body("data", nullValue())
                .body("errors", not(nullValue()));
    }

    @Test
    void shouldNotValidateAuthRequestIfPUsernameIsEmpty() {
        GraphQuery query = getGraphQueryForLogin(getVariablesForLogin("", "password"));
        given()
                .contentType(ContentType.JSON)
                .body(query)
        .when()
                .post(baseUrl + "/graphql")
        .then()
                .statusCode(OK.value())
                .body("data", nullValue())
                .body("errors", not(nullValue()));
    }

    @Test
    void shouldNotValidateAuthRequestIfUsernameIsNull() {
        GraphQuery query = getGraphQueryForLogin(getVariablesForLogin(null, "password"));
        given()
                .contentType(ContentType.JSON)
                .body(query)
        .when()
                .post(baseUrl + "/graphql")
        .then()
                .statusCode(OK.value())
                .body("data", nullValue())
                .body("errors", not(nullValue()));
    }

    @Test
    void shouldNotAcceptNullUsername() {
        GraphQuery query = getGraphQueryForRegistration(getVariablesForRegistration(null, "password", "password"));
        given()
                .contentType(ContentType.JSON)
                .body(query)
        .when()
                .post(baseUrl + "/graphql")
        .then()
                .statusCode(OK.value())
                .body("data", nullValue())
                .body("errors", not(nullValue()));
    }

    @Test
    void shouldNotAcceptEmptyUsername() {
        GraphQuery query = getGraphQueryForRegistration(getVariablesForRegistration("", "password", "password"));
        given()
                .contentType(ContentType.JSON)
                .body(query)
        .when()
                .post(baseUrl + "/graphql")
        .then()
                .statusCode(OK.value())
                .body("data", nullValue())
                .body("errors", not(nullValue()));
    }

    @Test
    void shouldNotAcceptBlankUsername() {
        GraphQuery query = getGraphQueryForRegistration(getVariablesForRegistration("    ", "password", "password"));
        given()
                .contentType(ContentType.JSON)
                .body(query)
        .when()
                .post(baseUrl + "/graphql")
        .then()
                .statusCode(OK.value())
                .body("data", nullValue())
                .body("errors", not(nullValue()));
    }

    @Test
    void shouldNotAcceptUsernameShorterThanFiveCharacters() {
        GraphQuery query = getGraphQueryForRegistration(getVariablesForRegistration("user", "password", "password"));
        given()
                .contentType(ContentType.JSON)
                .body(query)
        .when()
                .post(baseUrl + "/graphql")
        .then()
                .statusCode(OK.value())
                .body("data", nullValue())
                .body("errors", not(nullValue()));
    }

    @Test
    void shouldNotAcceptUsernameLongerThanFifteenCharacters() {
        GraphQuery query = getGraphQueryForRegistration(getVariablesForRegistration("userWithVeryLongUsername", "password", "password"));
        given()
                .contentType(ContentType.JSON)
                .body(query)
        .when()
                .post(baseUrl + "/graphql")
        .then()
                .statusCode(OK.value())
                .body("data", nullValue())
                .body("errors", not(nullValue()));
    }

    @Test
    void shouldNotAcceptNullPassword() {
        GraphQuery query = getGraphQueryForRegistration(getVariablesForRegistration("user5", null, null));
        given()
                .contentType(ContentType.JSON)
                .body(query)
        .when()
                .post(baseUrl + "/graphql")
        .then()
                .statusCode(OK.value())
                .body("data", nullValue())
                .body("errors", not(nullValue()));
    }

    @Test
    void shouldNotAcceptEmptyPassword() {
        GraphQuery query = getGraphQueryForRegistration(getVariablesForRegistration("user5", "", ""));
        given()
                .contentType(ContentType.JSON)
                .body(query)
        .when()
                .post(baseUrl + "/graphql")
        .then()
                .statusCode(OK.value())
                .body("data", nullValue())
                .body("errors", not(nullValue()));
    }

    @Test
    void shouldNotAcceptBlankPassword() {
        GraphQuery query = getGraphQueryForRegistration(getVariablesForRegistration("user5", "  ", "  "));
        given()
                .contentType(ContentType.JSON)
                .body(query)
        .when()
                .post(baseUrl + "/graphql")
        .then()
                .statusCode(OK.value())
                .body("data", nullValue())
                .body("errors", not(nullValue()));
    }
}