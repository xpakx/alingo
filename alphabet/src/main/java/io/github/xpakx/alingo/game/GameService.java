package io.github.xpakx.alingo.game;

import io.github.xpakx.alingo.clients.PublishGuess;
import io.github.xpakx.alingo.game.dto.*;
import io.github.xpakx.alingo.game.error.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@RequiredArgsConstructor
public class GameService {
    private final ExerciseRepository exerciseRepository;
    private final ExerciseService exerciseService;

    @PublishGuess
    public AnswerResponse checkAnswer(Long exerciseId, AnswerRequest request) {
        return createResponse(request, getExercise(exerciseId));
    }

    private AnswerResponse createResponse(AnswerRequest request, Exercise exercise) {
        return new AnswerResponse(
                request.answer().equals(exercise.getCorrectAnswer()),
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

    private Exercise getExercise(Long exerciseId) {
        return exerciseRepository.findById(exerciseId)
                .orElseThrow(NotFoundException::new);
    }

    public ExercisesResponse getExercisesForCourse(Long courseId, Integer page, Integer amount) {
        Page<Exercise> result = exerciseRepository.findByCourseId(courseId, toPageRequest(page, amount));
        return exerciseService.prepareResponse(result, new Random());
    }

    private PageRequest toPageRequest(Integer page, Integer amount) {
        return PageRequest.of(page, amount, Sort.by(Sort.Order.asc("order")).and(Sort.by(Sort.Order.asc("id"))));
    }
}
