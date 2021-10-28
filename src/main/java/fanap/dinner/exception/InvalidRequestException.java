package fanap.dinner.exception;

import org.springframework.http.HttpStatus;

public class InvalidRequestException extends ProjectRuntimeException {

    @Override
    protected HttpStatus getHttpStatus() {
        return HttpStatus.BAD_REQUEST;
    }

    public InvalidRequestException(String message) {
        super(message);
    }

    public static InvalidRequestException getInstance(String message) {
        return new InvalidRequestException(message);
    }

}
