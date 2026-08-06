package hr.asimr.radegast.domain.admin;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Getter
@Setter
public class CourseEnrolledStudentDto {

    private final Long id;

    private final String name;

    private final String email;

    private final LocalDateTime enrolledAt;
}
