package io.github.xpakx.alingo.utils;

import io.github.xpakx.alingo.game.Exercise;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.AfterReturning;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CacheAspect {
    private final GameCacheManager cache;

    @AfterReturning(value="@annotation(EvictExerciseCache)", returning = "response")
    public void publishGuess(Exercise response) throws Throwable {
        cache.invalidateExerciseCache(response.getId());
    }
}
