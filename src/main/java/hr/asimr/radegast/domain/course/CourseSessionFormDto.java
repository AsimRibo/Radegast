package hr.asimr.radegast.domain.course;

import hr.asimr.radegast.data.entities.CourseSession;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class CourseSessionFormDto {

    @NotNull(message = "Start time is required.")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime startsAt;

    @NotNull(message = "End time is required.")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime endsAt;

    @Size(
            max = 255,
            message = "Location must not exceed 255 characters."
    )
    private String location;

    @Size(
            max = 255,
            message = "Topic must not exceed 255 characters."
    )
    private String topic;

    public static CourseSessionFormDto from(CourseSession courseSession) {
        CourseSessionFormDto form = new CourseSessionFormDto();

        form.setStartsAt(courseSession.getStartsAt());
        form.setEndsAt(courseSession.getEndsAt());
        form.setLocation(courseSession.getLocation());
        form.setTopic(courseSession.getTopic());

        return form;
    }

    public boolean hasValidTimeRange() {
        if (startsAt == null || endsAt == null) {
            return true;
        }

        return endsAt.isAfter(startsAt);
    }
}
