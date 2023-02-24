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
        return createResponse(request, getAnswerForExercise(exerciseId));
    }

    private AnswerResponse createResponse(AnswerRequest request, String answer) {
        return new AnswerResponse(request.answer().equals(answer), answer);
    }

    private String getAnswerForExercise(Long exerciseId) {
        return exerciseRepository.findProjectedById(exerciseId)
                .map(ExerciseWithOnlyAnswer::getCorrectAnswer)
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
