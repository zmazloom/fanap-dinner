package fanap.dinner.exception;

import fanap.dinner.utils.ModelUtils;
import fanap.dinner.domain.vo.response.Response;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class LogUtils {

    private LogUtils() {
    }

    public static void trace(Logger logger, String message) {
        trace(logger, null, message);
    }

    public static void trace(Logger logger, Throwable e, String message) {
        if (logger != null) {
            if (e != null)
                logger.trace(message, e);
            else
                logger.trace(message);
        }
    }


    public static void debug(Logger logger, String message, Subject subject, AdditionalData... additionalData) {
        debug(logger, null, message, subject, additionalData);
    }

    public static void debug(Logger logger, Throwable e, Subject subject, AdditionalData... additionalData) {
        debug(logger, e, null, subject, additionalData);
    }

    public static void debug(Logger logger, Throwable e, String message, Subject subject, AdditionalData... additionalData) {
        additionalData = addLinesIfExceptionIsNull(e, additionalData);

        if (logger != null) {
            if (e != null)
                logger.debug(getLoggerMessage(message, subject, additionalData), e);
            else
                logger.debug(getLoggerMessage(message, subject, additionalData));
        }
    }


    public static void info(Logger logger, String message, Subject subject, AdditionalData... additionalData) {
        info(logger, null, message, subject, additionalData);
    }

    public static void info(Logger logger, Throwable e, Subject subject, AdditionalData... additionalData) {
        info(logger, e, null, subject, additionalData);
    }

    public static void info(Logger logger, Throwable e, String message, Subject subject, AdditionalData... additionalData) {
        additionalData = addLinesIfExceptionIsNull(e, additionalData);

        if (logger != null) {
            if (e != null)
                logger.info(getLoggerMessage(message, subject, additionalData), e);
            else
                logger.info(getLoggerMessage(message, subject, additionalData));
        }
    }


    //-------------http requests:
    public static void warn(Logger logger, HttpResponse response, Subject subject, AdditionalData... additionalData) {
        warn(logger, null, "Error executing http: " + getResponseString(response), subject, additionalData);
    }

    public static void warn(Logger logger, ResponseEntity<String> response, Subject subject, AdditionalData... additionalData) {
        warn(logger, null, "Error executing http: " + getResponseString(response), subject, additionalData);
    }

    public static void warn(Logger logger, Response response, Subject subject, AdditionalData... additionalData) {
        warn(logger, null, "Error executing http: " + response.toString(), subject, additionalData);
    }
    //-------------end of http requests


    public static void warn(Logger logger, String message, Subject subject, AdditionalData... additionalData) {
        warn(logger, null, message, subject, additionalData);
    }

    public static void warn(Logger logger, Throwable e, Subject subject, AdditionalData... additionalData) {
        warn(logger, e, null, subject, additionalData);
    }

    public static void warn(Logger logger, Throwable e, String message, Subject subject, AdditionalData... additionalData) {
        additionalData = addLinesIfExceptionIsNull(e, additionalData);

        if (logger != null) {
            if (e != null)
                logger.warn(getLoggerMessage(message, subject, additionalData), e);
            else
                logger.warn(getLoggerMessage(message, subject, additionalData));
        }

    }


    //-------------http requests:
    public static void error(Logger logger, HttpResponse response, Subject subject, AdditionalData... additionalData) {
        error(logger, null, "Error executing http: " + getResponseString(response), subject, additionalData);
    }

    public static void error(Logger logger, ResponseEntity<String> response, Subject subject, AdditionalData... additionalData) {
        error(logger, null, "Error executing http: " + getResponseString(response), subject, additionalData);
    }

    public static void error(Logger logger, Response response, Subject subject, AdditionalData... additionalData) {
        error(logger, null, "Error executing http: " + response.toString(), subject, additionalData);
    }
    //-------------end of http requests


    public static void error(Logger logger, Throwable e, Subject subject, AdditionalData... additionalData) {
        error(logger, e, null, subject, additionalData);
    }

    public static void error(Logger logger, String message, Subject subject, AdditionalData... additionalData) {
        error(logger, null, message, subject, additionalData);
    }

    public static void error(Logger logger, Throwable e, String message, Subject subject, AdditionalData... additionalData) {
        additionalData = addLinesIfExceptionIsNull(e, additionalData);

        if (logger != null) {
            if (e != null)
                logger.error(getLoggerMessage(message, subject, additionalData), e);
            else
                logger.error(getLoggerMessage(message, subject, additionalData));
        }
    }

    public static void fatal(Logger logger, Throwable e, String message, Subject subject, AdditionalData... additionalData) {
        additionalData = addLinesIfExceptionIsNull(e, additionalData);

        if (logger != null) {
            if (e != null)
                logger.error(getLoggerMessage(message, subject, additionalData), e);
            else
                logger.error(getLoggerMessage(message, subject, additionalData));
        }
    }

    //----------------private methods:
    private static AdditionalData[] addLinesIfExceptionIsNull(Throwable e, AdditionalData[] additionalData) {
        if (e == null) {
            if (additionalData == null) {
                additionalData = new AdditionalData[1];
                additionalData[0] = getLines();
            } else {
                ArrayUtils.add(additionalData, getLines());
            }
        }
        return additionalData;
    }

    private static AdditionalExtra getLines() {
        Exception lineFinder = getLineFinder();
        List<StackTraceElement> dinnerTrace = filterNoneDinnerTraces(lineFinder.getStackTrace());
        return new AdditionalExtra("lines", ModelUtils.toString(dinnerTrace));
    }

    public static Exception getLineFinder() {
        Exception lineFinder;
        try {
            throw new InternalServerException("line finder:(");
        } catch (InternalServerException e) {
            lineFinder = e;
        }
        return lineFinder;
    }

    private static List<StackTraceElement> filterNoneDinnerTraces(StackTraceElement[] stackTrace) {
        List<StackTraceElement> result = new ArrayList<>();
        String className;
        for (StackTraceElement element : stackTrace) {
            className = element.getClassName();
            if (className.startsWith("fanap.dinner"))
                result.add(element);
        }
        return result;
    }


    private static String getLoggerMessage(String message, Subject subject, AdditionalData... additionalData) {

        StringBuilder res = new StringBuilder();
        if (message != null)
            res.append(message);

        res.append("\nsubject: ").append(subject == null ? "unspecified" : subject.name());

        if (additionalData != null) {
            for (AdditionalData data : additionalData) {
                if (data != null)
                    res.append('\n').append(data);
            }
        }

        return res.toString();
    }


    public static String getResponseString(HttpResponse response) {
        String res = "status code: " + response.getStatusLine().getStatusCode() + "; ";
        try {
            if (response.getEntity() == null)
                res += "response entity is null.";
            else
                res += EntityUtils.toString(response.getEntity());
        } catch (IOException e) {
            res += "response entity is already read(stream closed).";
        }
        return res;
    }

    public static String getResponseString(ResponseEntity<String> response) {
        String res = "status code: " + response.getStatusCodeValue() + "; ";
        try {
            res += response.getBody();
        } catch (Exception e) {
            res += "error in reading response body!";
        }
        return res;
    }
}
