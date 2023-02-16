package io.github.xpakx.alingo.game;

import io.github.xpakx.alingo.game.dto.CourseRequest;
import io.github.xpakx.alingo.game.dto.LanguageRequest;
import io.github.xpakx.alingo.game.error.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CourseService {
    private final CourseRepository courseRepository;
    private final LanguageRepository languageRepository;

    public Course createCourse(CourseRequest request) {
        Course course = new Course();
        copyFieldsToCourse(request, course);
        return courseRepository.save(course);
    }

    public Course editCourse(Long languageId, CourseRequest request) {
        Course course = courseRepository.findById(languageId)
                .orElseThrow(NotFoundException::new);
        copyFieldsToCourse(request, course);
        return courseRepository.save(course);
    }

    private void copyFieldsToCourse(CourseRequest request, Course course) {
        course.setName(request.name());
        course.setDescription(request.description());
        course.setDifficulty(request.difficulty());
        if(request.languageId() != null) {
            course.setLanguage(languageRepository.getReferenceById(request.languageId()));
        } else {
            course.setLanguage(null);
        }
    }
}
