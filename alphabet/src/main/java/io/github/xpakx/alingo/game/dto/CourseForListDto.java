package io.github.xpakx.alingo.game.dto;

import io.github.xpakx.alingo.game.Difficulty;

public record CourseForListDto(
        Long id,
        String name,
        String description,
        boolean premium,
        Difficulty difficulty) {

    public static CourseForListDto of(CourseForList courseData) {
        return new CourseForListDto(
                courseData.getId(),
                courseData.getName(),
                courseData.getDescription(),
                courseData.isPremium(),
                courseData.getDifficulty());
    }
}
