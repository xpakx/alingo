package io.github.xpakx.alingo.game.dto;

import io.github.xpakx.alingo.game.Difficulty;

import java.io.Serial;
import java.io.Serializable;

public record CourseDataDto(
        Long id,
        String name,
        String description,
        boolean premium,
        Difficulty difficulty,
        LanguageMinDto language) implements Serializable {
    @Serial
    private static final long serialVersionUID = 235909352343234015L;

    public static CourseDataDto of(CourseData courseData) {
        return new CourseDataDto(
                courseData.getId(),
                courseData.getName(),
                courseData.getDescription(),
                courseData.isPremium(),
                courseData.getDifficulty(),
                courseData.getLanguage() != null ? LanguageMinDto.of(courseData.getLanguage()): null);
    }
}
