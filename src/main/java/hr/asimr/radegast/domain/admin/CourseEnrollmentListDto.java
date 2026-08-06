package hr.asimr.radegast.domain.admin;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.List;

@RequiredArgsConstructor
@Getter
@Setter
public class CourseEnrollmentListDto {

    private final Long id;

    private final String code;

    private final String name;

    private final String teacherName;

    private final Integer capacity;

    private final long enrolledCount;

    private final boolean administrator;

    private final boolean acceptingEnrollments;

    private final List<CourseEnrolledStudentDto> enrolledStudents;

    private final List<CourseEnrollmentCandidateDto> availableStudents;
}
