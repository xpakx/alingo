package io.github.xpakx.alingo.game.graphql;

import io.github.xpakx.alingo.game.Course;
import io.github.xpakx.alingo.game.CourseRepository;
import io.github.xpakx.alingo.game.Exercise;
import io.github.xpakx.alingo.game.ExerciseRepository;
import io.github.xpakx.alingo.game.dto.AnswerRequest;
import io.github.xpakx.alingo.security.JwtUtils;
import io.github.xpakx.alingo.utils.GraphAnswer;
import io.github.xpakx.alingo.utils.GraphQuery;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpStatus.OK;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GraphGameControllerTest {
    @LocalServerPort
    private int port;
    private String baseUrl;

    @Autowired
    JwtUtils jwt;
    @Autowired
    ExerciseRepository exerciseRepository;
    @Autowired
    CourseRepository courseRepository;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost".concat(":").concat(port + "");
    }

    @AfterEach
    void tearDown() {
        exerciseRepository.deleteAll();
        courseRepository.deleteAll();
    }

    private AnswerRequest getAnswerRequest(String answer) {
        AnswerRequest request = new AnswerRequest();
        request.setAnswer(answer);
        return request;
    }

    private String tokenFor(String username) {
        return tokenFor(username, new ArrayList<>());
    }

    private String tokenFor(String username, List<GrantedAuthority> authorities) {
        return jwt.generateToken(new User(username, "", authorities));
    }

    private Long addExercise(String correct, String wrong) {
        return addExercise(correct, wrong, null);
    }

    private Long addExercise(String correct, String wrong, Long courseId) {
        Exercise exercise = new Exercise();
        exercise.setCorrectAnswer(correct);
        exercise.setWrongAnswer(wrong);
        if(courseId != null) {
            exercise.setCourse(courseRepository.getReferenceById(courseId));
        }
        return exerciseRepository.save(exercise).getId();
    }

    private void addExercises(Long courseId, int amount) {
        for(int i=0; i<amount; i++) {
            addExercise("correct", "wrong", courseId);
        }
    }

    private Long createCourse(String courseName) {
        Course course = new Course();
        course.setName(courseName);
        return courseRepository.save(course).getId();
    }

    @Test
    public void shouldAcceptGuessWithGraphQL() {
        Long exerciseId = addExercise("correct", "wrong");
        GraphQuery query = getGraphQueryForAnswer(getVariablesForAnswer(exerciseId, "correct"));
        given()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(query)
                .log().all()
        .when()
                .post(baseUrl + "/graphql")
        .then()
                .log().all()
                .statusCode(OK.value())
                .body("data.answer.correct", equalTo(true));
    }

    private GraphQuery getGraphQueryForAnswer(GraphAnswer answer) {
        GraphQuery query = new GraphQuery();
        query.setQuery("""
                    mutation answer($id: Int, $guess: String){
                        answer(exercise: $id, guess: $guess)
                        {
                            correct
                            correctAnswer
                        }
                    }""");
        query.setVariables(answer);
        return query;
    }

    private GraphAnswer getVariablesForAnswer(Long exerciseId, String guess) {
        GraphAnswer answer = new GraphAnswer();
        answer.setId(exerciseId);
        answer.setGuess(guess);
        return answer;
    }
}