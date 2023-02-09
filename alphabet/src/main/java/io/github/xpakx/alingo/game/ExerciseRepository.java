package io.github.xpakx.alingo.game;

import io.github.xpakx.alingo.game.dto.ExerciseWithOnlyAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ExerciseRepository extends JpaRepository<Exercise, Long> {
    Optional<ExerciseWithOnlyAnswer> findProjectedById(Long id);
}