package io.github.xpakx.alingo.game;

import io.github.xpakx.alingo.game.dto.AnswerRequest;
import io.github.xpakx.alingo.game.dto.AnswerResponse;

public interface GameService {
    AnswerResponse checkAnswer(Long exerciseId, AnswerRequest request);
}
