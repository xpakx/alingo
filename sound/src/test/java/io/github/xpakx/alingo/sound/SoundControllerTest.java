package io.github.xpakx.alingo.sound;

import io.github.xpakx.alingo.security.JwtUtils;
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
import static org.hamcrest.Matchers.*;
import static org.springframework.http.HttpStatus.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SoundControllerTest {
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

    private String tokenFor(String username) {
        return tokenFor(username, new ArrayList<>());
    }

    private String tokenFor(String username, List<GrantedAuthority> authorities) {
        return jwt.generateToken(new User(username, "", authorities));
    }
    @Test
    void shouldRespondWith401ToGetNamesIfNotAuthenticated() {
        given()
                .param("page", 1)
        .when()
                .get(baseUrl + "/sound/list")
        .then()
                .statusCode(UNAUTHORIZED.value());
    }

    @Test
    void shouldRespondWith401ToGetNamesIfTokenIsWrong() {
        given()
                .auth()
                .oauth2("21090cjw")
                .param("page", 1)
        .when()
                .get(baseUrl + "/sound/list")
        .then()
                .statusCode(UNAUTHORIZED.value());
    }

    @Test
    void shouldRespondWith403ToGetNamesIfUserIsNotModerator() {
        given()
                .auth()
                .oauth2(tokenFor("user1"))
                .param("page", 1)
        .when()
                .get(baseUrl + "/sound/list")
        .then()
                .statusCode(FORBIDDEN.value());
    }

    @Test
    void shouldRespondWithEmptyList() {
        given()
                .auth()
                .oauth2(tokenFor("user1", List.of(new SimpleGrantedAuthority("MODERATOR"))))
                .param("page", 1)
        .when()
                .get(baseUrl + "/sound/list")
        .then()
                .statusCode(OK.value())
                .body("files", hasSize(0));
    }

    @Test
    void shouldRespondWithListOfNames() {
        addSound("sound1.mp3");
        addSound("sound2.mp3");
        addSound("sound3.mp3");
        given()
                .auth()
                .oauth2(tokenFor("user1", List.of(new SimpleGrantedAuthority("MODERATOR"))))
                .param("page", 1)
        .when()
                .get(baseUrl + "/sound/list")
        .then()
                .statusCode(OK.value())
                .body("files", hasSize(3));
    }

    private void addSound(String name) {
        Sound sound = new Sound();
        sound.setFilename(name);
        soundRepository.save(sound);
    }


}