package io.github.xpakx.alingo.game;

import io.github.xpakx.alingo.game.dto.*;
import io.github.xpakx.alingo.game.error.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class ExerciseService {
    private final ExerciseRepository exerciseRepository;
    private final CourseRepository courseRepository;

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

    public Exercise createExercise(ExerciseRequest request) {
        Exercise exercise = new Exercise();
        copyFieldsToExercise(request, exercise);
        exercise.setOrder(0);
        return exerciseRepository.save(exercise);
    }

    public Exercise editExercise(Long exerciseId, ExerciseRequest request) {
        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(NotFoundException::new);
        copyFieldsToExercise(request, exercise);
        return exerciseRepository.save(exercise);
    }

    private void copyFieldsToExercise(ExerciseRequest request, Exercise exercise) {
        exercise.setLetter(request.letter());
        exercise.setWrongAnswer(request.wrongAnswer());
        exercise.setCorrectAnswer(request.correctAnswer());
        if(request.courseId() != null) {
            exercise.setCourse(courseRepository.getReferenceById(request.courseId()));
        } else {
            exercise.setCourse(null);
        }
    }
}
