package hr.asimr.radegast.domain.course;

import hr.asimr.radegast.data.entities.CourseSession;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.List;

@RequiredArgsConstructor
@Getter
@Setter
public class StudentCourseDetailsDto {

    private final Long id;

    private final String code;

    private final String name;

    private final String description;

    private final String teacherName;

    private final List<CourseSession> sessions;
}
