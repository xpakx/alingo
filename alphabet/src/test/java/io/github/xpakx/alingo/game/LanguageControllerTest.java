package io.github.xpakx.alingo.game;

import io.github.xpakx.alingo.game.dto.LanguageRequest;
import io.github.xpakx.alingo.security.JwtUtils;
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
import static io.restassured.RestAssured.when;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpStatus.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LanguageControllerTest {
    @LocalServerPort
    private int port;
    private String baseUrl;

    @Autowired
    JwtUtils jwt;
    @Autowired
    LanguageRepository languageRepository;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost".concat(":").concat(port + "");
    }

    @AfterEach
    void tearDown() {
        languageRepository.deleteAll();
    }

    @Test
    void shouldRespondWith401ToAddLanguageIfNotAuthenticated() {
        when()
                .post(baseUrl + "/language")
        .then()
                .statusCode(UNAUTHORIZED.value())
                .body("error", equalTo(UNAUTHORIZED.value()))
                .body("errors", nullValue());
    }

    @Test
    void shouldRespondWith401ToAddLanguageIfTokenIsWrong() {
        given()
                .auth()
                .oauth2("329432853295")
                .contentType(ContentType.JSON)
                .body(getLanguageRequest("language1"))
        .when()
                .post(baseUrl + "/language")
        .then()
                .statusCode(UNAUTHORIZED.value())
                .body("error", equalTo(UNAUTHORIZED.value()))
                .body("errors", nullValue());
    }

    private LanguageRequest getLanguageRequest(String name) {
        LanguageRequest request = new LanguageRequest();
        request.setName(name);
        return request;
    }

    @Test
    void shouldRespondWith403ToAddLanguageIfUserIsNotModerator() {
        given()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(getLanguageRequest("language1"))
        .when()
                .post(baseUrl + "/language")
        .then()
                .statusCode(FORBIDDEN.value())
                .body("error", equalTo(FORBIDDEN.value()))
                .body("errors", nullValue());
    }

    private String tokenFor(String username) {
        return tokenFor(username, new ArrayList<>());
    }

    private String tokenFor(String username, List<GrantedAuthority> authorities) {
        return jwt.generateToken(new User(username, "", authorities));
    }

    @Test
    void shouldAddLanguage() {
        given()
                .auth()
                .oauth2(tokenFor("user1", List.of(new SimpleGrantedAuthority("MODERATOR"))))
                .contentType(ContentType.JSON)
                .body(getLanguageRequest("language1"))
        .when()
                .post(baseUrl + "/language")
        .then()
                .statusCode(OK.value())
                .body("name", equalTo("language1"));
    }

    @Test
    void shouldAddNewLanguageToDb() {
        given()
                .auth()
                .oauth2(tokenFor("user1", List.of(new SimpleGrantedAuthority("MODERATOR"))))
                .contentType(ContentType.JSON)
                .body(getLanguageRequest("newLanguage"))
        .when()
                .post(baseUrl + "/language");
        List<Language> languages = languageRepository.findAll();
        assertThat(languages, hasItem(hasProperty("name", equalTo("newLanguage"))));
    }

    @Test
    void shouldNotAcceptEmptyLanguageName() {
        given()
                .auth()
                .oauth2(tokenFor("user1", List.of(new SimpleGrantedAuthority("MODERATOR"))))
                .contentType(ContentType.JSON)
                .body(getLanguageRequest(""))
        .when()
                .post(baseUrl + "/language")
        .then()
                .statusCode(BAD_REQUEST.value())
                .body("error", equalTo(BAD_REQUEST.value()))
                .body("message", containsStringIgnoringCase("Validation failed"))
                .body("errors", hasItem(both(containsStringIgnoringCase("name")).and(containsStringIgnoringCase("empty"))));
    }

    @Test
    void shouldNotAcceptNullLanguageName() {
        given()
                .auth()
                .oauth2(tokenFor("user1", List.of(new SimpleGrantedAuthority("MODERATOR"))))
                .contentType(ContentType.JSON)
                .body(getLanguageRequest(null))
        .when()
                .post(baseUrl + "/language")
        .then()
                .statusCode(BAD_REQUEST.value())
                .body("error", equalTo(BAD_REQUEST.value()))
                .body("message", containsStringIgnoringCase("Validation failed"))
                .body("errors", hasItem(both(containsStringIgnoringCase("name")).and(containsStringIgnoringCase("empty"))));
    }

    @Test
    void shouldRespondWith401ToUpdateLanguageIfNotAuthenticated() {
        when()
                .put(baseUrl + "/language/{languageId}", 1L)
        .then()
                .statusCode(UNAUTHORIZED.value())
                .body("error", equalTo(UNAUTHORIZED.value()))
                .body("errors", nullValue());
    }

    @Test
    void shouldRespondWith401ToUpdateLanguageIfTokenIsWrong() {
        given()
                .auth()
                .oauth2("329432853295")
                .contentType(ContentType.JSON)
                .body(getLanguageRequest("language1"))
        .when()
                .put(baseUrl + "/language/{languageId}", 1L)
        .then()
                .statusCode(UNAUTHORIZED.value())
                .body("error", equalTo(UNAUTHORIZED.value()))
                .body("errors", nullValue());
    }

    @Test
    void shouldRespondWith403ToUpdateLanguageIfUserIsNotModerator() {
        given()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(getLanguageRequest("language1"))
        .when()
                .put(baseUrl + "/language/{languageId}", 1L)
        .then()
                .statusCode(FORBIDDEN.value())
                .body("error", equalTo(FORBIDDEN.value()))
                .body("errors", nullValue());
    }

    @Test
    void shouldRespondWith404ToUpdateLanguageIfLanguageDoesNotExist() {
        given()
                .auth()
                .oauth2(tokenFor("user1", List.of(new SimpleGrantedAuthority("MODERATOR"))))
                .contentType(ContentType.JSON)
                .body(getLanguageRequest("language1"))
        .when()
                .put(baseUrl + "/language/{languageId}", 1L)
        .then()
                .statusCode(NOT_FOUND.value())
                .body("error", equalTo(NOT_FOUND.value()))
                .body("errors", nullValue());
    }

    @Test
    void shouldUpdateLanguage() {
        Long languageId = addLanguage("language1");
        given()
                .auth()
                .oauth2(tokenFor("user1", List.of(new SimpleGrantedAuthority("MODERATOR"))))
                .contentType(ContentType.JSON)
                .body(getLanguageRequest("newName"))
        .when()
                .put(baseUrl + "/language/{languageId}", languageId)
        .then()
                .statusCode(OK.value())
                .body("name", equalTo("newName"));
    }

    private Long addLanguage(String name) {
        Language language = new Language();
        language.setName(name);
        return languageRepository.save(language).getId();
    }

    @Test
    void shouldUpdateLanguageIndDb() {
        Long languageId = addLanguage("language1");
        given()
                .auth()
                .oauth2(tokenFor("user1", List.of(new SimpleGrantedAuthority("MODERATOR"))))
                .contentType(ContentType.JSON)
                .body(getLanguageRequest("newName"))
        .when()
                .put(baseUrl + "/language/{languageId}", languageId);
        Optional<Language> language = languageRepository.findById(languageId);
        assertTrue(language.isPresent());
        assertThat(language.get(), hasProperty("name", equalTo("newLanguage")));
    }
}