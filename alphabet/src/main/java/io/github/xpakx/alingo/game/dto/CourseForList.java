package io.github.xpakx.alingo.game.dto;

import io.github.xpakx.alingo.game.Difficulty;

public interface CourseForList {
    Long getId();
    String getName();
    String getDescription();
    boolean isPremium();
    Difficulty getDifficulty();
}
