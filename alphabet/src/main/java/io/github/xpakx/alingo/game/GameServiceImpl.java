package io.github.xpakx.alingo.game;

import io.github.xpakx.alingo.game.dto.*;
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
    
    @Override
    public ExercisesResponse getExercisesForCourse(Long courseId, CourseExercisesRequest request) {
        return null;
    }
}
