package io.github.xpakx.alingo.game;

import io.github.xpakx.alingo.game.dto.LanguageRequest;

public interface LanguageService {
    Language createLanguage(LanguageRequest request);
    Language editLanguage(Long languageId, LanguageRequest request);
}
