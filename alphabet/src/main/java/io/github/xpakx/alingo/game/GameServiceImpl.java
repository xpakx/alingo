package io.github.xpakx.alingo.game;

import io.github.xpakx.alingo.game.dto.AnswerRequest;
import io.github.xpakx.alingo.game.dto.AnswerResponse;
import io.github.xpakx.alingo.game.dto.ExerciseWithOnlyAnswer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GameServiceImpl implements GameService {
    private final ExerciseRepository exerciseRepository;
    @Override
    public AnswerResponse checkAnswer(Long exerciseId, AnswerRequest request) {
        String answer = exerciseRepository.findProjectedById(exerciseId)
                .map(ExerciseWithOnlyAnswer::getCorrectAnswer)
                .orElseThrow();
        AnswerResponse response = new AnswerResponse();
        response.setCorrectAnswer(answer);
        response.setCorrect(request.getAnswer().equals(answer));
        return response;
    }
}
