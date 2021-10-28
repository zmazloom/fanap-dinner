package fanap.dinner.message;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class Translator {

    private static ResourceBundleMessageSource messageSource;

    @Autowired
    Translator(ResourceBundleMessageSource messageSource) {
        Translator.messageSource = messageSource;
    }

    static String toLocale(String messageCode, Object[] params) {
        Locale requestLocale = LocaleContextHolder.getLocale();

        if (requestLocale.equals(new Locale(Language.ARABIC)))
            requestLocale = new Locale(Language.ENGLISH);

        return sendMessage(messageCode, params, requestLocale);
    }

    private static String sendMessage(String messageCode, Object[] params, Locale requestLocale) {
        return messageSource.getMessage(messageCode, params, requestLocale);
    }

    public static Locale getLocale() {
        Locale requestLocale = LocaleContextHolder.getLocale();

        if (requestLocale.equals(new Locale(Language.ARABIC)))
            requestLocale = new Locale(Language.ENGLISH);

        return requestLocale;
    }

    public static String getRequestLanguage() {
        Locale requestLocale = LocaleContextHolder.getLocale();

        if (requestLocale.equals(new Locale(Language.ARABIC)) || requestLocale.equals(new Locale(Language.ENGLISH)))
            return Language.ENGLISH;

        return Language.PERSIAN;
    }

}