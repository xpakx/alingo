package io.github.xpakx.alingo.user.graphql;

import io.github.xpakx.alingo.clients.AccountPublisher;
import io.github.xpakx.alingo.security.JwtUtils;
import io.github.xpakx.alingo.user.Account;
import io.github.xpakx.alingo.user.AccountRepository;
import io.github.xpakx.alingo.user.UserRoleRepository;
import io.github.xpakx.alingo.user.dto.RegistrationRequest;
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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
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
                    mutation answer($username: String, $password: String, $passwordRe: String){
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

}