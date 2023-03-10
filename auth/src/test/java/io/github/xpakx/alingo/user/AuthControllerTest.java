package io.github.xpakx.alingo.user;

import io.github.xpakx.alingo.clients.AccountPublisher;
import io.github.xpakx.alingo.user.dto.AuthenticationRequest;
import io.github.xpakx.alingo.user.dto.RegistrationRequest;
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
import static org.springframework.http.HttpStatus.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthControllerTest {
    @LocalServerPort
    private int port;
    private String baseUrl;
    private Long userId;

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
        userId = accountRepository.save(user).getId();
    }

    @AfterEach
    void tearDown() {
        roleRepository.deleteAll();
        accountRepository.deleteAll();
    }

    private RegistrationRequest createRegistrationRequest(String username, String password, String repeatedPassword) {
        RegistrationRequest request = new RegistrationRequest();
        request.setUsername(username);
        request.setPassword(password);
        request.setPasswordRe(repeatedPassword);
        return request;
    }

    @Test
    void shouldNotAcceptUnmatchedPasswords() {
        RegistrationRequest request = createRegistrationRequest("user2", "password1", "password2");
        given()
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .post(baseUrl + "/register")
        .then()
                .statusCode(BAD_REQUEST.value())
                .body("error", equalTo(BAD_REQUEST.value()))
                .body("message", containsStringIgnoringCase("Validation failed"))
                .body("errors", hasSize(1))
                .body("errors", hasItem(both(containsStringIgnoringCase("passwords")).and(containsStringIgnoringCase("match"))));
    }

    @Test
    void shouldNotRegisterUserWithAlreadyTakenUsername() {
        RegistrationRequest request = createRegistrationRequest("user1", "password", "password");
        given()
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .post(baseUrl + "/register")
        .then()
                .statusCode(BAD_REQUEST.value())
                .body("error", equalTo(BAD_REQUEST.value()))
                .body("message", containsStringIgnoringCase("username"))
                .body("errors", nullValue());
    }

    @Test
    void shouldRegisterUser() {
        RegistrationRequest request = createRegistrationRequest("user2", "password", "password");
        given()
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .post(baseUrl + "/register")
        .then()
                .statusCode(CREATED.value());
    }

    @Test
    void shouldAddNewUsernameToDb() {
        RegistrationRequest request = createRegistrationRequest("user2", "password", "password");
        given()
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .post(baseUrl + "/register");
        List<Account> accounts = accountRepository.findAll();
        assertThat(accounts, hasItem(hasProperty("username", equalTo("user2"))));
    }

    @Test
    void shouldReturnTokenForRegisteredUser() {
        RegistrationRequest request = createRegistrationRequest("user3", "password", "password");
        given()
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .post(baseUrl + "/register")
        .then()
                .statusCode(CREATED.value())
                .body("token", notNullValue());
    }

    private AuthenticationRequest getAuthRequest(String username, String password) {
        AuthenticationRequest request = new AuthenticationRequest();
        request.setUsername(username);
        request.setPassword(password);
        return request;
    }

    @Test
    void shouldNotAuthenticateIfPasswordIsWrong() {
        AuthenticationRequest request = getAuthRequest("user1", "password2");
        given()
                .contentType(ContentType.JSON)
                .body(request)
         .when()
                .post(baseUrl + "/authenticate")
         .then()
                .statusCode(UNAUTHORIZED.value())
                .body("error", equalTo(UNAUTHORIZED.value()))
                .body("message", containsStringIgnoringCase("invalid password"))
                .body("errors", nullValue());
    }

    @Test
    void shouldNotAuthenticateIfUserDoesNotExistInDb() {
        AuthenticationRequest request = getAuthRequest("user2", "password");
        given()
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .post(baseUrl + "/authenticate")
        .then()
                .statusCode(FORBIDDEN.value())
                .body("error", equalTo(FORBIDDEN.value()))
                .body("message", containsStringIgnoringCase("no user"))
                .body("errors", nullValue());
    }

    @Test
    void shouldAuthenticate() {
        AuthenticationRequest request = getAuthRequest("user1", "password");
        given()
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .post(baseUrl + "/authenticate")
        .then()
                .statusCode(OK.value());
    }

    @Test
    void shouldReturnToken() {
        AuthenticationRequest request = getAuthRequest("user1", "password");
        given()
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .post(baseUrl + "/authenticate")
        .then()
                .statusCode(OK.value())
                .body("token", notNullValue());
    }

    @Test
    void shouldNotValidateAuthRequestIfPasswordIsEmpty() {
        AuthenticationRequest request = getAuthRequest("user1", "");
        given()
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .post(baseUrl + "/authenticate")
        .then()
                .statusCode(BAD_REQUEST.value())
                .body("error", equalTo(BAD_REQUEST.value()))
                .body("message", containsStringIgnoringCase("Validation failed"))
                .body("errors", hasSize(1))
                .body("errors", hasItem(both(containsStringIgnoringCase("password")).and(containsStringIgnoringCase("empty"))));
    }

    @Test
    void shouldNotValidateAuthRequestIfPasswordIsNull() {
        AuthenticationRequest request = getAuthRequest("user1", null);
        given()
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .post(baseUrl + "/authenticate")
        .then()
                .statusCode(BAD_REQUEST.value())
                .body("error", equalTo(BAD_REQUEST.value()))
                .body("message", containsStringIgnoringCase("Validation failed"))
                .body("errors", hasSize(1))
                .body("errors", hasItem(both(containsStringIgnoringCase("password")).and(containsStringIgnoringCase("empty"))));
    }

    @Test
    void shouldNotValidateAuthRequestIfUsernameIsEmpty() {
        AuthenticationRequest request = getAuthRequest("", "password");
        given()
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .post(baseUrl + "/authenticate")
        .then()
                .statusCode(BAD_REQUEST.value())
                .body("error", equalTo(BAD_REQUEST.value()))
                .body("message", containsStringIgnoringCase("Validation failed"))
                .body("errors", hasSize(1))
                .body("errors", hasItem(both(containsStringIgnoringCase("username")).and(containsStringIgnoringCase("empty"))));
    }

    @Test
    void shouldNotValidateAuthRequestIfUsernameIsNull() {
        AuthenticationRequest request = getAuthRequest(null, "password");
        given()
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .post(baseUrl + "/authenticate")
        .then()
                .statusCode(BAD_REQUEST.value())
                .body("error", equalTo(BAD_REQUEST.value()))
                .body("message", containsStringIgnoringCase("Validation failed"))
                .body("errors", hasSize(1))
                .body("errors", hasItem(both(containsStringIgnoringCase("username")).and(containsStringIgnoringCase("empty"))));
    }

    @Test
    void shouldNotAcceptNullUsername() {
        RegistrationRequest request = createRegistrationRequest(null, "password", "password");
        given()
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .post(baseUrl + "/register")
        .then()
                .statusCode(BAD_REQUEST.value())
                .body("error", equalTo(BAD_REQUEST.value()))
                .body("message", containsStringIgnoringCase("Validation failed"))
                .body("errors", hasSize(1))
                .body("errors", hasItem(both(containsStringIgnoringCase("username")).and(containsStringIgnoringCase("empty"))));
    }

    @Test
    void shouldNotAcceptEmptyUsername() {
        RegistrationRequest request = createRegistrationRequest("", "password", "password");
        given()
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .post(baseUrl + "/register")
        .then()
                .statusCode(BAD_REQUEST.value())
                .body("error", equalTo(BAD_REQUEST.value()))
                .body("message", containsStringIgnoringCase("Validation failed"))
                .body("errors", hasItem(both(containsStringIgnoringCase("username")).and(containsStringIgnoringCase("empty"))));
    }

    @Test
    void shouldNotAcceptBlankUsername() {
        RegistrationRequest request = createRegistrationRequest("   ", "password", "password");
        given()
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .post(baseUrl + "/register")
        .then()
                .statusCode(BAD_REQUEST.value())
                .body("error", equalTo(BAD_REQUEST.value()))
                .body("message", containsStringIgnoringCase("Validation failed"))
                .body("errors", hasItem(both(containsStringIgnoringCase("username")).and(containsStringIgnoringCase("empty"))));
    }

    @Test
    void shouldNotAcceptUsernameShorterThanFiveCharacters() {
        RegistrationRequest request = createRegistrationRequest("user", "password", "password");
        given()
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .post(baseUrl + "/register")
        .then()
                .statusCode(BAD_REQUEST.value())
                .body("error", equalTo(BAD_REQUEST.value()))
                .body("message", containsStringIgnoringCase("Validation failed"))
                .body("errors", hasSize(1))
                .body("errors", hasItem(both(containsStringIgnoringCase("username")).and(containsStringIgnoringCase("length"))));
    }

    @Test
    void shouldNotAcceptUsernameLongerThanFifteenCharacters() {
        RegistrationRequest request = createRegistrationRequest("userWithVeryLongUsername", "password", "password");
        given()
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .post(baseUrl + "/register")
        .then()
                .statusCode(BAD_REQUEST.value())
                .body("error", equalTo(BAD_REQUEST.value()))
                .body("message", containsStringIgnoringCase("Validation failed"))
                .body("errors", hasSize(1))
                .body("errors", hasItem(both(containsStringIgnoringCase("username")).and(containsStringIgnoringCase("length"))));
    }

    @Test
    void shouldNotAcceptNullPassword() {
        RegistrationRequest request = createRegistrationRequest("user5", null, null);
        given()
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .post(baseUrl + "/register")
        .then()
                .statusCode(BAD_REQUEST.value())
                .body("error", equalTo(BAD_REQUEST.value()))
                .body("message", containsStringIgnoringCase("Validation failed"))
                .body("errors", hasSize(1))
                .body("errors", hasItem(both(containsStringIgnoringCase("password")).and(containsStringIgnoringCase("empty"))));
    }

    @Test
    void shouldNotAcceptEmptyPassword() {
        RegistrationRequest request = createRegistrationRequest("user5", "", "");
        given()
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .post(baseUrl + "/register")
        .then()
                .statusCode(BAD_REQUEST.value())
                .body("error", equalTo(BAD_REQUEST.value()))
                .body("message", containsStringIgnoringCase("Validation failed"))
                .body("errors", hasSize(1))
                .body("errors", hasItem(both(containsStringIgnoringCase("password")).and(containsStringIgnoringCase("empty"))));
    }

    @Test
    void shouldNotAcceptBlankPassword() {
        RegistrationRequest request = createRegistrationRequest("user5", "  ", "  ");
        given()
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .post(baseUrl + "/register")
        .then()
                .statusCode(BAD_REQUEST.value())
                .body("error", equalTo(BAD_REQUEST.value()))
                .body("message", containsStringIgnoringCase("Validation failed"))
                .body("errors", hasSize(1))
                .body("errors", hasItem(both(containsStringIgnoringCase("password")).and(containsStringIgnoringCase("empty"))));
    }
}