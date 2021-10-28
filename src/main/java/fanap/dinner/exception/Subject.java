package fanap.dinner.exception;

/**
 * Subjects for Sentry events and log data.
 * can be used for searching in Sentry (customized search) and logs
 * also for auto-assigning events (based on subject) to specific people.
 */

public enum Subject {
    AUTH,
    USER,
    USER_GROUP,
    UNSPECIFIED,
    UNKNOWN_EXCEPTION,
    SEND_REQUEST
}
