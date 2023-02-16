package io.github.xpakx.alingo.game.dto;

import jakarta.validation.constraints.NotBlank;

public record AnswerRequest(@NotBlank(message = "Guess cannot be empty") String answer) {

}
