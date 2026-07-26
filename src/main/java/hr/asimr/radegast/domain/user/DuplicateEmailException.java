package hr.asimr.radegast.domain.user;

public class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException() {
        super("Account with this email already exists.");
    }
}
