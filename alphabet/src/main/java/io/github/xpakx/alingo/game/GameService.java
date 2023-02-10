package io.github.xpakx.alingo.game;

import io.github.xpakx.alingo.game.dto.AnswerRequest;
import io.github.xpakx.alingo.game.dto.AnswerResponse;
import io.github.xpakx.alingo.game.dto.ExercisesResponse;

public interface GameService {
    AnswerResponse checkAnswer(Long exerciseId, AnswerRequest request);
    ExercisesResponse getExercisesForCourse(Long courseId, Integer page, Integer amount);
}
