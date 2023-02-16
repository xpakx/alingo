package io.github.xpakx.alingo.game;

import io.github.xpakx.alingo.game.dto.CourseRequest;
import io.github.xpakx.alingo.game.dto.LanguageRequest;
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
                HttpStatus.OK
        );
    }

    @PutMapping("/{courseId}")
    @Secured("MODERATOR")
    public ResponseEntity<Course> editCourse(@Valid @RequestBody CourseRequest request, @PathVariable Long courseId) {
        return new ResponseEntity<>(
                service.editCourse(courseId, request),
                HttpStatus.OK
        );
    }
}
