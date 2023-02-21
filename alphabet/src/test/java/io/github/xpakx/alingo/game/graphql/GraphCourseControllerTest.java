package io.github.xpakx.alingo.game.graphql;

import io.github.xpakx.alingo.game.*;
import io.github.xpakx.alingo.security.JwtUtils;
import io.github.xpakx.alingo.utils.GraphCourse;
import io.github.xpakx.alingo.utils.GraphQuery;
import io.github.xpakx.alingo.utils.GraphUpdateCourse;
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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GraphCourseControllerTest {
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
        GraphQuery query = getNewCourseGraphQuery(getNewCourseVariables("course", "description", "EASY", null));
        given()
                .contentType(ContentType.JSON)
                .body(query)
        .when()
                .post(baseUrl + "/graphql")
        .then()
                .statusCode(UNAUTHORIZED.value())
                .body("error", equalTo(UNAUTHORIZED.value()))
                .body("errors", nullValue());
    }

    private GraphQuery getNewCourseGraphQuery(GraphCourse answer) {
        GraphQuery query = new GraphQuery();
        query.setQuery("""
                    mutation addCourse($name: String, $description: String, $difficulty: Difficulty, $id: Int){
                        addCourse(name: $name, description: $description, difficulty: $difficulty, languageId: $id)
                        {
                            id
                            name
                            description
                            difficulty
                        }
                    }""");
        query.setVariables(answer);
        return query;
    }

    private GraphCourse getNewCourseVariables(String name, String description, String difficulty, Long languageId) {
        return new GraphCourse(name, description, difficulty, languageId);
    }

    @Test
    void shouldRespondWith401ToAddCourseIfTokenIsWrong() {
        GraphQuery query = getNewCourseGraphQuery(getNewCourseVariables("course", "description", "EASY", null));
        given()
                .auth()
                .oauth2("204230990324")
                .contentType(ContentType.JSON)
                .body(query)
        .when()
                .post(baseUrl + "/graphql")
        .then()
                .statusCode(UNAUTHORIZED.value());
    }

    @Test
    void shouldRespondWith403ToAddCourseIfUserIsNotModerator() {
        GraphQuery query = getNewCourseGraphQuery(getNewCourseVariables("course", "description", "EASY", null));
        given()
                .auth()
                .oauth2(tokenFor())
                .contentType(ContentType.JSON)
                .body(query)
        .when()
                .post(baseUrl + "/graphql")
        .then()
                .statusCode(OK.value())
                .body("data", nullValue())
                .body("errors", not(nullValue()));
    }

    private String tokenFor() {
        return tokenFor(new ArrayList<>());
    }

    private String tokenFor(List<GrantedAuthority> authorities) {
        return jwt.generateToken(new User("user1", "", authorities));
    }

    @Test
    void shouldAddCourse() {
        GraphQuery query = getNewCourseGraphQuery(getNewCourseVariables("newCourse", "description", "EASY", null));
        given()
                .auth()
                .oauth2(tokenFor(List.of(new SimpleGrantedAuthority("MODERATOR"))))
                .contentType(ContentType.JSON)
                .body(query)
        .when()
                .post(baseUrl + "/graphql")
        .then()
                .statusCode(OK.value())
                .body("data.addCourse.name", equalTo("newCourse"));
    }

    @Test
    void shouldAddNewCourseToDb() {
        GraphQuery query = getNewCourseGraphQuery(getNewCourseVariables("newCourse", "description", "EASY", null));
        given()
                .auth()
                .oauth2(tokenFor(List.of(new SimpleGrantedAuthority("MODERATOR"))))
                .contentType(ContentType.JSON)
                .body(query)
        .when()
                .post(baseUrl + "/graphql");
        List<Course> courses = courseRepository.findAll();
        assertThat(courses, hasItem(hasProperty("name", equalTo("newCourse"))));
    }

    @Test
    void shouldNotAcceptEmptyCourseName() {
        GraphQuery query = getNewCourseGraphQuery(getNewCourseVariables("", "description", "EASY", null));
        given()
                .auth()
                .oauth2(tokenFor(List.of(new SimpleGrantedAuthority("MODERATOR"))))
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
    void shouldNotAcceptNullCourseName() {
        GraphQuery query = getNewCourseGraphQuery(getNewCourseVariables(null, "description", "EASY", null));
        given()
                .auth()
                .oauth2(tokenFor(List.of(new SimpleGrantedAuthority("MODERATOR"))))
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
    void shouldRespondWith401ToUpdateCourseIfNotAuthenticated() {
        GraphQuery query = getUpdateCourseGraphQuery(getUpdateCourseVariables(1L, "course", "description", "EASY", null));
        given()
                .contentType(ContentType.JSON)
                .body(query)
        .when()
                .post(baseUrl + "/graphql")
        .then()
                .statusCode(UNAUTHORIZED.value())
                .body("error", equalTo(UNAUTHORIZED.value()))
                .body("errors", nullValue());
    }

    private GraphQuery getUpdateCourseGraphQuery(GraphUpdateCourse answer) {
        GraphQuery query = new GraphQuery();
        query.setQuery("""
                    mutation editCourse($name: String, $description: String, $difficulty: Difficulty, $id: Int, $cid: ID){
                        editCourse(courseId: $cid, name: $name, description: $description, difficulty: $difficulty, languageId: $id)
                        {
                            id
                            name
                            description
                            difficulty
                        }
                    }""");
        query.setVariables(answer);
        return query;
    }

    private GraphUpdateCourse getUpdateCourseVariables(Long courseId, String name, String description, String difficulty, Long languageId) {
        return new GraphUpdateCourse(courseId, name, description, difficulty, languageId);
    }

    @Test
    void shouldRespondWith401ToUpdateCourseIfTokenIsWrong() {
        GraphQuery query = getUpdateCourseGraphQuery(getUpdateCourseVariables(1L, "course", "description", "EASY", null));
        given()
                .auth()
                .oauth2("204230990324")
                .contentType(ContentType.JSON)
                .body(query)
        .when()
                .post(baseUrl + "/graphql")
        .then()
                .statusCode(UNAUTHORIZED.value());
    }

    @Test
    void shouldRespondWith403ToUpdateCourseIfUserIsNotModerator() {
        GraphQuery query = getUpdateCourseGraphQuery(getUpdateCourseVariables(1L, "course", "description", "EASY", null));
        given()
                .auth()
                .oauth2(tokenFor())
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
    void shouldRespondWith404ToUpdateCourseIfCourseDoesNotExist() {
        GraphQuery query = getUpdateCourseGraphQuery(getUpdateCourseVariables(1L, "course", "description", "EASY", null));
        given()
                .auth()
                .oauth2(tokenFor(List.of(new SimpleGrantedAuthority("MODERATOR"))))
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
    void shouldUpdateCourse() {
        Long courseId = addCourse("course1");
        GraphQuery query = getUpdateCourseGraphQuery(getUpdateCourseVariables(courseId, "newCourseName", "description", "EASY", null));
        given()
                .auth()
                .oauth2(tokenFor(List.of(new SimpleGrantedAuthority("MODERATOR"))))
                .contentType(ContentType.JSON)
                .body(query)
        .when()
                .post(baseUrl + "/graphql")
        .then()
                .statusCode(OK.value())
                .body("data.editCourse.name", equalTo("newCourseName"));
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
        GraphQuery query = getUpdateCourseGraphQuery(getUpdateCourseVariables(courseId, "newCourseName", "description", "EASY", null));
        given()
                .auth()
                .oauth2(tokenFor(List.of(new SimpleGrantedAuthority("MODERATOR"))))
                .contentType(ContentType.JSON)
                .body(query)
        .when()
                .post(baseUrl + "/graphql");
        Optional<Course> language = courseRepository.findById(courseId);
        assertTrue(language.isPresent());
        assertThat(language.get(), hasProperty("name", equalTo("newCourseName")));
    }

    @Test
    void shouldNotAcceptEmptyCourseNameWhileUpdating() {
        Long courseId = addCourse("course");
        GraphQuery query = getUpdateCourseGraphQuery(getUpdateCourseVariables(courseId, "", "description", "EASY", null));
        given()
                .auth()
                .oauth2(tokenFor(List.of(new SimpleGrantedAuthority("MODERATOR"))))
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
    void shouldNotAcceptNullCourseNameWhileUpdating() {
        Long courseId = addCourse("course");
        GraphQuery query = getUpdateCourseGraphQuery(getUpdateCourseVariables(courseId, null, "description", "EASY", null));
        given()
                .auth()
                .oauth2(tokenFor(List.of(new SimpleGrantedAuthority("MODERATOR"))))
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
    void shouldUpdateCourseWithLanguageField() {
        Long courseId = addCourse("course");
        Long languageId = addLanguage("lang");
        GraphQuery query = getUpdateCourseGraphQuery(getUpdateCourseVariables(courseId, "newName", "description", "EASY", languageId));
        given()
                .auth()
                .oauth2(tokenFor(List.of(new SimpleGrantedAuthority("MODERATOR"))))
                .contentType(ContentType.JSON)
                .body(query)
        .when()
                .post(baseUrl + "/graphql");
        Optional<Course> language = courseRepository.findById(courseId);
        assertTrue(language.isPresent());
        assertThat(language.get(), hasProperty("name", equalTo("newName")));
        assertThat(language.get(), hasProperty("description", equalTo("description")));
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
        GraphQuery query = getNewCourseGraphQuery(getNewCourseVariables("course", "description", "EASY", languageId));
        Long courseId = given()
                .auth()
                .oauth2(tokenFor(List.of(new SimpleGrantedAuthority("MODERATOR"))))
                .contentType(ContentType.JSON)
                .body(query)
        .when()
                .post(baseUrl + "/graphql")
        .then()
                .extract()
                .jsonPath()
                .getLong("data.addCourse.id");
        Optional<Course> language = courseRepository.findById(courseId);
        assertTrue(language.isPresent());
        assertThat(language.get(), hasProperty("name", equalTo("course")));
        assertThat(language.get(), hasProperty("description", equalTo("description")));
        assertThat(language.get(), hasProperty("difficulty", equalTo(Difficulty.EASY)));
        assertThat(language.get(), hasProperty("language", hasProperty("id", equalTo(languageId))));
    }
}