package fanap.dinner.config;

import org.springframework.boot.web.servlet.filter.OrderedRequestContextFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.filter.RequestContextFilter;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;
import fanap.dinner.message.Language;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Configuration
public class LanguageConfig extends AcceptHeaderLocaleResolver implements WebMvcConfigurer {

    private final List<Locale> locales = Arrays.asList(
            new Locale(Language.ENGLISH),
            new Locale(Language.PERSIAN));

    @Bean
    public LocaleResolver localeResolver() {
        final AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
        resolver.setSupportedLocales(locales);
        resolver.setDefaultLocale(new Locale(Language.PERSIAN));
        return resolver;
    }

    @Bean
    public ResourceBundleMessageSource messageSource() {
        ResourceBundleMessageSource resourceBundleMessageSource = new ResourceBundleMessageSource();
        resourceBundleMessageSource.setBasename("messages/messages");
        resourceBundleMessageSource.setDefaultEncoding(StandardCharsets.UTF_8.toString());
        resourceBundleMessageSource.setUseCodeAsDefaultMessage(true);
        return resourceBundleMessageSource;
    }

    @Bean
    public RequestContextFilter requestContextFilter() {
        OrderedRequestContextFilter filter = new OrderedRequestContextFilter();
        filter.setThreadContextInheritable(true);
        return filter;
    }

}