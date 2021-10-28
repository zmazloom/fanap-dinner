package fanap.dinner.exception;

import org.springframework.http.HttpStatus;

public class NotAcceptableRequestException extends ProjectRuntimeException {

    @Override
    protected HttpStatus getHttpStatus() {
        return HttpStatus.NOT_ACCEPTABLE;
    }

    private final String resource;

    public NotAcceptableRequestException(String resource) {
        this.resource = resource;
    }

    public static NotAcceptableRequestException getInstance(String msg) {
        return new NotAcceptableRequestException(msg);
    }

    @Override
    public String getMessage() {
        return resource;
    }

}
