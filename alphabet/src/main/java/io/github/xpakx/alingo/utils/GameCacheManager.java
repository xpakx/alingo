package io.github.xpakx.alingo.utils;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Component;

@Component
public class GameCacheManager {
    @CacheEvict(cacheNames = "answers", key = "'answers'.concat(#id)")
    public void invalidateExerciseCache(Long id) {

    }

    @CacheEvict(cacheNames = "courses", key = "'courses'.concat(#id)")
    public void invalidateCourseCache(Long id) {

    }

    @CacheEvict(cacheNames = "languageLists", allEntries = true)
    public void invalidateLanguageListsCache() {

    }

    @CacheEvict(cacheNames = "exercises", allEntries = true)
    public void invalidateExerciseListsCache() {

    }

    @CacheEvict(cacheNames = {"courseListsByLang", "courseLists"}, allEntries = true)
    public void invalidateCourseListsCache() {

    }
}
