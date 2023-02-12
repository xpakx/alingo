package io.github.xpakx.alingo.game.graphql;

import io.github.xpakx.alingo.game.GameService;
import io.github.xpakx.alingo.game.dto.AnswerRequest;
import io.github.xpakx.alingo.game.dto.AnswerResponse;
import io.github.xpakx.alingo.game.dto.ExercisesResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class GraphGameController {
    private final GameService service;

    @QueryMapping
    public ExercisesResponse courseExercises(@Argument long course, @Argument int page, @Argument int amount) {
        return service.getExercisesForCourse(course, page, amount);
    }

    @MutationMapping
    public AnswerResponse answer(@Argument long exercise, @Argument String guess) {
        return service.checkAnswer(exercise, toRequestAnswer(guess));
    }

    private AnswerRequest toRequestAnswer(String guess) {
        AnswerRequest request = new AnswerRequest();
        request.setAnswer(guess);
        return request;
    }
}
