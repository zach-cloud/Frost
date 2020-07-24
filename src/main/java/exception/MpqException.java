package exception;

/**
 * An exception that is thrown when an encrypt/decrypt exception fails.
 */
public class MpqException extends RuntimeException {

    public MpqException(Exception other) {
        super(other);
    }

    public MpqException(String message) {
        super(message);
    }

    public MpqException() {
        super();
    }
}
