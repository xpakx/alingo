package io.github.xpakx.alingo.game;

import io.github.xpakx.alingo.game.dto.LanguageRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LanguageServiceImpl implements LanguageService {
    private final LanguageRepository languageRepository;

    @Override
    public Language createLanguage(LanguageRequest request) {
        return null;
    }

    @Override
    public Language editLanguage(Long languageId, LanguageRequest request) {
        return null;
    }
}
