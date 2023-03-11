package io.github.xpakx.alingo.game;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LanguageRepository extends JpaRepository<Language, Long> {
    List<Language> findBy(Pageable page);

    @Query("select l from Language l where upper(l.name) like upper(?1)")
    List<Language> findByNameLikeIgnoreCase(String name, Pageable pageable);


}