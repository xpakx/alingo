package io.github.xpakx.alingo.game.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnswerRequest {
    @NotBlank(message = "Guess cannot be empty")
    private String answer;
}
