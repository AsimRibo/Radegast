package hr.asimr.radegast.domain.assessment;

import hr.asimr.radegast.data.entities.enums.AssessmentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Getter
@Setter
public class AssessmentFormDto {

    @NotBlank
    @Size(max = 200)
    private String title;

    private String description;

    @NotNull
    private AssessmentType assessmentType;

    @NotNull
    @Positive
    private Integer maximumScore;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime dueAt;
}
