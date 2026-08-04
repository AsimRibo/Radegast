package hr.asimr.radegast.domain.course;

import hr.asimr.radegast.data.entities.CourseSession;

import java.time.format.DateTimeFormatter;

public record  CourseSessionListDto(
        Long id,
        String topic,
        String location,
        String startsAt,
        String endsAt,
        boolean cancelled
) {

    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern(
                    "dd MMM yyyy, HH:mm"
            );

    public static CourseSessionListDto from(
            CourseSession courseSession
    ) {
        return new CourseSessionListDto(
                courseSession.getId(),
                courseSession.getTopic(),
                courseSession.getLocation(),
                courseSession.getStartsAt().format(DISPLAY_FORMAT),
                courseSession.getEndsAt().format(DISPLAY_FORMAT),
                courseSession.isCancelled()
        );
    }
}
