package io.github.xpakx.alingo.game.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LanguageRequest {
    @NotBlank(message = "Language name cannot be empty")
    private String name;
}
