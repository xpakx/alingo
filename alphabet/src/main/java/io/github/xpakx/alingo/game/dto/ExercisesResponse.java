package io.github.xpakx.alingo.game.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

public record ExercisesResponse(List<ExerciseDto> exercises,
                                Integer page,
                                Long size,
                                Long totalSize,
                                @JsonIgnore boolean premium) {
}
