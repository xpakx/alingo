package io.github.xpakx.alingo.game;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CourseRepository extends JpaRepository<Course, Long> {
    @Query("select (count(c) > 0) from Course c where c.id = ?1 and c.premium = true")
    boolean isPremium(Long id);

}