package io.github.xpakx.alingo.game;

import io.github.xpakx.alingo.game.dto.AnswerRequest;
import io.github.xpakx.alingo.game.dto.AnswerResponse;
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
}
