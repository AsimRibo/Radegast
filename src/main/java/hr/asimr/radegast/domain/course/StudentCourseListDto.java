package hr.asimr.radegast.domain.course;

import hr.asimr.radegast.data.entities.enums.EnrollmentStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@Getter
@Setter
public class StudentCourseListDto {

    private final Long id;

    private final String code;

    private final String name;

    private final String description;

    private final String teacherName;

    private final Integer capacity;

    private final long enrolledCount;

    private final boolean enrollmentOpen;

    private final EnrollmentStatus enrollmentStatus;

    private final boolean full;

    public boolean isEnrolled() {
        return enrollmentStatus == EnrollmentStatus.ENROLLED;
    }

    public boolean isWithdrawn() {
        return enrollmentStatus == EnrollmentStatus.WITHDRAWN;
    }

    public boolean isCompleted() {
        return enrollmentStatus == EnrollmentStatus.COMPLETED;
    }

    public boolean isCanEnroll() {
        return enrollmentOpen
               && !full
               && enrollmentStatus != EnrollmentStatus.ENROLLED
               && enrollmentStatus != EnrollmentStatus.COMPLETED;
    }

    public boolean isCanWithdraw() {
        return enrollmentStatus == EnrollmentStatus.ENROLLED;
    }
}
