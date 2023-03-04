package io.github.xpakx.alingo.game;

import io.github.xpakx.alingo.game.dto.CourseData;
import io.github.xpakx.alingo.game.dto.CourseRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

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
}
