package io.github.xpakx.alingo.game.graphql;

import io.github.xpakx.alingo.game.Language;
import io.github.xpakx.alingo.game.LanguageRepository;
import io.github.xpakx.alingo.game.dto.LanguageRequest;
import io.github.xpakx.alingo.security.JwtUtils;
import io.github.xpakx.alingo.utils.GraphLanguage;
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
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

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
        GraphQuery query = getGraphQuery(getVariables("lang"));
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

    private GraphQuery getGraphQuery(GraphLanguage answer) {
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

    private GraphLanguage getVariables(String name) {
        return new GraphLanguage(name);
    }

    @Test
    void shouldRespondWith401ToAddLanguageIfTokenIsWrong() {
        GraphQuery query = getGraphQuery(getVariables("lang"));
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
        GraphQuery query = getGraphQuery(getVariables("lang"));
        given()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(query)
        .when()
                .post(baseUrl + "/graphql")
        .then()
                .statusCode(FORBIDDEN.value());
    }

    private String tokenFor(String username) {
        return tokenFor(username, new ArrayList<>());
    }

    private String tokenFor(String username, List<GrantedAuthority> authorities) {
        return jwt.generateToken(new User(username, "", authorities));
    }

    @Test
    void shouldAddLanguage() {
        GraphQuery query = getGraphQuery(getVariables("lang"));
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
        GraphQuery query = getGraphQuery(getVariables("lang"));
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
        GraphQuery query = getGraphQuery(getVariables(""));
        given()
                .auth()
                .oauth2(tokenFor("user1", List.of(new SimpleGrantedAuthority("MODERATOR"))))
                .contentType(ContentType.JSON)
                .body(query)
        .when()
                .post(baseUrl + "/graphql")
        .then()
                .statusCode(BAD_REQUEST.value())
                .body("data", nullValue())
                .body("errors", not(nullValue()));
    }

    @Test
    void shouldNotAcceptNullLanguageName() {
        GraphQuery query = getGraphQuery(getVariables(null));
        given()
                .auth()
                .oauth2(tokenFor("user1", List.of(new SimpleGrantedAuthority("MODERATOR"))))
                .contentType(ContentType.JSON)
                .body(query)
        .when()
                .post(baseUrl + "/graphql")
        .then()
                .statusCode(BAD_REQUEST.value())
                .body("data", nullValue())
                .body("errors", not(nullValue()));
    }

}