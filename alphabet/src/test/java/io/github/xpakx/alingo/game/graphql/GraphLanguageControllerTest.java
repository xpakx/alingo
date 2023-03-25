package io.github.xpakx.alingo.game.graphql;

import io.github.xpakx.alingo.game.Course;
import io.github.xpakx.alingo.game.CourseRepository;
import io.github.xpakx.alingo.game.Language;
import io.github.xpakx.alingo.game.LanguageRepository;
import io.github.xpakx.alingo.security.JwtUtils;
import io.github.xpakx.alingo.utils.*;
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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpStatus.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GraphLanguageControllerTest {
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
        GraphQuery query = getNewLanguageGraphQuery(getNewLanguageVariables("lang"));
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

    private GraphQuery getNewLanguageGraphQuery(GraphLanguage answer) {
        GraphQuery query = new GraphQuery();
        query.setQuery("""
                    mutation addLanguage($name: String){
                        addLanguage(name: $name)
                        {
                            id
                            name
                        }
                    }""");
        query.setVariables(answer);
        return query;
    }

    private GraphLanguage getNewLanguageVariables(String name) {
        return new GraphLanguage(name);
    }

    private GraphQuery getUpdateGraphQuery(GraphUpdateLanguage answer) {
        GraphQuery query = new GraphQuery();
        query.setQuery("""
                    mutation editLanguage($id: ID, $name: String){
                        editLanguage(languageId: $id, name: $name)
                        {
                            id
                            name
                        }
                    }""");
        query.setVariables(answer);
        return query;
    }

    private GraphUpdateLanguage getUpdateVariables(String name, Long languageId) {
        return new GraphUpdateLanguage(languageId, name);
    }

    @Test
    void shouldRespondWith401ToAddLanguageIfTokenIsWrong() {
        GraphQuery query = getNewLanguageGraphQuery(getNewLanguageVariables("lang"));
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
    void shouldRespondWith403ToAddLanguageIfUserIsNotModerator() {
        GraphQuery query = getNewLanguageGraphQuery(getNewLanguageVariables("lang"));
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
                .body("errors", not(nullValue()))
                .body("errors.message", hasItem(containsStringIgnoringCase("access denied")));
    }

    private String tokenFor() {
        return tokenFor(new ArrayList<>());
    }

    private String tokenFor(List<GrantedAuthority> authorities) {
        return jwt.generateToken(new User("user1", "", authorities));
    }

    @Test
    void shouldAddLanguage() {
        GraphQuery query = getNewLanguageGraphQuery(getNewLanguageVariables("lang"));
        given()
                .auth()
                .oauth2(tokenFor(List.of(new SimpleGrantedAuthority("MODERATOR"))))
                .contentType(ContentType.JSON)
                .body(query)
        .when()
                .post(baseUrl + "/graphql")
        .then()
                .statusCode(OK.value())
                .body("data.addLanguage.name", equalTo("lang"));
    }

    @Test
    void shouldAddNewLanguageToDb() {
        GraphQuery query = getNewLanguageGraphQuery(getNewLanguageVariables("lang"));
        given()
                .auth()
                .oauth2(tokenFor(List.of(new SimpleGrantedAuthority("MODERATOR"))))
                .contentType(ContentType.JSON)
                .body(query)
       .when()
                .post(baseUrl + "/graphql");
        List<Language> languages = languageRepository.findAll();
        assertThat(languages, hasItem(hasProperty("name", equalTo("lang"))));
    }

    @Test
    void shouldNotAcceptEmptyLanguageName() {
        GraphQuery query = getNewLanguageGraphQuery(getNewLanguageVariables(""));
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
                .body("errors", not(nullValue()))
                .body("errors.message", hasItem(both(containsStringIgnoringCase("name")).and(containsStringIgnoringCase("empty"))));
    }

    @Test
    void shouldNotAcceptNullLanguageName() {
        GraphQuery query = getNewLanguageGraphQuery(getNewLanguageVariables(null));
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
                .body("errors", not(nullValue()))
                .body("errors.message", hasItem(both(containsStringIgnoringCase("name")).and(containsStringIgnoringCase("empty"))));
    }

    @Test
    void shouldRespondWith401ToUpdateLanguageIfNotAuthenticated() {
        GraphQuery query = getUpdateGraphQuery(getUpdateVariables("lang", 1L));
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

    @Test
    void shouldRespondWith401ToUpdateLanguageIfTokenIsWrong() {
        GraphQuery query = getUpdateGraphQuery(getUpdateVariables("lang", 1L));
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
    void shouldRespondWith403ToUpdateLanguageIfUserIsNotModerator() {
        GraphQuery query = getUpdateGraphQuery(getUpdateVariables("lang", 1L));
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
                .body("errors", not(nullValue()))
                .body("errors.message", hasItem(containsStringIgnoringCase("access denied")));
    }

    @Test
    void shouldRespondWith404ToUpdateLanguageIfLanguageDoesNotExist() {
        GraphQuery query = getUpdateGraphQuery(getUpdateVariables("lang", 1L));
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
                .body("errors", not(nullValue()))
                .body("errors.message", hasItem(containsStringIgnoringCase("not found")));
    }

    @Test
    void shouldUpdateLanguage() {
        GraphQuery query = getUpdateGraphQuery(getUpdateVariables("newLanguage", addLanguage("language")));
        given()
                .auth()
                .oauth2(tokenFor(List.of(new SimpleGrantedAuthority("MODERATOR"))))
                .contentType(ContentType.JSON)
                .body(query)
        .when()
                .post(baseUrl + "/graphql")
        .then()
                .statusCode(OK.value())
                .body("data.editLanguage.name", equalTo("newLanguage"));
    }

    private Long addLanguage(String name) {
        Language language = new Language();
        language.setName(name);
        return languageRepository.save(language).getId();
    }

    @Test
    void shouldUpdateLanguageIndDb() {
        Long languageId = addLanguage("language");
        GraphQuery query = getUpdateGraphQuery(getUpdateVariables("newLanguage", languageId));
        given()
                .auth()
                .oauth2(tokenFor(List.of(new SimpleGrantedAuthority("MODERATOR"))))
                .contentType(ContentType.JSON)
                .body(query)
        .when()
                .post(baseUrl + "/graphql");
        Optional<Language> language = languageRepository.findById(languageId);
        assertTrue(language.isPresent());
        assertThat(language.get(), hasProperty("name", equalTo("newLanguage")));
    }

    @Test
    void shouldNotAcceptEmptyLanguageNameWhileUpdating() {
        Long languageId = addLanguage("language1");
        GraphQuery query = getUpdateGraphQuery(getUpdateVariables("", languageId));
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
                .body("errors", not(nullValue()))
                .body("errors.message", hasItem(both(containsStringIgnoringCase("name")).and(containsStringIgnoringCase("empty"))));
    }

    @Test
    void shouldNotAcceptNullLanguageNameWhileUpdating() {
        Long languageId = addLanguage("language1");
        GraphQuery query = getUpdateGraphQuery(getUpdateVariables(null, languageId));
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
                .body("errors", not(nullValue()))
                .body("errors.message", hasItem(both(containsStringIgnoringCase("name")).and(containsStringIgnoringCase("empty"))));
    }

    private GraphQuery getGetLanguageGraphQuery(GetByIdVariables answer) {
        GraphQuery query = new GraphQuery();
        query.setQuery("""
                    query getLanguage($id: ID){
                        getLanguage(id: $id)
                        {
                            id
                            name
                        }
                    }""");
        query.setVariables(answer);
        return query;
    }

    private GetByIdVariables getIdVariables(Long id) {
        return new GetByIdVariables(id);
    }

    @Test
    void shouldRespondWith401ToGetLanguageIfNotAuthenticated() {
        GraphQuery query = getGetLanguageGraphQuery(getIdVariables(1L));
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

    @Test
    void shouldRespondWith401ToGetLanguageIfTokenIsWrong() {
        GraphQuery query = getGetLanguageGraphQuery(getIdVariables(1L));
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
    void shouldRespondWith403ToGetLanguageIfUserIsNotModerator() {
        GraphQuery query = getGetLanguageGraphQuery(getIdVariables(1L));
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
                .body("errors", not(nullValue()))
                .body("errors.message", hasItem(containsStringIgnoringCase("access denied")));
    }

    @Test
    void shouldRespondWith404ToGetLanguageIfLanguageDoesNotExist() {
        GraphQuery query = getGetLanguageGraphQuery(getIdVariables(1L));
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
                .body("errors", not(nullValue()))
                .body("errors.message", hasItem(containsStringIgnoringCase("not found")));
    }

    @Test
    void shouldReturnLanguage() {
        GraphQuery query = getGetLanguageGraphQuery(getIdVariables(addLanguage("language")));
        given()
                .auth()
                .oauth2(tokenFor(List.of(new SimpleGrantedAuthority("MODERATOR"))))
                .contentType(ContentType.JSON)
                .body(query)
        .when()
                .post(baseUrl + "/graphql")
        .then()
                .statusCode(OK.value())
                .body("data.getLanguage.name", equalTo("language"));
    }

    @Test
    void shouldRespondWith401ToGetLanguagesIfNotAuthenticated() {
        GraphQuery query = getLanguagesGraphQuery(getPageVariables(1, 20));
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

    private GraphQuery getLanguagesGraphQuery(PageVariables variables) {
        GraphQuery query = new GraphQuery();
        query.setQuery("""
                    query getLanguages($page: Int, $amount: Int){
                        getLanguages(page: $page, amount: $amount)
                        {
                            id
                            name
                        }
                    }""");
        query.setVariables(variables);
        return query;
    }

    private PageVariables getPageVariables(Integer page, Integer amount) {
        return new PageVariables(page, amount);
    }

    @Test
    void shouldRespondWith401ToGetLanguagesIfTokenIsWrong() {
        GraphQuery query = getLanguagesGraphQuery(getPageVariables(1, 20));
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
    void shouldRespondWithEmptyListToGetLanguagesIfThereAreNoLanguages() {
        GraphQuery query = getLanguagesGraphQuery(getPageVariables(1, 20));
        given()
                .auth()
                .oauth2(tokenFor())
                .contentType(ContentType.JSON)
                .body(query)
        .when()
                .post(baseUrl + "/graphql")
        .then()
                .statusCode(OK.value())
                .body("data.getLanguages", hasSize(0));
    }

    @ParameterizedTest
    @ValueSource(ints = {-10, -1, 0})
    void shouldNotAcceptGetLanguagesRequestWithNonPositivePages(int page) {
        GraphQuery query = getLanguagesGraphQuery(getPageVariables(page, 20));
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
                .body("errors", not(nullValue()))
                .body("errors.message", hasItem(both(containsStringIgnoringCase("page")).and(containsStringIgnoringCase("positive"))));
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0, 21, 50})
    void shouldNotAcceptGetLanguagesRequestWithAmountOutsideBounds(int amount) {
        GraphQuery query = getLanguagesGraphQuery(getPageVariables(1, amount));
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
                .body("errors", not(nullValue()))
                .body("errors.message", hasItem(both(containsStringIgnoringCase("amount")).and(containsStringIgnoringCase("between"))));
    }

    @Test
    void shouldRespondWithListOfLanguages() {
        addLanguage("lang1");
        addLanguage("lang2");
        addLanguage("lang3");
        GraphQuery query = getLanguagesGraphQuery(getPageVariables(1, 2));
        given()
                .auth()
                .oauth2(tokenFor(List.of(new SimpleGrantedAuthority("MODERATOR"))))
                .contentType(ContentType.JSON)
                .body(query)
        .when()
                .post(baseUrl + "/graphql")
        .then()
                .log().body()
                .statusCode(OK.value())
                .body("data.getLanguages", hasSize(2));
    }

    @Test
    void shouldRespondWith401ToGetCoursesIfNotAuthenticated() {
        GraphQuery query = getCoursesGraphQuery(getPageAndIdVariables(1L, 1, 20));
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

    private GraphQuery getCoursesGraphQuery(PageWithIdVariables variables) {
        GraphQuery query = new GraphQuery();
        query.setQuery("""
                    query getCoursesForLanguage($id: ID, $page: Int, $amount: Int){
                        getCoursesForLanguage(languageId: $id, page: $page, amount: $amount)
                        {
                            courses {
                                id
                                name
                            }
                        }
                    }""");
        query.setVariables(variables);
        return query;
    }

    private PageWithIdVariables getPageAndIdVariables(Long id, Integer page, Integer amount) {
        return new PageWithIdVariables(id, page, amount);
    }

    @Test
    void shouldRespondWith401ToGetCoursesIfTokenIsWrong() {
        GraphQuery query = getCoursesGraphQuery(getPageAndIdVariables(1L, 1, 20));
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
    void shouldRespondWithEmptyListToGetCoursesIfLanguageNotFound() {
        GraphQuery query = getCoursesGraphQuery(getPageAndIdVariables(1L, 1, 20));
        given()
                .auth()
                .oauth2(tokenFor())
                .contentType(ContentType.JSON)
                .body(query)
        .when()
                .post(baseUrl + "/graphql")
        .then()
                .statusCode(OK.value())
                .body("data.getCoursesForLanguage.courses", hasSize(0));
    }

    @ParameterizedTest
    @ValueSource(ints = {-10, -1, 0})
    void shouldNotAcceptGetCoursesRequestWithNonPositivePages(int page) {
        GraphQuery query = getCoursesGraphQuery(getPageAndIdVariables(1L, page, 20));
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
                .body("errors", not(nullValue()))
                .body("errors.message", hasItem(both(containsStringIgnoringCase("page")).and(containsStringIgnoringCase("positive"))));
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0, 21, 50})
    void shouldNotAcceptGetCoursesRequestWithAmountOutsideBounds(int amount) {
        GraphQuery query = getCoursesGraphQuery(getPageAndIdVariables(1L, 1, amount));
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
                .body("errors", not(nullValue()))
                .body("errors.message", hasItem(both(containsStringIgnoringCase("amount")).and(containsStringIgnoringCase("between"))));
    }

    @Test
    void shouldRespondWithListOfCourses() {
        Long languageId = addLanguage("lang");
        addCourse("course1", languageId);
        addCourse("course2", languageId);
        addCourse("course3", languageId);
        GraphQuery query = getCoursesGraphQuery(getPageAndIdVariables(languageId, 1, 2));
        given()
                .auth()
                .oauth2(tokenFor(List.of(new SimpleGrantedAuthority("MODERATOR"))))
                .contentType(ContentType.JSON)
                .body(query)
        .when()
                .post(baseUrl + "/graphql")
        .then()
                .statusCode(OK.value())
                .body("data.getCoursesForLanguage.courses", hasSize(2));
    }

    private void addCourse(String name, Long languageId) {
        Course course = new Course();
        course.setName(name);
        course.setLanguage(languageRepository.getReferenceById(languageId));
        courseRepository.save(course);
    }
}