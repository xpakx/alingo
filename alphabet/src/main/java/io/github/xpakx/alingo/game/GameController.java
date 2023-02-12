package io.github.xpakx.alingo.game;

import io.github.xpakx.alingo.game.dto.AnswerRequest;
import io.github.xpakx.alingo.game.dto.AnswerResponse;
import io.github.xpakx.alingo.game.dto.ExercisesResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Validated
public class GameController {
    private final GameService service;

    @PostMapping("/exercise/{exerciseId}")
    public ResponseEntity<AnswerResponse> answer(@Valid @RequestBody AnswerRequest request, @PathVariable Long exerciseId) {
        return new ResponseEntity<>(
                service.checkAnswer(exerciseId, request),
                HttpStatus.OK
        );
    }

    @GetMapping("/course/{courseId}/exercise")
    public ResponseEntity<ExercisesResponse> getExercises(@RequestParam @Min(value = 1) @NotNull Integer page,
                                                          @RequestParam @NotNull @Min(value = 1) @Max(value = 20) Integer amount,
                                                          @PathVariable Long courseId) {
        return new ResponseEntity<>(
                service.getExercisesForCourse(courseId, page-1, amount),
                HttpStatus.OK
        );
    }
}
