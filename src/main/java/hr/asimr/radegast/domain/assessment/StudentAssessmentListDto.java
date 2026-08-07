package hr.asimr.radegast.domain.assessment;

import hr.asimr.radegast.data.entities.enums.AssessmentType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StudentAssessmentListDto {

    private Long id;

    private String title;

    private String description;

    private AssessmentType assessmentType;

    private Integer maximumScore;

    private LocalDateTime dueAt;
}
