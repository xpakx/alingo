package io.github.xpakx.alingo.game;

import io.github.xpakx.alingo.game.dto.ExerciseDto;
import io.github.xpakx.alingo.game.dto.ExercisesResponse;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
public class ExerciseServiceImpl implements ExerciseService {
    @Override
    public ExercisesResponse prepareResponse(Page<Exercise> page) {
        ExercisesResponse response = new ExercisesResponse();
        response.setPage(page.getNumber());
        response.setSize(page.getTotalElements());
        Random random = new Random();
        response.setExercises(
                page.getContent().stream()
                        .map((a) -> toDto(a, random))
                        .toList()
        );
        return response;
    }


    private ExerciseDto toDto(Exercise exercise, Random random) {
        ExerciseDto dto = new ExerciseDto();
        dto.setId(exercise.getId());
        List<String> options =
                random.nextBoolean() ? List.of(exercise.getCorrectAnswer(), exercise.getWrongAnswer()) : List.of(exercise.getWrongAnswer(), exercise.getCorrectAnswer());
        dto.setOptions(options);
        return dto;
    }
}
