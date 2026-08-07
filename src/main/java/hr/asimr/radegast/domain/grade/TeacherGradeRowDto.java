package hr.asimr.radegast.domain.grade;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TeacherGradeRowDto {

    private Long studentId;

    private String studentName;

    private Integer score;

    private String feedback;
}
