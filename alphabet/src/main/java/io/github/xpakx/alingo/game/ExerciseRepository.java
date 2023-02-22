package io.github.xpakx.alingo.game;

import io.github.xpakx.alingo.game.dto.ExerciseWithOnlyAnswer;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ExerciseRepository extends JpaRepository<Exercise, Long> {

    @Cacheable(cacheNames = "answers", key = "'answers'.concat(#id)")
    Optional<ExerciseWithOnlyAnswer> findProjectedById(Long id);
    Page<Exercise> findByCourseId(Long courseId, Pageable pageable);
    @Query("SELECT coalesce(max(ex.order), 0) FROM Exercise ex WHERE ex.course.id = :courseId")
    Integer getMaxOrderByCourseId(Long courseId);
}