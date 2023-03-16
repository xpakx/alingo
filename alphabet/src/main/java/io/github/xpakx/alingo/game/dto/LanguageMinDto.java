package io.github.xpakx.alingo.game.dto;

import java.io.Serial;
import java.io.Serializable;

public record LanguageMinDto(Long id, String name) implements Serializable {
    @Serial
    private static final long serialVersionUID = 766345643465464643L;

    public static LanguageMinDto of(LanguageMin courseData) {
        return new LanguageMinDto(
                courseData.getId(),
                courseData.getName());
    }
}
