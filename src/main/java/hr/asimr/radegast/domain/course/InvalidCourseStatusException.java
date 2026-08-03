package hr.asimr.radegast.domain.course;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class InvalidCourseStatusException extends RuntimeException {

    public InvalidCourseStatusException(String message) {
        super(message);
    }
}
