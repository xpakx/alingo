package io.github.xpakx.alingo.game;

import io.github.xpakx.alingo.game.dto.ExerciseRequest;
import io.github.xpakx.alingo.game.dto.OrderRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/exercise")
public class ExerciseController {
    private final ExerciseService service;

    @PostMapping("/new")
    @Secured("MODERATOR")
    public ResponseEntity<Exercise> createExercise(@Valid @RequestBody ExerciseRequest request) {
        return new ResponseEntity<>(
                service.createExercise(request),
                HttpStatus.OK
        );
    }

    @PutMapping("/{exerciseId}")
    @Secured("MODERATOR")
    public ResponseEntity<Exercise> editExercise(@Valid @RequestBody ExerciseRequest request, @PathVariable Long exerciseId) {
        return new ResponseEntity<>(
                service.editExercise(exerciseId, request),
                HttpStatus.OK
        );
    }

    @PutMapping("/{exerciseId}/order")
    @Secured("MODERATOR")
    public ResponseEntity<Exercise> reorder(@Valid @RequestBody OrderRequest request, @PathVariable Long exerciseId) {
        return new ResponseEntity<>(
                service.changeOrder(exerciseId, request),
                HttpStatus.OK
        );
    }
}
