package hr.asimr.radegast.domain.course;

public class CourseSessionOperationNotAllowedException extends RuntimeException {

    public CourseSessionOperationNotAllowedException(String message) {
        super(message);
    }
}
