package fanap.dinner.exception;

import fanap.dinner.domain.vo.exception.ApiErrorVO;
import fanap.dinner.message.ErrorMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Calendar;

import static org.springframework.web.context.request.RequestAttributes.SCOPE_REQUEST;

@Slf4j
@ControllerAdvice(basePackages = "fanap.dinner")
public class DinnerExceptionHandler extends ResponseEntityExceptionHandler implements AccessDeniedHandler {

    @ResponseBody
    @org.springframework.web.bind.annotation.ExceptionHandler(value = {
            RuntimeException.class,
            InterruptedException.class
    })
    public final ResponseEntity<ApiErrorVO> handleExceptions(RuntimeException ex, WebRequest request) {
        HttpHeaders headers = new HttpHeaders();

        if (ex instanceof ProjectRuntimeException) {
            return handleExceptionInternal(ex, headers, ((ProjectRuntimeException) ex).getHttpStatus(), request);
        } else if (ex instanceof org.springframework.security.access.AccessDeniedException) {
            HttpStatus status = HttpStatus.UNAUTHORIZED;
            return handleExceptionInternal(ex, headers, status, request);
        } else {
            LogUtils.fatal(log, ex, "Unknown exception type: " + ex.getClass().getName(), Subject.UNKNOWN_EXCEPTION);

            HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
            return handleExceptionInternal(InternalServerException.getInstance(ErrorMessage.unknownError()),
                    headers, status, request);
        }
    }

    /**
     * A single place to customize the response body of all Exception types.
     *
     * <p>The default implementation sets the {@link WebUtils#ERROR_EXCEPTION_ATTRIBUTE}
     * request attribute and creates a {@link ResponseEntity} from the given
     * body, headers, and status.
     *
     * @param ex      The exception
     * @param status  The response status
     * @param request The current request
     */
    protected ResponseEntity<ApiErrorVO> handleExceptionInternal(Exception ex,
                                                                 HttpHeaders headers, HttpStatus status,
                                                                 WebRequest request) {
        if (HttpStatus.INTERNAL_SERVER_ERROR.equals(status)) {
            request.setAttribute(WebUtils.ERROR_EXCEPTION_ATTRIBUTE, ex, SCOPE_REQUEST);
        }

        ApiErrorVO body = ApiErrorVO.builder()
                .timestamp(Calendar.getInstance().getTime())
                .error(ex.getClass().getSimpleName())
                .message(ex.getMessage())
                .status(status.value())
                .path(((ServletWebRequest) request).getRequest().getRequestURI())
                .build();


        return new ResponseEntity<>(body, headers, status);
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse httpServletResponse, org.springframework.security.access.AccessDeniedException e) throws IOException {
        httpServletResponse.setStatus(HttpStatus.FORBIDDEN.value());
        httpServletResponse.sendRedirect("/error/access-denied");
    }

}
