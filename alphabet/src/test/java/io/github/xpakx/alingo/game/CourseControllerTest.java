package io.github.xpakx.alingo.game;

import io.github.xpakx.alingo.game.dto.CourseRequest;
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
import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CourseControllerTest {
    @LocalServerPort
    private int port;
    private String baseUrl;

    @Autowired
    JwtUtils jwt;
    @Autowired
    CourseRepository courseRepository;
    @Autowired
    LanguageRepository languageRepository;

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
    void shouldRespondWith401ToAddCourseIfNotAuthenticated() {
        when()
                .post(baseUrl + "/course")
        .then()
                .statusCode(UNAUTHORIZED.value())
                .body("error", equalTo(UNAUTHORIZED.value()))
                .body("errors", nullValue());
    }

    @Test
    void shouldRespondWith401ToAddCourseIfTokenIsWrong() {
        given()
                .auth()
                .oauth2("329432853295")
                .contentType(ContentType.JSON)
                .body(getCourseRequest("language1"))
        .when()
                .post(baseUrl + "/course")
        .then()
                .statusCode(UNAUTHORIZED.value())
                .body("error", equalTo(UNAUTHORIZED.value()))
                .body("errors", nullValue());
    }

    private CourseRequest getCourseRequest(String name) {
        return getCourseRequest(name, "", null, null);
    }

    private CourseRequest getCourseRequest(String name, String description, Difficulty difficulty, Long languageId) {
        return new CourseRequest(name, description, difficulty, languageId);
    }

    @Test
    void shouldRespondWith403ToAddCourseIfUserIsNotModerator() {
        given()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(getCourseRequest("course1"))
        .when()
                .post(baseUrl + "/course")
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
    void shouldAddCourse() {
        given()
                .auth()
                .oauth2(tokenFor("user1", List.of(new SimpleGrantedAuthority("MODERATOR"))))
                .contentType(ContentType.JSON)
                .body(getCourseRequest("newCourse"))
        .when()
                .post(baseUrl + "/course")
        .then()
                .statusCode(OK.value())
                .body("name", equalTo("newCourse"));
    }

    @Test
    void shouldAddNewCourseToDb() {
        given()
                .auth()
                .oauth2(tokenFor("user1", List.of(new SimpleGrantedAuthority("MODERATOR"))))
                .contentType(ContentType.JSON)
                .body(getCourseRequest("newCourse"))
        .when()
                .post(baseUrl + "/course");
        List<Course> languages = courseRepository.findAll();
        assertThat(languages, hasItem(hasProperty("name", equalTo("newCourse"))));
    }

    @Test
    void shouldNotAcceptEmptyCourseName() {
        given()
                .auth()
                .oauth2(tokenFor("user1", List.of(new SimpleGrantedAuthority("MODERATOR"))))
                .contentType(ContentType.JSON)
                .body(getCourseRequest(""))
        .when()
                .post(baseUrl + "/course")
        .then()
                .statusCode(BAD_REQUEST.value())
                .body("error", equalTo(BAD_REQUEST.value()))
                .body("message", containsStringIgnoringCase("Validation failed"))
                .body("errors", hasItem(both(containsStringIgnoringCase("name")).and(containsStringIgnoringCase("empty"))));
    }

    @Test
    void shouldNotAcceptNullCourseName() {
        given()
                .auth()
                .oauth2(tokenFor("user1", List.of(new SimpleGrantedAuthority("MODERATOR"))))
                .contentType(ContentType.JSON)
                .body(getCourseRequest(null))
        .when()
                .post(baseUrl + "/course")
        .then()
                .statusCode(BAD_REQUEST.value())
                .body("error", equalTo(BAD_REQUEST.value()))
                .body("message", containsStringIgnoringCase("Validation failed"))
                .body("errors", hasItem(both(containsStringIgnoringCase("name")).and(containsStringIgnoringCase("empty"))));
    }

    @Test
    void shouldRespondWith401ToUpdateCourseIfNotAuthenticated() {
        when()
                .put(baseUrl + "/course/{courseId}", 1L)
        .then()
                .statusCode(UNAUTHORIZED.value())
                .body("error", equalTo(UNAUTHORIZED.value()))
                .body("errors", nullValue());
    }

    @Test
    void shouldRespondWith401ToUpdateCourseIfTokenIsWrong() {
        given()
                .auth()
                .oauth2("329432853295")
                .contentType(ContentType.JSON)
                .body(getCourseRequest("course"))
        .when()
                .put(baseUrl + "/course/{courseId}", 1L)
        .then()
                .statusCode(UNAUTHORIZED.value())
                .body("error", equalTo(UNAUTHORIZED.value()))
                .body("errors", nullValue());
    }

    @Test
    void shouldRespondWith403ToUpdateCourseIfUserIsNotModerator() {
        given()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(getCourseRequest("course"))
        .when()
                .put(baseUrl + "/course/{courseId}", 1L)
        .then()
                .statusCode(FORBIDDEN.value())
                .body("error", equalTo(FORBIDDEN.value()))
                .body("errors", nullValue());
    }

    @Test
    void shouldRespondWith404ToUpdateCourseIfCourseDoesNotExist() {
        given()
                .auth()
                .oauth2(tokenFor("user1", List.of(new SimpleGrantedAuthority("MODERATOR"))))
                .contentType(ContentType.JSON)
                .body(getCourseRequest("course"))
        .when()
                .put(baseUrl + "/course/{courseId}", 1L)
        .then()
                .statusCode(NOT_FOUND.value())
                .body("error", equalTo(NOT_FOUND.value()))
                .body("errors", nullValue());
    }

    @Test
    void shouldUpdateCourse() {
        Long courseId = addCourse("course1");
        given()
                .auth()
                .oauth2(tokenFor("user1", List.of(new SimpleGrantedAuthority("MODERATOR"))))
                .contentType(ContentType.JSON)
                .body(getCourseRequest("newName"))
        .when()
                .put(baseUrl + "/course/{courseId}", courseId)
        .then()
                .statusCode(OK.value())
                .body("name", equalTo("newName"));
    }

    private Long addCourse(String name) {
        return addCourse(name, null);
    }

    private Long addCourse(String name, Long languageId) {
        Course course = new Course();
        course.setName(name);
        course.setLanguage(languageId != null ? languageRepository.getReferenceById(languageId) : null);
        return courseRepository.save(course).getId();
    }

    @Test
    void shouldUpdateCourseIndDb() {
        Long courseId = addCourse("course");
        given()
                .auth()
                .oauth2(tokenFor("user1", List.of(new SimpleGrantedAuthority("MODERATOR"))))
                .contentType(ContentType.JSON)
                .body(getCourseRequest("newName"))
        .when()
                .put(baseUrl + "/course/{courseId}", courseId);
        Optional<Course> language = courseRepository.findById(courseId);
        assertTrue(language.isPresent());
        assertThat(language.get(), hasProperty("name", equalTo("newName")));
    }

    @Test
    void shouldNotAcceptEmptyCourseNameWhileUpdating() {
        Long courseId = addCourse("course");
        given()
                .auth()
                .oauth2(tokenFor("user1", List.of(new SimpleGrantedAuthority("MODERATOR"))))
                .contentType(ContentType.JSON)
                .body(getCourseRequest(""))
        .when()
                .put(baseUrl + "/course/{courseId}", courseId)
        .then()
                .statusCode(BAD_REQUEST.value())
                .body("error", equalTo(BAD_REQUEST.value()))
                .body("message", containsStringIgnoringCase("Validation failed"))
                .body("errors", hasItem(both(containsStringIgnoringCase("name")).and(containsStringIgnoringCase("empty"))));
    }

    @Test
    void shouldNotAcceptNullCourseNameWhileUpdating() {
        Long courseId = addCourse("course");
        given()
                .auth()
                .oauth2(tokenFor("user1", List.of(new SimpleGrantedAuthority("MODERATOR"))))
                .contentType(ContentType.JSON)
                .body(getCourseRequest(null))
        .when()
                .put(baseUrl + "/course/{courseId}", courseId)
        .then()
                .statusCode(BAD_REQUEST.value())
                .body("error", equalTo(BAD_REQUEST.value()))
                .body("message", containsStringIgnoringCase("Validation failed"))
                .body("errors", hasItem(both(containsStringIgnoringCase("name")).and(containsStringIgnoringCase("empty"))));
    }

    @Test
    void shouldUpdateCourseWithLanguageField() {
        Long courseId = addCourse("course");
        Long languageId = addLanguage("lang");
        given()
                .auth()
                .oauth2(tokenFor("user1", List.of(new SimpleGrantedAuthority("MODERATOR"))))
                .contentType(ContentType.JSON)
                .body(getCourseRequest("newName", "desc", Difficulty.EASY, languageId))
        .when()
                .put(baseUrl + "/course/{courseId}", courseId);
        Optional<Course> language = courseRepository.findById(courseId);
        assertTrue(language.isPresent());
        assertThat(language.get(), hasProperty("name", equalTo("newName")));
        assertThat(language.get(), hasProperty("description", equalTo("desc")));
        assertThat(language.get(), hasProperty("difficulty", equalTo(Difficulty.EASY)));
        assertThat(language.get(), hasProperty("language", hasProperty("id", equalTo(languageId))));
    }

    private Long addLanguage(String name) {
        Language language = new Language();
        language.setName(name);
        return languageRepository.save(language).getId();
    }

    @Test
    void shouldAddCourseWithLanguageField() {
        Long languageId = addLanguage("lang");
        Long courseId = given()
                .auth()
                .oauth2(tokenFor("user1", List.of(new SimpleGrantedAuthority("MODERATOR"))))
                .contentType(ContentType.JSON)
                .body(getCourseRequest("course", "desc", Difficulty.EASY, languageId))
        .when()
                .post(baseUrl + "/course")
        .then()
                .extract()
                .jsonPath()
                .getLong("id");
        Optional<Course> language = courseRepository.findById(courseId);
        assertTrue(language.isPresent());
        assertThat(language.get(), hasProperty("name", equalTo("course")));
        assertThat(language.get(), hasProperty("description", equalTo("desc")));
        assertThat(language.get(), hasProperty("difficulty", equalTo(Difficulty.EASY)));
        assertThat(language.get(), hasProperty("language", hasProperty("id", equalTo(languageId))));
    }
}