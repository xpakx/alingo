package io.github.xpakx.alingo.game;

import io.github.xpakx.alingo.game.dto.AnswerRequest;
import io.github.xpakx.alingo.game.dto.AnswerResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GameServiceImpl implements GameService {
    private final ExerciseRepository exerciseRepository;
    @Override
    public AnswerResponse checkAnswer(Long exerciseId, AnswerRequest request) {
        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow();
        AnswerResponse response = new AnswerResponse();
        response.setCorrectAnswer(exercise.getCorrectAnswer());
        if(request.getAnswer().equals(exercise.getCorrectAnswer())) {
            response.setCorrect(true);
        }
        return response;
    }
}
