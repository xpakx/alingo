package io.github.xpakx.alingo.game;

import io.github.xpakx.alingo.game.dto.CourseDataDto;
import io.github.xpakx.alingo.game.dto.CourseRequest;
import io.github.xpakx.alingo.game.error.NotFoundException;
import io.github.xpakx.alingo.utils.EvictCourseCache;
import io.github.xpakx.alingo.utils.EvictCoursesCache;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseService {
    private final CourseRepository courseRepository;
    private final LanguageRepository languageRepository;

    @EvictCoursesCache
    public Course createCourse(CourseRequest request) {
        Course course = new Course();
        copyFieldsToCourse(request, course);
        return courseRepository.save(course);
    }

    @EvictCourseCache
    @EvictCoursesCache
    public Course editCourse(Long courseId, CourseRequest request) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(NotFoundException::new);
        copyFieldsToCourse(request, course);
        return courseRepository.save(course);
    }

    private void copyFieldsToCourse(CourseRequest request, Course course) {
        course.setName(request.name());
        course.setDescription(request.description());
        course.setDifficulty(request.difficulty());
        course.setPremium(request.premium());
        if(request.languageId() != null) {
            course.setLanguage(languageRepository.getReferenceById(request.languageId()));
        } else {
            course.setLanguage(null);
        }
    }

    @Cacheable(cacheNames = "courses", key = "'courses'.concat(#courseId)")
    public CourseDataDto getCourse(Long courseId) {
        return CourseDataDto.of(
                courseRepository.findProjectedById(courseId)
                .orElseThrow(NotFoundException::new)
        );
    }

    @Cacheable(cacheNames = "courseLists", key = "'courseLists'.concat(#page).concat('_').concat(#amount)", unless = "#result.size() == 0")
    public List<CourseDataDto> getCourses(Integer page, Integer amount) {
        return courseRepository.findListBy(createPageRequestSortedById(page, amount)).stream()
                .map(CourseDataDto::of)
                .toList();
    }

    private static PageRequest createPageRequestSortedById(Integer page, Integer amount) {
        return PageRequest.of(
                page,
                amount,
                Sort.by(Sort.Order.asc("id"))
        );
    }

    public List<CourseDataDto> findCourses(String name, Integer page, Integer amount) {
        return courseRepository.findByNameLikeIgnoreCase(
                name,
                createPageRequestSortedById(page, amount)
        ).stream()
                .map(CourseDataDto::of)
                .toList();
    }
}
