package io.github.xpakx.alingo.game;

import io.github.xpakx.alingo.game.dto.CourseData;
import io.github.xpakx.alingo.game.dto.CourseRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/course")
public class CourseController {
    private final CourseService service;

    @PostMapping
    @Secured("MODERATOR")
    public ResponseEntity<Course> createCourse(@Valid @RequestBody CourseRequest request) {
        return new ResponseEntity<>(
                service.createCourse(request),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/{courseId}")
    @Secured("MODERATOR")
    @ResponseBody
    public Course editCourse(@Valid @RequestBody CourseRequest request, @PathVariable Long courseId) {
        return service.editCourse(courseId, request);
    }

    @GetMapping("/{courseId}")
    @Secured("MODERATOR")
    @ResponseBody
    public CourseData getCourse(@PathVariable Long courseId) {
        return service.getCourse(courseId);
    }
    @GetMapping("/all")
    @Secured("MODERATOR")
    @ResponseBody
    public List<CourseData> getCourses(@RequestParam Integer page, @RequestParam Integer amount) {
        return service.getCourses(page, amount);
    }

    @GetMapping("/byName/{name}")
    @Secured("MODERATOR")
    @ResponseBody
    public List<CourseData> getCoursesByName(@PathVariable @NotBlank String name,
                                             @RequestParam @Min(value = 1, message = "Page must be positive") @NotNull(message = "Page cannot be null") Integer page,
                                             @RequestParam @NotNull @Min(value = 1, message = "Amount must be between 1 and 20") @Max(value = 20, message = "Amount must be between 1 and 20") Integer amount) {
        return service.findCourses(name, page-1, amount);
    }
}
