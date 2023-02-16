package io.github.xpakx.alingo.game.dto;

import java.util.List;

public record ExercisesResponse(List<ExerciseDto> exercises,
    Integer page,
    Long size,
    Long totalSize) {
}
