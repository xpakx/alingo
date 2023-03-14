package io.github.xpakx.alingo.game.dto;

import java.io.Serial;
import java.io.Serializable;

public record ExerciseForGuess(String correctAnswer,
                               String letter,
                               Long courseId,
                               String courseName,
                               String language) implements Serializable {
    @Serial
    private static final long serialVersionUID = 594398435388882315L;
}
