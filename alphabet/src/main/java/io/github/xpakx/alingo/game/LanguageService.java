package io.github.xpakx.alingo.game;

import io.github.xpakx.alingo.game.dto.CourseDataDto;
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


    @Cacheable(cacheNames = "courseListsByLang", key = "'courseListsByLang'.concat(#languageId).concat('_').concat(#page).concat('_').concat(#amount)", unless = "#result.size() == 0")
    public List<CourseDataDto> getCourses(Long languageId, Integer page, Integer amount) {
        return courseRepository.findByLanguageId(
                languageId,
                createPageRequestSortedById(page, amount)
        ).stream()
                .map(CourseDataDto::of)
                .toList();
    }

    private static PageRequest createPageRequestSortedById(Integer page, Integer amount) {
        return PageRequest.of(
                page,
                amount,
                Sort.by(Sort.Order.asc("id"))
        );
    }

    @Cacheable(cacheNames = "langLists", key = "'langLists'.concat(#page).concat('_').concat(#amount)", unless = "#result.size() == 0")
    public List<Language> getLanguages(Integer page, Integer amount) {
        return languageRepository.findBy(
                createPageRequestSortedById(page, amount)
        );
    }

    public List<Language> findLanguages(String name, Integer page, Integer amount) {
        return languageRepository.findByNameLikeIgnoreCase(
                name,
                createPageRequestSortedById(page, amount)
        );
    }
}
