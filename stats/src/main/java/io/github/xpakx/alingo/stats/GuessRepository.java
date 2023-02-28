package io.github.xpakx.alingo.stats;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuessRepository extends JpaRepository<Guess, Long> {
    Page<Guess> findByUsername(String username, Pageable pageable);
}