package hr.asimr.radegast.domain.course;


public class CourseCodeAlreadyExistsException extends RuntimeException {

    public CourseCodeAlreadyExistsException(String code) {
        super("A course with code '" + code + "' already exists.");
    }
}
