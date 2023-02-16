package io.github.xpakx.alingo.game;

import io.github.xpakx.alingo.game.dto.LanguageRequest;
import io.github.xpakx.alingo.game.error.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LanguageService {
    private final LanguageRepository languageRepository;

    public Language createLanguage(LanguageRequest request) {
        Language language = new Language();
        language.setName(request.name());
        return languageRepository.save(language);
    }

    public Language editLanguage(Long languageId, LanguageRequest request) {
        Language language = languageRepository.findById(languageId)
                .orElseThrow(NotFoundException::new);
        language.setName(request.name());
        return languageRepository.save(language);
    }
}
