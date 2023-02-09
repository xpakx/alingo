package io.github.xpakx.alingo.game;

import io.github.xpakx.alingo.game.dto.AnswerRequest;
import io.github.xpakx.alingo.game.dto.AnswerResponse;
import io.github.xpakx.alingo.game.dto.ExerciseWithOnlyAnswer;
import io.github.xpakx.alingo.game.error.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GameServiceImpl implements GameService {
    private final ExerciseRepository exerciseRepository;
    @Override
    public AnswerResponse checkAnswer(Long exerciseId, AnswerRequest request) {
        return createResponse(request, getAnswerForExercise(exerciseId));
    }

    private AnswerResponse createResponse(AnswerRequest request, String answer) {
        AnswerResponse response = new AnswerResponse();
        response.setCorrectAnswer(answer);
        response.setCorrect(request.getAnswer().equals(answer));
        return response;
    }

    private String getAnswerForExercise(Long exerciseId) {
        return exerciseRepository.findProjectedById(exerciseId)
                .map(ExerciseWithOnlyAnswer::getCorrectAnswer)
                .orElseThrow(NotFoundException::new);
    }
}
