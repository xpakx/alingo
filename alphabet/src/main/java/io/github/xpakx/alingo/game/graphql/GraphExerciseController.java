package io.github.xpakx.alingo.game.graphql;

import io.github.xpakx.alingo.game.*;
import io.github.xpakx.alingo.game.dto.ExerciseData;
import io.github.xpakx.alingo.game.dto.ExerciseRequest;
import io.github.xpakx.alingo.game.dto.OrderRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;

@Controller
@RequiredArgsConstructor
@Validated
public class GraphExerciseController {
    private final ExerciseService service;

    @MutationMapping
    @Secured("MODERATOR")
    public Exercise addExercise(@Argument String letter,
                                @NotBlank(message = "Wrong answer must be provided!") @Argument String wrongAnswer,
                                @NotBlank(message = "Correct answer must be provided!") @Argument String correctAnswer,
                                @NotNull(message = "Exercise must belong to a course!") @Argument Long courseId) {
        return service.createExercise(toRequest(letter, wrongAnswer, correctAnswer, courseId));
    }

    @MutationMapping
    @Secured("MODERATOR")
    public Exercise editExercise(@NotNull(message = "Exercise id must be provided!") @Argument Long exerciseId,
                                   @Argument String letter,
                                   @NotBlank(message = "Wrong answer must be provided!") @Argument String wrongAnswer,
                                   @NotBlank(message = "Correct answer must be provided!") @Argument String correctAnswer,
                                   @NotNull(message = "Exercise must belong to a course!") @Argument Long courseId) {
        return service.editExercise(exerciseId, toRequest(letter, wrongAnswer, correctAnswer, courseId));
    }

    private ExerciseRequest toRequest(String letter, String wrongAnswer, String correctAnswer, Long courseId) {
        return new ExerciseRequest(letter, wrongAnswer, correctAnswer, courseId);
    }

    @MutationMapping
    @Secured("MODERATOR")
    public Exercise reorderExercise(@NotNull(message = "Exercise id must be provided!") @Argument Long exerciseId,
                                 @NotNull(message = "Order must be provided") @PositiveOrZero(message = "Order cannot be negative") @Argument Integer order) {
        return service.changeOrder(exerciseId, new OrderRequest(order));
    }

    @QueryMapping
    @Secured("MODERATOR")
    public ExerciseData getExercise(@NotNull(message = "Exercise id must be provided!") @Argument Long exerciseId) {
        return service.getExercise(exerciseId);
    }
}
