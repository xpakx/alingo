package io.github.xpakx.alingo.game;

import io.github.xpakx.alingo.game.dto.CourseData;
import io.github.xpakx.alingo.game.dto.CourseForList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {
    @Query("select (count(c) > 0) from Course c where c.id = ?1 and c.premium = true")
    boolean isPremium(Long id);

    Optional<CourseData> findProjectedById(Long courseId);

    Page<CourseForList> findByLanguageId(Long languageId, Pageable page);

    Page<CourseForList> findListBy(Pageable page);

    @Query("select c from Course c where upper(c.name) like upper(?1)")
    Page<CourseForList> findByNameLikeIgnoreCase(String name, Pageable pageable);
}