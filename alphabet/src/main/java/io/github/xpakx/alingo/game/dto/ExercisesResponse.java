package io.github.xpakx.alingo.game.dto;

import io.github.xpakx.alingo.game.Exercise;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Setter
public class ExercisesResponse {
    List<ExerciseDto> exercises;
    Integer page;
    Long size;
    Long totalSize;
}
