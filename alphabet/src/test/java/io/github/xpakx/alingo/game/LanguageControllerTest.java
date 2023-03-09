package io.github.xpakx.alingo.game;

import io.github.xpakx.alingo.game.dto.LanguageRequest;
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
    @Autowired
    CourseRepository courseRepository;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost".concat(":").concat(port + "");
    }

    @AfterEach
    void tearDown() {
        courseRepository.deleteAll();
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
        LanguageRequest request = new LanguageRequest(name);
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
                .statusCode(CREATED.value())
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
        assertThat(language.get(), hasProperty("name", equalTo("newName")));
    }

    @Test
    void shouldNotAcceptEmptyLanguageNameWhileUpdating() {
        Long languageId = addLanguage("language1");
        given()
                .auth()
                .oauth2(tokenFor("user1", List.of(new SimpleGrantedAuthority("MODERATOR"))))
                .contentType(ContentType.JSON)
                .body(getLanguageRequest(""))
        .when()
                .put(baseUrl + "/language/{languageId}", languageId)
        .then()
                .statusCode(BAD_REQUEST.value())
                .body("error", equalTo(BAD_REQUEST.value()))
                .body("message", containsStringIgnoringCase("Validation failed"))
                .body("errors", hasItem(both(containsStringIgnoringCase("name")).and(containsStringIgnoringCase("empty"))));
    }

    @Test
    void shouldNotAcceptNullLanguageNameWhileUpdating() {
        Long languageId = addLanguage("language1");
        given()
                .auth()
                .oauth2(tokenFor("user1", List.of(new SimpleGrantedAuthority("MODERATOR"))))
                .contentType(ContentType.JSON)
                .body(getLanguageRequest(null))
         .when()
                .put(baseUrl + "/language/{languageId}", languageId)
         .then()
                .statusCode(BAD_REQUEST.value())
                .body("error", equalTo(BAD_REQUEST.value()))
                .body("message", containsStringIgnoringCase("Validation failed"))
                .body("errors", hasItem(both(containsStringIgnoringCase("name")).and(containsStringIgnoringCase("empty"))));
    }

    @Test
    void shouldRespondWith401ToGetLanguageIfNotAuthenticated() {
        when()
                .get(baseUrl + "/language/{languageId}", 1L)
        .then()
                .statusCode(UNAUTHORIZED.value())
                .body("error", equalTo(UNAUTHORIZED.value()))
                .body("errors", nullValue());
    }

    @Test
    void shouldRespondWith401ToGetLanguageIfTokenIsWrong() {
        given()
                .auth()
                .oauth2("329432853295")
        .when()
                .get(baseUrl + "/language/{languageId}", 1L)
        .then()
                .statusCode(UNAUTHORIZED.value())
                .body("error", equalTo(UNAUTHORIZED.value()))
                .body("errors", nullValue());
    }

    @Test
    void shouldRespondWith403ToGetLanguageIfUserIsNotModerator() {
        given()
                .auth()
                .oauth2(tokenFor("user1"))
        .when()
                .get(baseUrl + "/language/{languageId}", 1L)
        .then()
                .statusCode(FORBIDDEN.value())
                .body("error", equalTo(FORBIDDEN.value()))
                .body("errors", nullValue());
    }

    @Test
    void shouldRespondWith404ToGetLanguageIfLanguageDoesNotExist() {
        given()
                .auth()
                .oauth2(tokenFor("user1", List.of(new SimpleGrantedAuthority("MODERATOR"))))
        .when()
                .get(baseUrl + "/language/{languageId}", 1L)
        .then()
                .statusCode(NOT_FOUND.value())
                .body("error", equalTo(NOT_FOUND.value()))
                .body("errors", nullValue());
    }

    @Test
    void shouldReturnLanguage() {
        Long languageId = addLanguage("language");
        given()
                .auth()
                .oauth2(tokenFor("user1", List.of(new SimpleGrantedAuthority("MODERATOR"))))
        .when()
                .get(baseUrl + "/language/{languageId}", languageId)
        .then()
                .statusCode(OK.value())
                .body("name", equalTo("language"));
    }

    @Test
    void shouldRespondWith401ToGetLanguagesIfNotAuthenticated() {
        given()
                .param("page", 1)
                .param("amount", 10)
        .when()
                .get(baseUrl + "/language")
        .then()
                .statusCode(UNAUTHORIZED.value())
                .body("error", equalTo(UNAUTHORIZED.value()))
                .body("errors", nullValue());
    }

    @Test
    void shouldRespondWith401ToGetLanguagesIfTokenIsWrong() {
        given()
                .auth()
                .oauth2("21090cjw")
                .param("page", 1)
                .param("amount", 10)
        .when()
                .get(baseUrl + "/language")
        .then()
                .statusCode(UNAUTHORIZED.value())
                .body("error", equalTo(UNAUTHORIZED.value()))
                .body("errors", nullValue());
    }

    @Test
    void shouldRespondWith403ToGetLanguagesIfUserIsNotModerator() {
        given()
                .auth()
                .oauth2(tokenFor("user1"))
                .param("page", 1)
                .param("amount", 10)
        .when()
                .get(baseUrl + "/language")
        .then()
                .statusCode(FORBIDDEN.value())
                .body("error", equalTo(FORBIDDEN.value()))
                .body("errors", nullValue());
    }

    @Test
    void shouldRespondWithEmptyListToGetLanguagesIfThereAreNoLanguages() {
        given()
                .auth()
                .oauth2(tokenFor("user1", List.of(new SimpleGrantedAuthority("MODERATOR"))))
                .param("page", 1)
                .param("amount", 10)
        .when()
                .get(baseUrl + "/language")
        .then()
                .statusCode(OK.value())
                .body("$", hasSize(0));
    }

    @ParameterizedTest
    @ValueSource(ints = {-10, -1, 0})
    void shouldNotAcceptGetLanguagesRequestWithNonPositivePages(int page) {
        given()
                .auth()
                .oauth2(tokenFor("user1", List.of(new SimpleGrantedAuthority("MODERATOR"))))
                .param("page", page)
                .param("amount", 10)
        .when()
                .get(baseUrl + "/language")
        .then()
                .statusCode(BAD_REQUEST.value())
                .body("error", equalTo(BAD_REQUEST.value()))
                .body("message", containsStringIgnoringCase("validation failed"))
                .body("errors", hasItem(both(containsStringIgnoringCase("page")).and(containsStringIgnoringCase("positive"))));
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0, 21, 50})
    void shouldNotAcceptGetLanguagesRequestWithAmountOutsideBounds(int amount) {
        given()
                .auth()
                .oauth2(tokenFor("user1", List.of(new SimpleGrantedAuthority("MODERATOR"))))
                .param("page", 1)
                .param("amount", amount)
        .when()
                .get(baseUrl + "/language")
        .then()
                .statusCode(BAD_REQUEST.value())
                .body("error", equalTo(BAD_REQUEST.value()))
                .body("message", containsStringIgnoringCase("validation failed"))
                .body("errors", hasItem(both(containsStringIgnoringCase("amount")).and(containsStringIgnoringCase("between"))));
    }

    @Test
    void shouldRespondWithListOfLanguages() {
        addLanguage("lang1");
        addLanguage("lang2");
        addLanguage("lang3");
        given()
                .auth()
                .oauth2(tokenFor("user1", List.of(new SimpleGrantedAuthority("MODERATOR"))))
                .param("page", 1)
                .param("amount", 2)
        .when()
                .get(baseUrl + "/language")
        .then()
                .statusCode(OK.value())
                .body("$", hasSize(2));
    }

    @Test
    void shouldRespondWith401ToGetCoursesIfNotAuthenticated() {
        given()
                .param("page", 1)
                .param("amount", 10)
        .when()
                .get(baseUrl + "/language/{languageId}/course", 1L)
        .then()
                .statusCode(UNAUTHORIZED.value())
                .body("error", equalTo(UNAUTHORIZED.value()))
                .body("errors", nullValue());
    }

    @Test
    void shouldRespondWith401ToGetCoursesIfTokenIsWrong() {
        given()
                .auth()
                .oauth2("21090cjw")
                .param("page", 1)
                .param("amount", 10)
        .when()
                .get(baseUrl + "/language/{languageId}/course", 1L)
        .then()
                .statusCode(UNAUTHORIZED.value())
                .body("error", equalTo(UNAUTHORIZED.value()))
                .body("errors", nullValue());
    }

    @Test
    void shouldRespondWith403ToGetCoursesIfUserIsNotModerator() {
        given()
                .auth()
                .oauth2(tokenFor("user1"))
                .param("page", 1)
                .param("amount", 10)
        .when()
                .get(baseUrl + "/language/{languageId}/course", 1L)
        .then()
                .statusCode(FORBIDDEN.value())
                .body("error", equalTo(FORBIDDEN.value()))
                .body("errors", nullValue());
    }

    @Test
    void shouldRespondWithEmptyListToGetCoursesIfLanguageDoesNotExist() {
        given()
                .auth()
                .oauth2(tokenFor("user1", List.of(new SimpleGrantedAuthority("MODERATOR"))))
                .param("page", 1)
                .param("amount", 10)
        .when()
                .get(baseUrl + "/language/{languageId}/course", 1L)
        .then()
                .statusCode(OK.value())
                .body("$", hasSize(0));
    }

    @ParameterizedTest
    @ValueSource(ints = {-10, -1, 0})
    void shouldNotAcceptGetCoursesRequestWithNonPositivePages(int page) {
        given()
                .auth()
                .oauth2(tokenFor("user1", List.of(new SimpleGrantedAuthority("MODERATOR"))))
                .param("page", page)
                .param("amount", 10)
        .when()
                .get(baseUrl + "/language/{languageId}/course", 1L)
        .then()
                .statusCode(BAD_REQUEST.value())
                .body("error", equalTo(BAD_REQUEST.value()))
                .body("message", containsStringIgnoringCase("validation failed"))
                .body("errors", hasItem(both(containsStringIgnoringCase("page")).and(containsStringIgnoringCase("positive"))));
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0, 21, 50})
    void shouldNotAcceptGetCoursesRequestWithAmountOutsideBounds(int amount) {
        given()
                .auth()
                .oauth2(tokenFor("user1", List.of(new SimpleGrantedAuthority("MODERATOR"))))
                .param("page", 1)
                .param("amount", amount)
        .when()
                .get(baseUrl + "/language/{languageId}/course", 1L)
        .then()
                .statusCode(BAD_REQUEST.value())
                .body("error", equalTo(BAD_REQUEST.value()))
                .body("message", containsStringIgnoringCase("validation failed"))
                .body("errors", hasItem(both(containsStringIgnoringCase("amount")).and(containsStringIgnoringCase("between"))));
    }

    @Test
    void shouldRespondWithListOfCourses() {
        Long languageId = addLanguage("lang");
        addCourse("course1", languageId);
        addCourse("course2", languageId);
        addCourse("course3", languageId);
        given()
                .auth()
                .oauth2(tokenFor("user1", List.of(new SimpleGrantedAuthority("MODERATOR"))))
                .param("page", 1)
                .param("amount", 2)
        .when()
                .get(baseUrl + "/language/{languageId}/course", languageId)
        .then()
                .statusCode(OK.value())
                .body("$", hasSize(2));
    }

    private void addCourse(String name, Long languageId) {
        Course course = new Course();
        course.setName(name);
        course.setLanguage(languageRepository.getReferenceById(languageId));
        courseRepository.save(course);
    }
}