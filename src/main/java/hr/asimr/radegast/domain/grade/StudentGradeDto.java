package hr.asimr.radegast.domain.grade;

import hr.asimr.radegast.data.entities.enums.AssessmentType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class StudentGradeDto {

    private Long assessmentId;

    private String assessmentTitle;

    private AssessmentType assessmentType;

    private Integer maximumScore;

    private LocalDateTime dueAt;

    private Integer score;

    private String feedback;
}
