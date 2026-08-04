package hr.asimr.radegast.domain.course;


public class CourseSessionNotFoundException extends RuntimeException {

    public CourseSessionNotFoundException(Long sessionId, Long courseId) {
        super(
                "Course session with id " + sessionId + " was not found for course " + courseId + "."
        );
    }
}
