package io.github.xpakx.alingo.game.graphql;

import io.github.xpakx.alingo.game.Language;
import io.github.xpakx.alingo.game.LanguageRepository;
import io.github.xpakx.alingo.game.dto.LanguageRequest;
import io.github.xpakx.alingo.security.JwtUtils;
import io.github.xpakx.alingo.utils.GraphLanguage;
import io.github.xpakx.alingo.utils.GraphQuery;
import io.github.xpakx.alingo.utils.GraphUpdateLanguage;
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

    private LanguageRequest getLanguageRequest(String name) {
        LanguageRequest request = new LanguageRequest(name);
        return request;
    }

    @Test
    void shouldRespondWith403ToAddLanguageIfUserIsNotModerator() {
        GraphQuery query = getNewLanguageGraphQuery(getNewLanguageVariables("lang"));
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
                .body("errors", not(nullValue()));
    }

    private String tokenFor(String username) {
        return tokenFor(username, new ArrayList<>());
    }

    private String tokenFor(String username, List<GrantedAuthority> authorities) {
        return jwt.generateToken(new User(username, "", authorities));
    }

    @Test
    void shouldAddLanguage() {
        GraphQuery query = getNewLanguageGraphQuery(getNewLanguageVariables("lang"));
        given()
                .auth()
                .oauth2(tokenFor("user1", List.of(new SimpleGrantedAuthority("MODERATOR"))))
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
                .oauth2(tokenFor("user1", List.of(new SimpleGrantedAuthority("MODERATOR"))))
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
                .oauth2(tokenFor("user1", List.of(new SimpleGrantedAuthority("MODERATOR"))))
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
    void shouldNotAcceptNullLanguageName() {
        GraphQuery query = getNewLanguageGraphQuery(getNewLanguageVariables(null));
        given()
                .auth()
                .oauth2(tokenFor("user1", List.of(new SimpleGrantedAuthority("MODERATOR"))))
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
                .oauth2(tokenFor("user1"))
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
    void shouldRespondWith404ToUpdateLanguageIfLanguageDoesNotExist() {
        GraphQuery query = getUpdateGraphQuery(getUpdateVariables("lang", 1L));
        given()
                .auth()
                .oauth2(tokenFor("user1", List.of(new SimpleGrantedAuthority("MODERATOR"))))
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
    void shouldUpdateLanguage() {
        GraphQuery query = getUpdateGraphQuery(getUpdateVariables("newLanguage", addLanguage("language")));
        given()
                .auth()
                .oauth2(tokenFor("user1", List.of(new SimpleGrantedAuthority("MODERATOR"))))
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
                .oauth2(tokenFor("user1", List.of(new SimpleGrantedAuthority("MODERATOR"))))
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
                .oauth2(tokenFor("user1", List.of(new SimpleGrantedAuthority("MODERATOR"))))
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
    void shouldNotAcceptNullLanguageNameWhileUpdating() {
        Long languageId = addLanguage("language1");
        GraphQuery query = getUpdateGraphQuery(getUpdateVariables(null, languageId));
        given()
                .auth()
                .oauth2(tokenFor("user1", List.of(new SimpleGrantedAuthority("MODERATOR"))))
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