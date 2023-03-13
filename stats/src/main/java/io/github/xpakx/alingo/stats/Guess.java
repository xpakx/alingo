package io.github.xpakx.alingo.stats;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Guess {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private boolean correct;
    private String letter;
    private Long exerciseId;
    private Long courseId;
    private String courseName;
    private String language;
    private LocalDateTime time;
}
