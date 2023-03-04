package io.github.xpakx.alingo.game;

import io.github.xpakx.alingo.game.dto.*;
import io.github.xpakx.alingo.game.error.DataException;
import io.github.xpakx.alingo.game.error.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class ExerciseService {
    private final ExerciseRepository exerciseRepository;
    private final CourseRepository courseRepository;

    public ExercisesResponse prepareResponse(Page<Exercise> page, Random random, boolean premium) {
        return new ExercisesResponse(
                page.getContent().stream()
                        .map((a) -> toDto(a, random))
                        .toList(),
                page.getNumber(),
                (long) page.getContent().size(),
                page.getTotalElements(),
                premium
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

    @Transactional
    public Exercise createExercise(ExerciseRequest request) {
        Exercise exercise = new Exercise();
        copyFieldsToExercise(request, exercise);
        exercise.setOrder(exerciseRepository.getMaxOrderByCourseId(exercise.getCourse().getId()));
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

    @Transactional
    public Exercise changeOrder(Long exerciseId, OrderRequest request) {
        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(NotFoundException::new);
        if(Objects.equals(exercise.getOrder(), request.newOrder())) {
            return exercise;
        }
        Integer highestOrder = exerciseRepository.getMaxOrderByCourseId(exercise.getCourse().getId());
        if(request.newOrder() > highestOrder) {
            throw new DataException("Order is too high!");
        }
        if(request.newOrder() > exercise.getOrder()) {
            exerciseRepository.decrementOrderBetween(exercise.getCourse().getId(), exercise.getOrder(), request.newOrder()+1);
            exercise.setOrder(request.newOrder());
        } else {
            exerciseRepository.incrementOrderBetween(exercise.getCourse().getId(),request.newOrder()-1,  exercise.getOrder());
            exercise.setOrder(request.newOrder());
        }
        return exerciseRepository.save(exercise);
    }

    public ExerciseData getExercise(Long exerciseId) {
        return exerciseRepository.getProjectedById(exerciseId)
                .orElseThrow(NotFoundException::new);
    }
}
