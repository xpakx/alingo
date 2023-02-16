package io.github.xpakx.alingo.game;

import io.github.xpakx.alingo.game.dto.ExerciseDto;
import io.github.xpakx.alingo.game.dto.ExercisesResponse;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
public class ExerciseService {
    public ExercisesResponse prepareResponse(Page<Exercise> page, Random random) {
        return new ExercisesResponse(
                page.getContent().stream()
                        .map((a) -> toDto(a, random))
                        .toList(),
                page.getNumber(),
                (long) page.getContent().size(),
                page.getTotalElements()
        );
    }

    private ExerciseDto toDto(Exercise exercise, Random random) {
        ExerciseDto dto = new ExerciseDto();
        dto.setId(exercise.getId());
        if(random.nextBoolean()) {
            dto.setOptions(List.of(exercise.getCorrectAnswer(), exercise.getWrongAnswer()));
        } else {
            dto.setOptions(List.of(exercise.getWrongAnswer(), exercise.getCorrectAnswer()));
        }
        return dto;
    }
}
