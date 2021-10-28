package fanap.dinner.exception;

import fanap.dinner.message.ErrorMessage;
import org.springframework.http.HttpStatus;

public class ResourceConflictException extends ProjectRuntimeException {

    @Override
    protected HttpStatus getHttpStatus() {
        return HttpStatus.CONFLICT;
    }

    public ResourceConflictException(String message) {
        super(message);
    }


    public static ResourceConflictException getInstance() {
        return new ResourceConflictException(ErrorMessage.errorResourceConflict());

    }

    public static ResourceConflictException getInstance(String msg) {
        return new ResourceConflictException(msg);

    }
}
