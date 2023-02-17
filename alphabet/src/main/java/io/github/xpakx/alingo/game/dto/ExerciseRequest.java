package io.github.xpakx.alingo.game.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ExerciseRequest(String letter,
                              @NotBlank(message = "Wrong answer must be provided!") String wrongAnswer,
                              @NotBlank(message = "Correct answer must be provided!") String correctAnswer,
                              @NotNull(message = "Exercise must belong to a course!") Long courseId) {
}
