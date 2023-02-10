package io.github.xpakx.alingo.game.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ExercisesResponse {
    List<ExerciseDto> exercises;
    Long start;
    Long end;
    Long size;
}
