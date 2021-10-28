package fanap.dinner.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@Configuration
@ComponentScan("fanap.dinner")
@PropertySource(
        value = {"file:env.properties", "file:${config.location}", "classpath:/env.properties"}
        , ignoreResourceNotFound = true)
public class AppSettings {

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }
}
