package io.github.xpakx.alingo.game.graphql;

import io.github.xpakx.alingo.game.Course;
import io.github.xpakx.alingo.game.CourseService;
import io.github.xpakx.alingo.game.Difficulty;
import io.github.xpakx.alingo.game.dto.CourseRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;

@Controller
@RequiredArgsConstructor
@Validated
public class GraphCourseController {
    private final CourseService service;

    @MutationMapping
    @Secured("MODERATOR")
    public Course addCourse(@NotBlank(message = "Course name cannot be empty!") @Argument String name,
                                @Argument String description,
                                @Argument Difficulty difficulty,
                                @Argument Long languageId) {
        return service.createCourse(toRequest(name, description, difficulty, languageId));
    }

    @MutationMapping
    @Secured("MODERATOR")
    public Course editCourse(@NotNull @Argument Long courseId,
                                 @NotBlank(message = "Course name cannot be empty!") @Argument String name,
                                 @Argument String description,
                                 @Argument Difficulty difficulty,
                                 @Argument Long languageId) {
        return service.editCourse(courseId, toRequest(name, description, difficulty, languageId));
    }

    private CourseRequest toRequest(String name, String description, Difficulty difficulty, Long languageId) {
        return new CourseRequest(name, description, difficulty, languageId);
    }
}
