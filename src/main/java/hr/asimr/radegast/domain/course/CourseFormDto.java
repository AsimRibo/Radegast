package hr.asimr.radegast.domain.course;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CourseFormDto {

    @NotBlank(message = "Course code is required.")
    @Size(
            max = 30,
            message = "Course code must not exceed 30 characters."
    )
    private String code;

    @NotBlank(message = "Course name is required.")
    @Size(
            max = 150,
            message = "Course name must not exceed 150 characters."
    )
    private String name;

    private String description;

    @Positive(message = "Capacity must be greater than zero.")
    private Integer capacity;

    private boolean enrollmentOpen = false;
}
