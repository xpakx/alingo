package io.github.xpakx.alingo.game;

import io.github.xpakx.alingo.game.dto.ExerciseDto;
import io.github.xpakx.alingo.game.dto.ExercisesResponse;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExerciseServiceImpl implements ExerciseService {
    @Override
    public ExercisesResponse prepareResponse(Page<Exercise> page) {
        ExercisesResponse response = new ExercisesResponse();
        response.setPage(page.getNumber());
        response.setSize(page.getTotalElements());
        response.setExercises(page.getContent().stream().map(this::toDto).toList());
        return response;
    }


    private ExerciseDto toDto(Exercise exercise) {
        ExerciseDto dto = new ExerciseDto();
        dto.setId(exercise.getId());
        List<String> options = List.of(exercise.getCorrectAnswer(), exercise.getWrongAnswer());
        dto.setOptions(options);
        return dto;
    }
}
