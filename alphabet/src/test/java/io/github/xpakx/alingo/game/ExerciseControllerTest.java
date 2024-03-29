package io.github.xpakx.alingo.game;

import io.github.xpakx.alingo.game.dto.ExerciseRequest;
import io.github.xpakx.alingo.game.dto.OrderRequest;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ExerciseControllerTest {
    @LocalServerPort
    private int port;
    private String baseUrl;

    @Autowired
    JwtUtils jwt;
    @Autowired
    CourseRepository courseRepository;
    @Autowired
    ExerciseRepository exerciseRepository;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost".concat(":").concat(port + "");
    }

    @AfterEach
    void tearDown() {
        exerciseRepository.deleteAll();
        courseRepository.deleteAll();
    }

    @Test
    void shouldRespondWith401ToAddExerciseIfNotAuthenticated() {
        when()
                .post(baseUrl + "/exercise/new")
       .then()
                .statusCode(UNAUTHORIZED.value())
                .body("error", equalTo(UNAUTHORIZED.value()))
                .body("errors", nullValue());
    }

    @Test
    void shouldRespondWith401ToAddExerciseIfTokenIsWrong() {
        given()
                .auth()
                .oauth2("329432853295")
                .contentType(ContentType.JSON)
                .body(getExerciseRequest("wrong", "correct", 1L))
        .when()
                .post(baseUrl + "/exercise/new")
        .then()
                .statusCode(UNAUTHORIZED.value())
                .body("error", equalTo(UNAUTHORIZED.value()))
                .body("errors", nullValue());
    }

    private ExerciseRequest getExerciseRequest(String wrong, String correct, Long courseId) {
        return getExerciseRequest("a", wrong, correct, courseId);
    }

    private ExerciseRequest getExerciseRequest(String letter, String wrong, String correct, Long courseId) {
        return new ExerciseRequest(letter, wrong, correct, courseId, null);
    }

    @Test
    void shouldRespondWith403ToAddExerciseIfUserIsNotModerator() {
        given()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(getExerciseRequest("wrong", "correct", 1L))
        .when()
                .post(baseUrl + "/exercise/new")
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
    void shouldAddExercise() {
        given()
                .auth()
                .oauth2(tokenFor("user1", List.of(new SimpleGrantedAuthority("MODERATOR"))))
                .contentType(ContentType.JSON)
                .body(getExerciseRequest("g", "wrong", "correct", addCourse()))
        .when()
                .post(baseUrl + "/exercise/new")
        .then()
                .statusCode(CREATED.value())
                .body("letter", equalTo("g"));
    }

    private Long addCourse() {
        Course course = new Course();
        course.setName("course");
        return courseRepository.save(course).getId();
    }


    @Test
    void shouldAddNewCourseToDb() {
        given()
                .auth()
                .oauth2(tokenFor("user1", List.of(new SimpleGrantedAuthority("MODERATOR"))))
                .contentType(ContentType.JSON)
                .body(getExerciseRequest("g", "wrong", "correct", addCourse()))
        .when()
                .post(baseUrl + "/exercise/new");
        List<Exercise> exercises = exerciseRepository.findAll();
        assertThat(exercises, hasItem(hasProperty("letter", equalTo("g"))));
    }

    @Test
    void shouldNotAcceptEmptyWrongOption() {
        given()
                .auth()
                .oauth2(tokenFor("user1", List.of(new SimpleGrantedAuthority("MODERATOR"))))
                .contentType(ContentType.JSON)
                .body(getExerciseRequest("", "correct", addCourse()))
        .when()
                .post(baseUrl + "/exercise/new")
        .then()
                .statusCode(BAD_REQUEST.value())
                .body("error", equalTo(BAD_REQUEST.value()))
                .body("message", containsStringIgnoringCase("Validation failed"))
                .body("errors", hasItem(both(containsStringIgnoringCase("wrong")).and(containsStringIgnoringCase("provided"))));
    }

    @Test
    void shouldNotAcceptNullWrongOption() {
        given()
                .auth()
                .oauth2(tokenFor("user1", List.of(new SimpleGrantedAuthority("MODERATOR"))))
                .contentType(ContentType.JSON)
                .body(getExerciseRequest(null, "correct", addCourse()))
        .when()
                .post(baseUrl + "/exercise/new")
        .then()
                .statusCode(BAD_REQUEST.value())
                .body("error", equalTo(BAD_REQUEST.value()))
                .body("message", containsStringIgnoringCase("Validation failed"))
                .body("errors", hasItem(both(containsStringIgnoringCase("wrong")).and(containsStringIgnoringCase("provided"))));
    }

    @Test
    void shouldNotAcceptEmptyCorrectOption() {
        given()
                .auth()
                .oauth2(tokenFor("user1", List.of(new SimpleGrantedAuthority("MODERATOR"))))
                .contentType(ContentType.JSON)
                .body(getExerciseRequest("wrong", "", addCourse()))
        .when()
                .post(baseUrl + "/exercise/new")
        .then()
                .statusCode(BAD_REQUEST.value())
                .body("error", equalTo(BAD_REQUEST.value()))
                .body("message", containsStringIgnoringCase("Validation failed"))
                .body("errors", hasItem(both(containsStringIgnoringCase("correct")).and(containsStringIgnoringCase("provided"))));
    }

    @Test
    void shouldNotAcceptNullCorrectOption() {
        given()
                .auth()
                .oauth2(tokenFor("user1", List.of(new SimpleGrantedAuthority("MODERATOR"))))
                .contentType(ContentType.JSON)
                .body(getExerciseRequest("wrong", null, addCourse()))
        .when()
                .post(baseUrl + "/exercise/new")
        .then()
                .statusCode(BAD_REQUEST.value())
                .body("error", equalTo(BAD_REQUEST.value()))
                .body("message", containsStringIgnoringCase("Validation failed"))
                .body("errors", hasItem(both(containsStringIgnoringCase("correct")).and(containsStringIgnoringCase("provided"))));
    }

    @Test
    void shouldNotAcceptNullCourseId() {
        given()
                .auth()
                .oauth2(tokenFor("user1", List.of(new SimpleGrantedAuthority("MODERATOR"))))
                .contentType(ContentType.JSON)
                .body(getExerciseRequest("wrong", "correct", null))
        .when()
                .post(baseUrl + "/exercise/new")
        .then()
                .statusCode(BAD_REQUEST.value())
                .body("error", equalTo(BAD_REQUEST.value()))
                .body("message", containsStringIgnoringCase("Validation failed"))
                .body("errors", hasItem(both(containsStringIgnoringCase("belong")).and(containsStringIgnoringCase("course"))));
    }

    @Test
    void shouldRespondWith401ToUpdateExerciseIfNotAuthenticated() {
        when()
                .put(baseUrl + "/exercise/{exerciseId}", 1L)
        .then()
                .statusCode(UNAUTHORIZED.value())
                .body("error", equalTo(UNAUTHORIZED.value()))
                .body("errors", nullValue());
    }

    @Test
    void shouldRespondWith401ToUpdateExerciseIfTokenIsWrong() {
        given()
                .auth()
                .oauth2("329432853295")
                .contentType(ContentType.JSON)
                .body(getExerciseRequest("wrong", "correct", 1L))
        .when()
                .put(baseUrl + "/exercise/{exerciseId}", 1L)
        .then()
                .statusCode(UNAUTHORIZED.value())
                .body("error", equalTo(UNAUTHORIZED.value()))
                .body("errors", nullValue());
    }

    @Test
    void shouldRespondWith403ToUpdateExerciseIfUserIsNotModerator() {
        given()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(getExerciseRequest("wrong", "correct", 1L))
        .when()
                .put(baseUrl + "/exercise/{exerciseId}", 1L)
        .then()
                .statusCode(FORBIDDEN.value())
                .body("error", equalTo(FORBIDDEN.value()))
                .body("errors", nullValue());
    }

    @Test
    void shouldRespondWith404ToUpdateExerciseIfExerciseDoesNotExist() {
        given()
                .auth()
                .oauth2(tokenFor("user1", List.of(new SimpleGrantedAuthority("MODERATOR"))))
                .contentType(ContentType.JSON)
                .body(getExerciseRequest("wrong", "correct", 1L))
        .when()
                .put(baseUrl + "/exercise/{exerciseId}", 1L)
        .then()
                .statusCode(NOT_FOUND.value())
                .body("error", equalTo(NOT_FOUND.value()))
                .body("errors", nullValue());
    }

    @Test
    void shouldUpdateExercise() {
        Long courseId = addCourse();
        Long exerciseId = addExercise("g", courseId);
        given()
                .auth()
                .oauth2(tokenFor("user1", List.of(new SimpleGrantedAuthority("MODERATOR"))))
                .contentType(ContentType.JSON)
                .body(getExerciseRequest("n", "wrong", "correct", courseId))
        .when()
                .put(baseUrl + "/exercise/{exerciseId}", exerciseId)
        .then()
                .statusCode(OK.value())
                .body("letter", equalTo("n"));
    }

    private Long addExercise(String letter, Long courseId) {
        Exercise exercise = new Exercise();
        exercise.setLetter(letter);
        exercise.setWrongAnswer("wrong");
        exercise.setCorrectAnswer("correct");
        exercise.setCourse(courseId != null ? courseRepository.getReferenceById(courseId) : null);
        return exerciseRepository.save(exercise).getId();
    }

    @Test
    void shouldUpdateExerciseIndDb() {
        Long courseId = addCourse();
        Long exerciseId = addExercise("g", courseId);
        given()
                .auth()
                .oauth2(tokenFor("user1", List.of(new SimpleGrantedAuthority("MODERATOR"))))
                .contentType(ContentType.JSON)
                .body(getExerciseRequest("n", "wrong", "correct", courseId))
        .when()
                .put(baseUrl + "/exercise/{exerciseId}", exerciseId);
        Optional<Exercise> exercise = exerciseRepository.findById(exerciseId);
        assertTrue(exercise.isPresent());
        assertThat(exercise.get(), hasProperty("letter", equalTo("n")));
    }

    @Test
    void shouldNotAcceptEmptyWrongAnswerWhileUpdating() {
        Long courseId = addCourse();
        Long exerciseId = addExercise("g", courseId);
        given()
                .auth()
                .oauth2(tokenFor("user1", List.of(new SimpleGrantedAuthority("MODERATOR"))))
                .contentType(ContentType.JSON)
                .body(getExerciseRequest("n", "", "correct", courseId))
       .when()
                .put(baseUrl + "/exercise/{exerciseId}", exerciseId)
       .then()
                .statusCode(BAD_REQUEST.value())
                .body("error", equalTo(BAD_REQUEST.value()))
                .body("message", containsStringIgnoringCase("Validation failed"))
                .body("errors", hasItem(both(containsStringIgnoringCase("wrong")).and(containsStringIgnoringCase("provided"))));
    }

    @Test
    void shouldNotAcceptNullWrongAnswerWhileUpdating() {
        Long courseId = addCourse();
        Long exerciseId = addExercise("g", courseId);
        given()
                .auth()
                .oauth2(tokenFor("user1", List.of(new SimpleGrantedAuthority("MODERATOR"))))
                .contentType(ContentType.JSON)
                .body(getExerciseRequest("n", null, "correct", courseId))
        .when()
                .put(baseUrl + "/exercise/{exerciseId}", exerciseId)
        .then()
                .statusCode(BAD_REQUEST.value())
                .body("error", equalTo(BAD_REQUEST.value()))
                .body("message", containsStringIgnoringCase("Validation failed"))
                .body("errors", hasItem(both(containsStringIgnoringCase("wrong")).and(containsStringIgnoringCase("provided"))));
    }

    @Test
    void shouldNotAcceptEmptyCorrectAnswerWhileUpdating() {
        Long courseId = addCourse();
        Long exerciseId = addExercise("g", courseId);
        given()
                .auth()
                .oauth2(tokenFor("user1", List.of(new SimpleGrantedAuthority("MODERATOR"))))
                .contentType(ContentType.JSON)
                .body(getExerciseRequest("n", "wrong", "", courseId))
        .when()
                .put(baseUrl + "/exercise/{exerciseId}", exerciseId)
        .then()
                .statusCode(BAD_REQUEST.value())
                .body("error", equalTo(BAD_REQUEST.value()))
                .body("message", containsStringIgnoringCase("Validation failed"))
                .body("errors", hasItem(both(containsStringIgnoringCase("correct")).and(containsStringIgnoringCase("provided"))));
    }

    @Test
    void shouldNotAcceptNullCorrectAnswerWhileUpdating() {
        Long courseId = addCourse();
        Long exerciseId = addExercise("g", courseId);
        given()
                .auth()
                .oauth2(tokenFor("user1", List.of(new SimpleGrantedAuthority("MODERATOR"))))
                .contentType(ContentType.JSON)
                .body(getExerciseRequest("n", "wrong", null, courseId))
        .when()
                .put(baseUrl + "/exercise/{exerciseId}", exerciseId)
        .then()
                .statusCode(BAD_REQUEST.value())
                .body("error", equalTo(BAD_REQUEST.value()))
                .body("message", containsStringIgnoringCase("Validation failed"))
                .body("errors", hasItem(both(containsStringIgnoringCase("correct")).and(containsStringIgnoringCase("provided"))));
    }

    @Test
    void shouldNotAcceptNullCourseIdWhileUpdating() {
        Long courseId = addCourse();
        Long exerciseId = addExercise("g", courseId);
        given()
                .auth()
                .oauth2(tokenFor("user1", List.of(new SimpleGrantedAuthority("MODERATOR"))))
                .contentType(ContentType.JSON)
                .body(getExerciseRequest("n", "wrong", "correct", null))
        .when()
                .put(baseUrl + "/exercise/{exerciseId}", exerciseId)
        .then()
                .statusCode(BAD_REQUEST.value())
                .body("error", equalTo(BAD_REQUEST.value()))
                .body("message", containsStringIgnoringCase("Validation failed"))
                .body("errors", hasItem(both(containsStringIgnoringCase("belong")).and(containsStringIgnoringCase("course"))));
    }

    @Test
    void shouldRespondWith401ToReorderExerciseIfNotAuthenticated() {
        when()
                .put(baseUrl + "/exercise/{exerciseId}/order", 1L)
        .then()
                .statusCode(UNAUTHORIZED.value())
                .body("error", equalTo(UNAUTHORIZED.value()))
                .body("errors", nullValue());
    }

    @Test
    void shouldRespondWith401ToReorderExerciseIfTokenIsWrong() {
        given()
                .auth()
                .oauth2("329432853295")
                .contentType(ContentType.JSON)
                .body(getOrderRequest(0))
        .when()
                .put(baseUrl + "/exercise/{exerciseId}/order", 1L)
        .then()
                .statusCode(UNAUTHORIZED.value())
                .body("error", equalTo(UNAUTHORIZED.value()))
                .body("errors", nullValue());
    }

    private OrderRequest getOrderRequest(Integer order) {
        return new OrderRequest(order);
    }

    @Test
    void shouldRespondWith403ToReorderExerciseIfUserIsNotModerator() {
        given()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(getOrderRequest(0))
        .when()
                .put(baseUrl + "/exercise/{exerciseId}/order", 1L)
        .then()
                .statusCode(FORBIDDEN.value())
                .body("error", equalTo(FORBIDDEN.value()))
                .body("errors", nullValue());
    }

    @Test
    void shouldRespondWith404ToReorderExerciseIfExerciseDoesNotExist() {
        given()
                .auth()
                .oauth2(tokenFor("user1", List.of(new SimpleGrantedAuthority("MODERATOR"))))
                .contentType(ContentType.JSON)
                .body(getOrderRequest(0))
        .when()
                .put(baseUrl + "/exercise/{exerciseId}/order", 1L)
        .then()
                .statusCode(NOT_FOUND.value())
                .body("error", equalTo(NOT_FOUND.value()))
                .body("errors", nullValue());
    }

    @Test
    void shouldNotAcceptNullOrderWhileReordering() {
        Long exerciseId = addExercise("g", addCourse());
        given()
                .auth()
                .oauth2(tokenFor("user1", List.of(new SimpleGrantedAuthority("MODERATOR"))))
                .contentType(ContentType.JSON)
                .body(getOrderRequest(null))
        .when()
                .put(baseUrl + "/exercise/{exerciseId}/order", exerciseId)
        .then()
                .statusCode(BAD_REQUEST.value())
                .body("error", equalTo(BAD_REQUEST.value()))
                .body("message", containsStringIgnoringCase("Validation failed"))
                .body("errors", hasItem(both(containsStringIgnoringCase("order")).and(containsStringIgnoringCase("provided"))));
    }

    @Test
    void shouldNotAcceptNegativeOrderWhileReordering() {
        Long exerciseId = addExercise("g", addCourse());
        given()
                .auth()
                .oauth2(tokenFor("user1", List.of(new SimpleGrantedAuthority("MODERATOR"))))
                .contentType(ContentType.JSON)
                .body(getOrderRequest(-1))
        .when()
                .put(baseUrl + "/exercise/{exerciseId}/order", exerciseId)
        .then()
                .statusCode(BAD_REQUEST.value())
                .body("error", equalTo(BAD_REQUEST.value()))
                .body("message", containsStringIgnoringCase("Validation failed"))
                .body("errors", hasItem(both(containsStringIgnoringCase("order")).and(containsStringIgnoringCase("negative"))));
    }

    @Test
    void shouldReorder() {
        Long exerciseId = addOrderedExercise(addCourse(), 0);
        given()
                .auth()
                .oauth2(tokenFor("user1", List.of(new SimpleGrantedAuthority("MODERATOR"))))
                .contentType(ContentType.JSON)
                .body(getOrderRequest(0))
        .when()
                .put(baseUrl + "/exercise/{exerciseId}/order", exerciseId)
        .then()
                .statusCode(OK.value());
    }

    private Long addOrderedExercise(Long courseId, Integer order) {
        Exercise exercise = new Exercise();
        exercise.setLetter("a");
        exercise.setWrongAnswer("wrong");
        exercise.setCorrectAnswer("correct");
        exercise.setCourse(courseId != null ? courseRepository.getReferenceById(courseId) : null);
        exercise.setOrder(order);
        return exerciseRepository.save(exercise).getId();
    }

    @Test
    void shouldReorderUpwardWithoutGaps() {
        Long courseId = addCourse();
        Long ex1Id = addOrderedExercise(courseId, 0);
        Long ex2Id = addOrderedExercise(courseId, 1);
        Long ex3Id = addOrderedExercise(courseId, 2);
        Long ex4Id = addOrderedExercise(courseId, 3);
        Long ex5Id = addOrderedExercise(courseId, 4);
        Long ex6Id = addOrderedExercise(courseId, 5);
        Long ex7Id = addOrderedExercise(courseId, 6);
        given()
                .auth()
                .oauth2(tokenFor("user1", List.of(new SimpleGrantedAuthority("MODERATOR"))))
                .contentType(ContentType.JSON)
                .body(getOrderRequest(1))
        .when()
                .put(baseUrl + "/exercise/{exerciseId}/order", ex5Id)
        .then()
                .statusCode(OK.value());
        List<Exercise> exercises = exerciseRepository.findAll();
        assertThat(exercises, hasItem(both(hasProperty("id", equalTo(ex1Id))).and(hasProperty("order", equalTo(0)))));
        assertThat(exercises, hasItem(both(hasProperty("id", equalTo(ex5Id))).and(hasProperty("order", equalTo(1)))));
        assertThat(exercises, hasItem(both(hasProperty("id", equalTo(ex2Id))).and(hasProperty("order", equalTo(2)))));
        assertThat(exercises, hasItem(both(hasProperty("id", equalTo(ex3Id))).and(hasProperty("order", equalTo(3)))));
        assertThat(exercises, hasItem(both(hasProperty("id", equalTo(ex4Id))).and(hasProperty("order", equalTo(4)))));
        assertThat(exercises, hasItem(both(hasProperty("id", equalTo(ex6Id))).and(hasProperty("order", equalTo(5)))));
        assertThat(exercises, hasItem(both(hasProperty("id", equalTo(ex7Id))).and(hasProperty("order", equalTo(6)))));
    }

    @Test
    void shouldReorderDownwardWithoutGaps() {
        Long courseId = addCourse();
        Long ex1Id = addOrderedExercise(courseId, 0);
        Long ex2Id = addOrderedExercise(courseId, 1);
        Long ex3Id = addOrderedExercise(courseId, 2);
        Long ex4Id = addOrderedExercise(courseId, 3);
        Long ex5Id = addOrderedExercise(courseId, 4);
        Long ex6Id = addOrderedExercise(courseId, 5);
        Long ex7Id = addOrderedExercise(courseId, 6);
        given()
                .auth()
                .oauth2(tokenFor("user1", List.of(new SimpleGrantedAuthority("MODERATOR"))))
                .contentType(ContentType.JSON)
                .body(getOrderRequest(5))
        .when()
                .put(baseUrl + "/exercise/{exerciseId}/order", ex3Id)
        .then()
                .statusCode(OK.value());
        List<Exercise> exercises = exerciseRepository.findAll();
        assertThat(exercises, hasItem(both(hasProperty("id", equalTo(ex1Id))).and(hasProperty("order", equalTo(0)))));
        assertThat(exercises, hasItem(both(hasProperty("id", equalTo(ex2Id))).and(hasProperty("order", equalTo(1)))));
        assertThat(exercises, hasItem(both(hasProperty("id", equalTo(ex4Id))).and(hasProperty("order", equalTo(2)))));
        assertThat(exercises, hasItem(both(hasProperty("id", equalTo(ex5Id))).and(hasProperty("order", equalTo(3)))));
        assertThat(exercises, hasItem(both(hasProperty("id", equalTo(ex6Id))).and(hasProperty("order", equalTo(4)))));
        assertThat(exercises, hasItem(both(hasProperty("id", equalTo(ex3Id))).and(hasProperty("order", equalTo(5)))));
        assertThat(exercises, hasItem(both(hasProperty("id", equalTo(ex7Id))).and(hasProperty("order", equalTo(6)))));
    }

    @Test
    void shouldMoveAtTheFirstPlaceWithoutGaps() {
        Long courseId = addCourse();
        Long ex1Id = addOrderedExercise(courseId, 0);
        Long ex2Id = addOrderedExercise(courseId, 1);
        Long ex3Id = addOrderedExercise(courseId, 2);
        Long ex4Id = addOrderedExercise(courseId, 3);
        given()
                .auth()
                .oauth2(tokenFor("user1", List.of(new SimpleGrantedAuthority("MODERATOR"))))
                .contentType(ContentType.JSON)
                .body(getOrderRequest(0))
        .when()
                .put(baseUrl + "/exercise/{exerciseId}/order", ex3Id)
        .then()
                .statusCode(OK.value());
        List<Exercise> exercises = exerciseRepository.findAll();
        assertThat(exercises, hasItem(both(hasProperty("id", equalTo(ex3Id))).and(hasProperty("order", equalTo(0)))));
        assertThat(exercises, hasItem(both(hasProperty("id", equalTo(ex1Id))).and(hasProperty("order", equalTo(1)))));
        assertThat(exercises, hasItem(both(hasProperty("id", equalTo(ex2Id))).and(hasProperty("order", equalTo(2)))));
        assertThat(exercises, hasItem(both(hasProperty("id", equalTo(ex4Id))).and(hasProperty("order", equalTo(3)))));
    }

    @Test
    void shouldMoveAtTheLastPlaceWithoutGaps() {
        Long courseId = addCourse();
        Long ex1Id = addOrderedExercise(courseId, 0);
        Long ex2Id = addOrderedExercise(courseId, 1);
        Long ex3Id = addOrderedExercise(courseId, 2);
        Long ex4Id = addOrderedExercise(courseId, 3);
        given()
                .auth()
                .oauth2(tokenFor("user1", List.of(new SimpleGrantedAuthority("MODERATOR"))))
                .contentType(ContentType.JSON)
                .body(getOrderRequest(3))
        .when()
                .put(baseUrl + "/exercise/{exerciseId}/order", ex2Id)
        .then()
                .statusCode(OK.value());
        List<Exercise> exercises = exerciseRepository.findAll();
        assertThat(exercises, hasItem(both(hasProperty("id", equalTo(ex1Id))).and(hasProperty("order", equalTo(0)))));
        assertThat(exercises, hasItem(both(hasProperty("id", equalTo(ex3Id))).and(hasProperty("order", equalTo(1)))));
        assertThat(exercises, hasItem(both(hasProperty("id", equalTo(ex4Id))).and(hasProperty("order", equalTo(2)))));
        assertThat(exercises, hasItem(both(hasProperty("id", equalTo(ex2Id))).and(hasProperty("order", equalTo(3)))));
    }

    @Test
    void shouldNotAcceptOrderGreaterThanTheGreatestForTheCourse() {
        Long courseId = addCourse();
        addOrderedExercise(courseId, 0);
        Long exerciseId = addOrderedExercise(courseId, 1);
        addOrderedExercise(courseId, 2);
        given()
                .auth()
                .oauth2(tokenFor("user1", List.of(new SimpleGrantedAuthority("MODERATOR"))))
                .contentType(ContentType.JSON)
                .body(getOrderRequest(5))
        .when()
                .put(baseUrl + "/exercise/{exerciseId}/order", exerciseId)
        .then()
                .statusCode(BAD_REQUEST.value())
                .body("error", equalTo(BAD_REQUEST.value()))
                .body("message", both(containsStringIgnoringCase("order")).and(containsStringIgnoringCase("high")));
    }

    @Test
    void shouldRespondWith401ToGetExerciseIfNotAuthenticated() {
        when()
                .get(baseUrl + "/exercise/{exerciseId}", 1L)
        .then()
                .statusCode(UNAUTHORIZED.value())
                .body("error", equalTo(UNAUTHORIZED.value()))
                .body("errors", nullValue());
    }

    @Test
    void shouldRespondWith401ToGetExerciseIfTokenIsWrong() {
        given()
                .auth()
                .oauth2("329432853295")
        .when()
                .get(baseUrl + "/exercise/{exerciseId}", 1L)
        .then()
                .statusCode(UNAUTHORIZED.value())
                .body("error", equalTo(UNAUTHORIZED.value()))
                .body("errors", nullValue());
    }

    @Test
    void shouldRespondWith403ToGetExerciseIfUserIsNotModerator() {
        given()
                .auth()
                .oauth2(tokenFor("user1"))
        .when()
                .get(baseUrl + "/exercise/{exerciseId}", 1L)
        .then()
                .statusCode(FORBIDDEN.value())
                .body("error", equalTo(FORBIDDEN.value()))
                .body("errors", nullValue());
    }

    @Test
    void shouldRespondWith404ToGetExerciseIfCourseDoesNotExist() {
        given()
                .auth()
                .oauth2(tokenFor("user1", List.of(new SimpleGrantedAuthority("MODERATOR"))))
        .when()
                .get(baseUrl + "/exercise/{exerciseId}", 1L)
        .then()
                .statusCode(NOT_FOUND.value())
                .body("error", equalTo(NOT_FOUND.value()))
                .body("errors", nullValue());
    }

    @Test
    void shouldReturnExercise() {
        Long exerciseId = addExercise("a", addCourse());
        given()
                .auth()
                .oauth2(tokenFor("user1", List.of(new SimpleGrantedAuthority("MODERATOR"))))
        .when()
                .get(baseUrl + "/exercise/{exerciseId}", exerciseId)
        .then()
                .statusCode(OK.value())
                .body("letter", equalTo("a"));
    }
}