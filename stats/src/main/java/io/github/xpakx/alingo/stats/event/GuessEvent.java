package io.github.xpakx.alingo.stats.event;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class GuessEvent {
    private String username;
    private Long exerciseId;
    private boolean correct;
    private String letter;
    private Long courseId;
    private String courseName;
    private String language;
    private LocalDateTime time;
}
