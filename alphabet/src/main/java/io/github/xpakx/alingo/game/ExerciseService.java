package io.github.xpakx.alingo.game;

import io.github.xpakx.alingo.game.dto.ExercisesResponse;
import org.springframework.data.domain.Page;

public interface ExerciseService {
    ExercisesResponse prepareResponse(Page<Exercise> page);
}
