package hr.asimr.radegast.domain.user;

public class InvalidRegistrationRoleException extends RuntimeException {

    public InvalidRegistrationRoleException() {
        super("Submitted role is not available for you.");
    }
}
