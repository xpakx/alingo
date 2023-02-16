package io.github.xpakx.alingo.game.dto;

import io.github.xpakx.alingo.game.Difficulty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CourseRequest {
    private String name;
    private String description;
    private Difficulty difficulty;
    private Long languageId;
}
