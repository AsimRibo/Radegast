package hr.asimr.radegast.domain.course;

import hr.asimr.radegast.data.entities.Course;
import hr.asimr.radegast.data.entities.enums.CourseStatus;

public record CourseSessionCourseDto(
        Long id,
        String code,
        String name,
        CourseStatus status
) {

    public static CourseSessionCourseDto from(
            Course course
    ) {
        return new CourseSessionCourseDto(
                course.getId(),
                course.getCode(),
                course.getName(),
                course.getStatus()
        );
    }
}
