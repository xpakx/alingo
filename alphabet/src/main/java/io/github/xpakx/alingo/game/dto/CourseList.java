package io.github.xpakx.alingo.game.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public record CourseList(List<CourseForListDto> courses,
                         Integer page,
                         Long size,
                         Long totalSize) {

    public static CourseList of(Page<CourseForList> courses) {
        return new CourseList(
                courses.stream().map(CourseForListDto::of).toList(),
                courses.getNumber()+1,
                (long) courses.getSize(),
                courses.getTotalElements()
        );
    }
}
