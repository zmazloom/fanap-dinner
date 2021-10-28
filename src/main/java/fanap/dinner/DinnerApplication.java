package fanap.dinner;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.rest.RepositoryRestMvcAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Arrays;

/**
 * The main class of project
 */

@EnableSwagger2
@SpringBootApplication(
        exclude = {RepositoryRestMvcAutoConfiguration.class, HibernateJpaAutoConfiguration.class},
        scanBasePackages = "fanap.dinner"
)
public class DinnerApplication {

    private static String projectSessionId;

    private static ConfigurableApplicationContext applicationContext;

    public static void main(String[] args) {
        applicationContext = SpringApplication.run(fanap.dinner.DinnerApplication.class);
    }

    @Value("${server.servlet.session.cookie.name}")
    public void setProjectSessionId(String projectSessionId) {
        DinnerApplication.projectSessionId = projectSessionId;
    }

    @Bean
    public ServletContextInitializer servletContextInitializer() {
        return servletContext -> servletContext.getSessionCookieConfig().setName(projectSessionId);
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyConfigInDev() {
        return new PropertySourcesPlaceholderConfigurer();
    }

}
