package io.github.xpakx.alingo.game.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CourseExercisesRequest {
    @Min(value = 1)
    @NotNull
    Long start;

    @NotNull
    @Min(value = 1)
    @Max(value = 20)
    Integer amount;
}
