package io.github.xpakx.alingo.game;

import io.github.xpakx.alingo.game.dto.*;
import io.github.xpakx.alingo.game.error.DataException;
import io.github.xpakx.alingo.game.error.NotFoundException;
import io.github.xpakx.alingo.utils.EvictExerciseCache;
import io.github.xpakx.alingo.utils.EvictExercisesCache;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
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
                page.getNumber()+1,
                (long) page.getContent().size(),
                page.getTotalElements(),
                premium
        );
    }

    private ExerciseDto toDto(Exercise exercise, Random random) {
        ExerciseDto dto = new ExerciseDto();
        dto.setId(exercise.getId());
        dto.setSoundFilename(exercise.getSoundFilename());
        if(random.nextBoolean()) {
            dto.setOptions(List.of(exercise.getCorrectAnswer(), exercise.getWrongAnswer()));
        } else {
            dto.setOptions(List.of(exercise.getWrongAnswer(), exercise.getCorrectAnswer()));
        }
        return dto;
    }

    @Transactional
    @EvictExercisesCache
    public Exercise createExercise(ExerciseRequest request) {
        Exercise exercise = new Exercise();
        copyFieldsToExercise(request, exercise);
        exercise.setOrder(exerciseRepository.getMaxOrderByCourseId(exercise.getCourse().getId()));
        return exerciseRepository.save(exercise);
    }

    @EvictExerciseCache
    @EvictExercisesCache
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
        exercise.setSoundFilename(request.soundFilename());
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
        return exerciseRepository.findProjectedById(exerciseId, ExerciseData.class)
                .orElseThrow(NotFoundException::new);
    }

    @Cacheable(cacheNames = "answers", key = "'answers'.concat(#exerciseId)")
    public ExerciseForGuess getAnswer(Long exerciseId) {
        Exercise exercise = exerciseRepository.findCacheableById(exerciseId)
                .orElseThrow(NotFoundException::new);
        return new ExerciseForGuess(
                exercise.getCorrectAnswer(),
                exercise.getLetter(),
                getCourseId(exercise),
                getCourseName(exercise),
                getLanguageName(exercise)
        );
    }

    private String getLanguageName(Exercise exercise) {
        return exercise.getCourse() != null && exercise.getCourse().getLanguage() != null ? exercise.getCourse().getLanguage().getName() : null;
    }

    private String getCourseName(Exercise exercise) {
        return exercise.getCourse() != null ? exercise.getCourse().getName() : null;
    }

    private Long getCourseId(Exercise exercise) {
        return exercise.getCourse() != null ? exercise.getCourse().getId() : null;
    }
}
