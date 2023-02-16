package io.github.xpakx.alingo.game.dto;

import io.github.xpakx.alingo.game.Difficulty;

public record CourseRequest(String name,
        String description,
        Difficulty difficulty,
        Long languageId) {

}
