package io.github.xpakx.alingo.game;

import io.github.xpakx.alingo.game.dto.*;
import io.github.xpakx.alingo.game.error.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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
        Page<Exercise> page = exerciseRepository.findByCourseId(
                courseId,
                PageRequest.of(request.getPage(),
                        request.getAmount(),
                        Sort.by(Sort.Order.asc("id")))
        );
        ExercisesResponse response = new ExercisesResponse();
        response.setPage(page.getNumber());
        response.setSize(page.getTotalElements());
        response.setExercises(page.getContent().stream().map(this::toDto).toList());
        return response;
    }

    private ExerciseDto toDto(Exercise exercise) {
        ExerciseDto dto = new ExerciseDto();
        dto.setId(exercise.getId());
        List<String> options = List.of(exercise.getCorrectAnswer(), exercise.getWrongAnswer());
        dto.setOptions(options);
        return dto;
    }
}
