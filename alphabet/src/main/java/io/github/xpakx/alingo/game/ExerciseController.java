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
                HttpStatus.CREATED
        );
    }

    @PutMapping("/{exerciseId}")
    @Secured("MODERATOR")
    @ResponseBody
    public Exercise editExercise(@Valid @RequestBody ExerciseRequest request, @PathVariable Long exerciseId) {
        return service.editExercise(exerciseId, request);
    }

    @PutMapping("/{exerciseId}/order")
    @Secured("MODERATOR")
    @ResponseBody
    public Exercise reorder(@Valid @RequestBody OrderRequest request, @PathVariable Long exerciseId) {
        return service.changeOrder(exerciseId, request);
    }
}
