package io.github.xpakx.alingo.game.graphql;

import io.github.xpakx.alingo.game.GameService;
import io.github.xpakx.alingo.game.dto.AnswerRequest;
import io.github.xpakx.alingo.game.dto.AnswerResponse;
import io.github.xpakx.alingo.game.dto.ExercisesResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;

@Controller
@RequiredArgsConstructor
@Validated
public class GraphGameController {
    private final GameService service;

    @QueryMapping
    public ExercisesResponse courseExercises(@NotNull @Argument long course,
                                             @Min(value = 1) @Argument int page,
                                             @NotNull @Min(value = 1) @Max(value = 20) @Argument int amount) {
        return service.getExercisesForCourse(course, page-1, amount);
    }

    @MutationMapping
    public AnswerResponse answer(@NotNull @Argument long exercise, @NotBlank @Argument String guess) {
        return service.checkAnswer(exercise, toRequestAnswer(guess));
    }

    private AnswerRequest toRequestAnswer(String guess) {
        return new AnswerRequest(guess);
    }
}
