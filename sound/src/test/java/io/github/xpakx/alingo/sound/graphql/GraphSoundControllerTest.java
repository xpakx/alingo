package io.github.xpakx.alingo.sound.graphql;

import io.github.xpakx.alingo.security.JwtUtils;
import io.github.xpakx.alingo.sound.Sound;
import io.github.xpakx.alingo.sound.SoundRepository;
import io.github.xpakx.alingo.utils.GraphQuery;
import io.github.xpakx.alingo.utils.PageVariables;
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

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GraphSoundControllerTest {
    @LocalServerPort
    private int port;
    private String baseUrl;

    @Autowired
    JwtUtils jwt;
    @Autowired
    SoundRepository soundRepository;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost".concat(":").concat(port + "");
    }

    @AfterEach
    void tearDown() {
        soundRepository.deleteAll();
    }

    @Test
    void shouldRespondWith401ToGetSoundsIfNotAuthenticated() {
        GraphQuery query = getSoundsQuery(getPageVariables(1));
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

    private GraphQuery getSoundsQuery(PageVariables variables) {
        GraphQuery query = new GraphQuery();
        query.setQuery("""
                    query getSounds($page: Int){
                        getSounds(page: $page)
                        {
                            files
                        }
                    }""");
        query.setVariables(variables);
        return query;
    }

    private PageVariables getPageVariables(Integer page) {
        return new PageVariables(page);
    }

    private String tokenFor() {
        return tokenFor(new ArrayList<>());
    }

    private String tokenFor(List<GrantedAuthority> authorities) {
        return jwt.generateToken(new User("user1", "", authorities));
    }

    @Test
    void shouldRespondWith401ToGetSoundsIfTokenIsWrong() {
        GraphQuery query = getSoundsQuery(getPageVariables(1));
        given()
                .auth()
                .oauth2("204230990324")
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
    void shouldRespondWithEmptyListToGetSoundsIfThereAreNoSounds() {
        GraphQuery query = getSoundsQuery(getPageVariables(1));
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
                .body("data.getSounds.files", hasSize(0));
    }

    @ParameterizedTest
    @ValueSource(ints = {-10, -1, 0})
    void shouldNotAcceptGetSoundsRequestWithNonPositivePages(int page) {
        GraphQuery query = getSoundsQuery(getPageVariables(page));
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
    void shouldRespondWithListOfSounds() {
        addSound("sound1");
        addSound("sound2");
        addSound("sound2");
        GraphQuery query = getSoundsQuery(getPageVariables(1));
        given()
                .auth()
                .oauth2(tokenFor(List.of(new SimpleGrantedAuthority("MODERATOR"))))
                .contentType(ContentType.JSON)
                .body(query)
        .when()
                .post(baseUrl + "/graphql")
        .then()
                .statusCode(OK.value())
                .body("data.getSounds.files", hasSize(3));
    }

    private void addSound(String name) {
        Sound sound = new Sound();
        sound.setFilename(name);
        soundRepository.save(sound);
    }

}