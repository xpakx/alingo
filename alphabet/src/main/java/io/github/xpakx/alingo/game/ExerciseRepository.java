package io.github.xpakx.alingo.game;

import io.github.xpakx.alingo.game.dto.ExerciseWithOnlyAnswer;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ExerciseRepository extends JpaRepository<Exercise, Long> {

    @Cacheable(cacheNames = "answers", key = "'answers'.concat(#id)")
    Optional<ExerciseWithOnlyAnswer> findProjectedById(Long id);
}