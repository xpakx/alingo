package io.github.xpakx.alingo.game.dto;

import io.github.xpakx.alingo.game.Difficulty;

public interface CourseData {
    Long getId();
    String getName();
    String getDescription();
    boolean isPremium();
    Difficulty getDifficulty();
    LanguageMin getLanguage();
}
