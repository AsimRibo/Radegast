package hr.asimr.radegast.domain.grade;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GradeFormDto {

    private Long studentId;

    private Integer score;

    private String feedback;
}
