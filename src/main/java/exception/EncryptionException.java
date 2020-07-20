package exception;

/**
 * An exception that is thrown when an encrypt/decrypt exception fails.
 */
public class EncryptionException extends RuntimeException {

    public EncryptionException(Exception other) {
        super(other);
    }

    public EncryptionException(String message) {
        super(message);
    }

    public EncryptionException() {
        super();
    }
}
