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
        Page<Exercise> page = exerciseRepository.findByCourseId(courseId, toPageRequest(request));
        return exerciseService.prepareResponse(page);
    }

    private PageRequest toPageRequest(CourseExercisesRequest request) {
        return PageRequest.of(request.getPage(), request.getAmount(), Sort.by(Sort.Order.asc("id")));
    }
}
