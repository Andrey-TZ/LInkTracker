package backend.academy.scrapper.exceptions;

public class APIException extends RuntimeException {
    public APIException(String message) {
        super(message);
    }
}
