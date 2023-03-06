package io.github.xpakx.alingo.game;

import io.github.xpakx.alingo.game.dto.ExerciseWithOnlyAnswer;
import jakarta.transaction.Transactional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ExerciseRepository extends JpaRepository<Exercise, Long> {

    @Cacheable(cacheNames = "answers", key = "'answers'.concat(#id)")
    Optional<Exercise> findCacheableById(Long id);
    Page<Exercise> findByCourseId(Long courseId, Pageable pageable);
    @Query("SELECT coalesce(max(ex.order), 0) FROM Exercise ex WHERE ex.course.id = :courseId")
    Integer getMaxOrderByCourseId(Long courseId);

    @Modifying
    @Transactional
    @Query("UPDATE Exercise ex SET ex.order = ex.order + 1 WHERE ex.course.id = :courseId AND ex.order > :orderStart AND ex.order < :orderEnd")
    void incrementOrderBetween(Long courseId, Integer orderStart, Integer orderEnd);

    @Modifying
    @Transactional
    @Query("UPDATE Exercise ex SET ex.order = ex.order - 1 WHERE ex.course.id = :courseId AND ex.order > :orderStart AND ex.order < :orderEnd")
    void decrementOrderBetween(Long courseId, Integer orderStart, Integer orderEnd);

    <T> Optional<T> findProjectedById(Long id, Class<T> type);
}