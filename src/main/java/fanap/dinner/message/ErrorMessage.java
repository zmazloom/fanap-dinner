package fanap.dinner.message;

public class ErrorMessage {

    private ErrorMessage() {
    }

    public static String errorInternalServer() {
        return Translator.toLocale("error.internal.server", null);
    }

    public static String accessDenied() {
        return Translator.toLocale("access.denied", null);
    }

    public static String errorResourceConflict() {
        return Translator.toLocale("error.resource.conflict", null);
    }

    public static String unknownError() {
        return Translator.toLocale("error.unknown", null);
    }
}
