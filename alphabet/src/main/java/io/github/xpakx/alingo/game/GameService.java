package io.github.xpakx.alingo.game;

import io.github.xpakx.alingo.clients.PublishGuess;
import io.github.xpakx.alingo.game.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
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
    private final CourseRepository courseRepository;

    @PublishGuess
    public AnswerResponse checkAnswer(Long exerciseId, AnswerRequest request) {
        return createResponse(request, exerciseService.getAnswer(exerciseId));
    }

    private AnswerResponse createResponse(AnswerRequest request, ExerciseForGuess exercise) {
        return new AnswerResponse(
                request.answer().equals(exercise.correctAnswer()),
                exercise.correctAnswer(),
                exercise.letter(),
                exercise.courseId(),
                exercise.courseName(),
                exercise.language()
        );
    }

    @Cacheable(cacheNames = "exercises", key = "'exercises'.concat(#courseId).concat('_').concat(#page).concat('_').concat(#amount)", unless = "#result.size == 0")
    public ExercisesResponse getExercisesForCourse(Long courseId, Integer page, Integer amount) {
        Page<Exercise> result = exerciseRepository.findByCourseId(courseId, toPageRequest(page, amount));
        return exerciseService.prepareResponse(result, new Random(), courseRepository.isPremium(courseId));
    }

    private PageRequest toPageRequest(Integer page, Integer amount) {
        return PageRequest.of(page, amount, Sort.by(Sort.Order.asc("order")).and(Sort.by(Sort.Order.asc("id"))));
    }
}
