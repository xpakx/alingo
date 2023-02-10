package io.github.xpakx.alingo.game;

import io.github.xpakx.alingo.game.dto.AnswerRequest;
import io.github.xpakx.alingo.game.dto.AnswerResponse;
import io.github.xpakx.alingo.game.dto.CourseExercisesRequest;
import io.github.xpakx.alingo.game.dto.ExercisesResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class GameController {
    private final GameService service;

    @PostMapping("/exercise/{exerciseId}")
    public ResponseEntity<AnswerResponse> answer(@Valid @RequestBody AnswerRequest request, @PathVariable Long exerciseId) {
        return new ResponseEntity<>(
                service.checkAnswer(exerciseId, request),
                HttpStatus.OK
        );
    }

    @PostMapping("/course/{courseId}/exercise")
    public ResponseEntity<ExercisesResponse> getExercises(@Valid @RequestBody CourseExercisesRequest request, @PathVariable Long courseId) {
        return new ResponseEntity<>(
                service.getExercisesForCourse(courseId, request),
                HttpStatus.OK
        );
    }
}
