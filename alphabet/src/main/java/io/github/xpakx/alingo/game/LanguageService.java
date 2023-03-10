package io.github.xpakx.alingo.game;

import io.github.xpakx.alingo.game.dto.CourseData;
import io.github.xpakx.alingo.game.dto.LanguageRequest;
import io.github.xpakx.alingo.game.error.NotFoundException;
import io.github.xpakx.alingo.utils.EvictLanguageCache;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LanguageService {
    private final LanguageRepository languageRepository;
    private final CourseRepository courseRepository;

    public Language createLanguage(LanguageRequest request) {
        Language language = new Language();
        language.setName(request.name());
        return languageRepository.save(language);
    }

    @EvictLanguageCache
    public Language editLanguage(Long languageId, LanguageRequest request) {
        Language language = languageRepository.findById(languageId)
                .orElseThrow(NotFoundException::new);
        language.setName(request.name());
        return languageRepository.save(language);
    }

    public Language getLanguage(Long languageId) {
        return languageRepository.findById(languageId)
                .orElseThrow(NotFoundException::new);
    }


    @Cacheable(cacheNames = "courseListsByLang", key = "'courseListsByLang'.concat(#languageId).concat('_').concat(#page).concat('_').concat(#amount)")
    public List<CourseData> getCourses(Long languageId, Integer page, Integer amount) {
        return courseRepository.findByLanguageId(
                languageId,
                PageRequest.of(
                        page,
                        amount,
                        Sort.by(Sort.Order.asc("id"))
                )
        );
    }

    @Cacheable(cacheNames = "langLists", key = "'langLists'.concat(#page).concat('_').concat(#amount)")
    public List<Language> getLanguages(Integer page, Integer amount) {
        return languageRepository.findBy(
                PageRequest.of(
                        page,
                        amount,
                        Sort.by(Sort.Order.asc("id"))
                )
        );
    }
}
