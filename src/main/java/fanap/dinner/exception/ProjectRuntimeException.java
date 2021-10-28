package fanap.dinner.exception;

import org.springframework.http.HttpStatus;

/**
 * This class is for our handled exceptions. every exception class must extend this class.
 */

public abstract class ProjectRuntimeException extends RuntimeException {

    protected abstract HttpStatus getHttpStatus();

    protected ProjectRuntimeException() {
        super();
    }

    protected ProjectRuntimeException(String message) {
        super(message);
    }

    protected ProjectRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

}
