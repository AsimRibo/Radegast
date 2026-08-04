package hr.asimr.radegast.domain.course;

import java.util.List;

public record CourseSessionsPageDto(
        CourseSessionCourseDto course,
        List<CourseSessionListDto> sessions,
        boolean canManage
) {
}
