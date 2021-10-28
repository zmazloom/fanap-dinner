package fanap.dinner.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends ProjectRuntimeException {

    @Override
    protected HttpStatus getHttpStatus() {
        return HttpStatus.NOT_FOUND;
    }

    private final String resource;

    public ResourceNotFoundException(String resource) {
        this.resource = resource;
    }

    public static ResourceNotFoundException getInstance(String msg) {
        return new ResourceNotFoundException(msg);
    }

    @Override
    public String getMessage() {
        return resource;
    }

}
