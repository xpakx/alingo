package io.github.xpakx.alingo.game.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ExerciseDto {
    Long id;
    List<String> options;
    String soundFilename;
}
