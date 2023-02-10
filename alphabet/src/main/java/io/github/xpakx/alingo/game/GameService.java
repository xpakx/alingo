package io.github.xpakx.alingo.game;

import io.github.xpakx.alingo.game.dto.AnswerResponse;
import io.github.xpakx.alingo.game.dto.ExercisesResponse;

public interface GameService {
    AnswerResponse checkAnswer(Long exerciseId, String guess);
    ExercisesResponse getExercisesForCourse(Long courseId, Integer page, Integer amount);
}
