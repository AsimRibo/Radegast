package hr.asimr.radegast.domain.grade;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class StudentProgressDto {

    private int earnedScore;

    private int maximumScore;

    private int percentage;

    private boolean hasGrades;
}
