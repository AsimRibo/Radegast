package hr.asimr.radegast.domain.admin;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@Getter
@Setter
public class CourseEnrollmentCandidateDto {

    private final Long id;

    private final String name;

    private final String email;
}
