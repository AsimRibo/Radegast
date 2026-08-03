package hr.asimr.radegast.domain.course;

import hr.asimr.radegast.data.entities.Course;
import hr.asimr.radegast.data.entities.enums.CourseStatus;

public record CourseListDto(
        Long id,
        String code,
        String name,
        String description,
        String teacherName,
        Integer capacity,
        boolean enrollmentOpen,
        CourseStatus status
) {

    public static CourseListDto from(Course course) {
        String teacherName = null;

        if (course.getTeacher() != null) {
            teacherName = course.getTeacher().getFirstName()
                          + " "
                          + course.getTeacher().getLastName();
        }

        return new CourseListDto(
                course.getId(),
                course.getCode(),
                course.getName(),
                course.getDescription(),
                teacherName,
                course.getCapacity(),
                course.isEnrollmentOpen(),
                course.getStatus()
        );
    }
}
