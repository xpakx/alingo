package io.github.xpakx.alingo.game.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

public record AnswerResponse(
        boolean correct,
        String correctAnswer,
        @JsonIgnore String letter,
        @JsonIgnore Long courseId,
        @JsonIgnore String courseName,
        @JsonIgnore String language) {

}
