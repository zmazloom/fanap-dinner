package fanap.dinner.message;

public class AuthMessage {

    private AuthMessage() {
    }

    public static String internalErrorAtGetLoginAddressWithPodSSO() {
        return Translator.toLocale("error.internal.get.login.address.with.podsso", null);
    }

    public static String userIsNotActive() {
        return Translator.toLocale("account.not.active", null);
    }

    public static String tokenIsExpired() {
        return Translator.toLocale("error.token.is.expired", null);
    }

    public static String errorAtGettingAccessToken() {
        return Translator.toLocale("error.get.access.token", null);
    }

}
