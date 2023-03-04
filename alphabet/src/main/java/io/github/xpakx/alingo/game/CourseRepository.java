package io.github.xpakx.alingo.game;

import io.github.xpakx.alingo.game.dto.CourseData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {
    @Query("select (count(c) > 0) from Course c where c.id = ?1 and c.premium = true")
    boolean isPremium(Long id);

    Optional<CourseData> findProjectedById(Long courseId);
}