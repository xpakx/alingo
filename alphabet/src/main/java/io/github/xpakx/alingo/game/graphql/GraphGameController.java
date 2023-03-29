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
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;

@Controller
@RequiredArgsConstructor
@Validated
public class GraphGameController {
    private final GameService service;

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    @PostAuthorize("hasAuthority('SUBSCRIBER') or returnObject.premium==false")
    public ExercisesResponse courseExercises(@NotNull(message = "Course id must be provided!") @Argument long course,
                                             @Min(value = 1, message = "Page must be positive") @NotNull(message = "Page cannot be null") @Argument int page,
                                             @NotNull @Min(value = 1, message = "Amount must be between 1 and 20") @Max(value = 20, message = "Amount must be between 1 and 20") @Argument int amount) {
        return service.getExercisesForCourse(course, page-1, amount);
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public AnswerResponse answer(@NotNull(message = "Exercise id must be provided!") @Argument long exercise,
                                 @NotBlank(message = "Guess cannot be empty")  @Argument String guess) {
        return service.checkAnswer(exercise, toRequestAnswer(guess));
    }

    private AnswerRequest toRequestAnswer(String guess) {
        return new AnswerRequest(guess);
    }
}
