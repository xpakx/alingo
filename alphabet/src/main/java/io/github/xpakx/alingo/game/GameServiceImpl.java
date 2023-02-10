package io.github.xpakx.alingo.game;

import io.github.xpakx.alingo.game.dto.*;
import io.github.xpakx.alingo.game.error.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GameServiceImpl implements GameService {
    private final ExerciseRepository exerciseRepository;
    private final ExerciseService exerciseService;

    @Override
    public AnswerResponse checkAnswer(Long exerciseId, String guess) {
        return createResponse(guess, getAnswerForExercise(exerciseId));
    }

    private AnswerResponse createResponse(String guess, String answer) {
        AnswerResponse response = new AnswerResponse();
        response.setCorrectAnswer(answer);
        response.setCorrect(guess.equals(answer));
        return response;
    }

    private String getAnswerForExercise(Long exerciseId) {
        return exerciseRepository.findProjectedById(exerciseId)
                .map(ExerciseWithOnlyAnswer::getCorrectAnswer)
                .orElseThrow(NotFoundException::new);
    }

    @Override
    public ExercisesResponse getExercisesForCourse(Long courseId, Integer page, Integer amount) {
        Page<Exercise> result = exerciseRepository.findByCourseId(courseId, toPageRequest(page, amount));
        return exerciseService.prepareResponse(result);
    }

    private PageRequest toPageRequest(Integer page, Integer amount) {
        return PageRequest.of(page, amount, Sort.by(Sort.Order.asc("id")));
    }
}
