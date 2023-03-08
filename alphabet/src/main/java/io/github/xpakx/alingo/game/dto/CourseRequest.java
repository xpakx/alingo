package io.github.xpakx.alingo.game.dto;

import io.github.xpakx.alingo.game.Difficulty;
import jakarta.validation.constraints.NotBlank;

public record CourseRequest(@NotBlank(message = "Course name cannot be empty!") String name,
        String description,
        Difficulty difficulty,
        Long languageId,
        boolean premium) {

}
