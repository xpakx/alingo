package io.github.xpakx.alingo.game.graphql;

import io.github.xpakx.alingo.game.Course;
import io.github.xpakx.alingo.game.CourseRepository;
import io.github.xpakx.alingo.game.Difficulty;
import io.github.xpakx.alingo.game.LanguageRepository;
import io.github.xpakx.alingo.game.dto.CourseRequest;
import io.github.xpakx.alingo.security.JwtUtils;
import io.github.xpakx.alingo.utils.GraphCourse;
import io.github.xpakx.alingo.utils.GraphQuery;
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

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
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
}