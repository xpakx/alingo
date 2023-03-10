package io.github.xpakx.alingo.utils;

import io.github.xpakx.alingo.game.Course;
import io.github.xpakx.alingo.game.Exercise;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.AfterReturning;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CacheAspect {
    private final GameCacheManager cache;

    @AfterReturning(value="@annotation(EvictExerciseCache)", returning = "response")
    public void evictExercise(Exercise response) {
        cache.invalidateExerciseCache(response.getId());
    }
    @AfterReturning(value="@annotation(EvictCourseCache)", returning = "response")
    public void evictCourse(Course response) {
        cache.invalidateCourseCache(response.getId());
        cache.invalidateCourseListsCache(); // TODO ? 
    }
}
