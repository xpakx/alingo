package io.github.xpakx.alingo.game;

import io.github.xpakx.alingo.game.dto.LanguageRequest;
import io.github.xpakx.alingo.game.error.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LanguageServiceImpl implements LanguageService {
    private final LanguageRepository languageRepository;

    @Override
    public Language createLanguage(LanguageRequest request) {
        Language language = new Language();
        language.setName(request.getName());
        return languageRepository.save(language);
    }

    @Override
    public Language editLanguage(Long languageId, LanguageRequest request) {
        Language language = languageRepository.findById(languageId)
                .orElseThrow(NotFoundException::new);
        language.setName(request.getName());
        return languageRepository.save(language);
    }
}
