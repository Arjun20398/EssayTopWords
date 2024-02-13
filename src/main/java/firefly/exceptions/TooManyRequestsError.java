package firefly.exceptions;

public class TooManyRequestsError extends RuntimeException {

    private String message;

    public TooManyRequestsError(String message) {
        super(message);
    }
}
