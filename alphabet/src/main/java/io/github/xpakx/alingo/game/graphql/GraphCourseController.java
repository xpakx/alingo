package io.github.xpakx.alingo.game.graphql;

import io.github.xpakx.alingo.game.Course;
import io.github.xpakx.alingo.game.CourseService;
import io.github.xpakx.alingo.game.Difficulty;
import io.github.xpakx.alingo.game.dto.CourseDataDto;
import io.github.xpakx.alingo.game.dto.CourseRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
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
                                @Argument Long languageId,
                                @Argument boolean premium) {
        return service.createCourse(toRequest(name, description, difficulty, languageId, premium));
    }

    @MutationMapping
    @Secured("MODERATOR")
    public Course editCourse(@NotNull(message = "Course id must be provided!") @Argument Long courseId,
                                 @NotBlank(message = "Course name cannot be empty!") @Argument String name,
                                 @Argument String description,
                                 @Argument Difficulty difficulty,
                                 @Argument Long languageId,
                                @Argument boolean premium) {
        return service.editCourse(courseId, toRequest(name, description, difficulty, languageId, premium));
    }

    @QueryMapping
    @Secured("MODERATOR")
    public CourseDataDto getCourse(@NotNull(message = "Course id must be provided!") @Argument Long id) {
        return service.getCourse(id);
    }

    private CourseRequest toRequest(String name, String description, Difficulty difficulty, Long languageId, boolean premium) {
        return new CourseRequest(name, description, difficulty, languageId, premium);
    }
}
