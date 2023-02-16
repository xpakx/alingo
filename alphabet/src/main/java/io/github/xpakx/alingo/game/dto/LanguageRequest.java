package io.github.xpakx.alingo.game.dto;

import jakarta.validation.constraints.NotBlank;

public record LanguageRequest(@NotBlank(message = "Language name cannot be empty") String name) {
}
