package io.github.xpakx.alingo.game;

import io.github.xpakx.alingo.game.dto.AnswerRequest;
import io.github.xpakx.alingo.game.dto.AnswerResponse;
import io.github.xpakx.alingo.game.dto.ExercisesResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Validated
public class GameController {
    private final GameService service;

    @PostMapping("/exercise/{exerciseId}")
    @ResponseBody
    public AnswerResponse answer(@Valid @RequestBody AnswerRequest request, @PathVariable Long exerciseId) {
        return service.checkAnswer(exerciseId, request);
    }

    @GetMapping("/course/{courseId}/exercise")
    @ResponseBody
    @PostAuthorize("hasAuthority('SUBSCRIBER') or returnObject.premium==false")
    public ExercisesResponse getExercises(@RequestParam @Min(value = 1, message = "Page must be positive") @NotNull(message = "Page cannot be null") Integer page,
                                                          @RequestParam @NotNull @Min(value = 1, message = "Amount must be between 1 and 20") @Max(value = 20, message = "Amount must be between 1 and 20") Integer amount,
                                                          @PathVariable Long courseId) {
        return service.getExercisesForCourse(courseId, page-1, amount);
    }
}
