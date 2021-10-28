package fanap.dinner.config;

import fanap.dinner.document.AdminAPI;
import com.google.common.base.Predicate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.RequestHandler;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.google.common.base.Predicates.*;

/**
 * config of Swagger
 */

@Configuration
@EnableSwagger2
public class SwaggerConfig {

    @Value("${platform.core.host}")
    private String coreHost;

    @Bean
    public Docket publicApi() {
        return buildDocket("general", and(
                        not(RequestHandlerSelectors.withClassAnnotation(AdminAPI.class)),
                        not(RequestHandlerSelectors.withMethodAnnotation(AdminAPI.class))
                ),
                publicSecuritySchemes(),
                publicSecurityContexts());
    }

    @Bean
    public Docket api() {
        return buildDocket("private",
                RequestHandlerSelectors.any(),
                securitySchemes(),
                securityContexts());
    }

    private Docket buildDocket(String groupName, Predicate<RequestHandler> apis, List<ApiKey> securitySchemes, List<SecurityContext> securityContexts) {

        return new Docket(DocumentationType.SWAGGER_2)
                .host(coreHost)
                .groupName(groupName)
                .select()
                .apis(apis)
                .paths(PathSelectors.any())
                .build()

                .securitySchemes(securitySchemes)
                .securityContexts(securityContexts)

                .apiInfo(getApiInfo());
    }

    private List<ApiKey> publicSecuritySchemes() {
        return Collections.singletonList(new ApiKey("Authorization", "Authorization", "header"));
    }

    private List<ApiKey> securitySchemes() {
        return Arrays.asList(new ApiKey("Authorization", "Authorization", "header"),
                new ApiKey("admin", "service", "header"),
                new ApiKey("admin-key", "service-key", "header"));
    }

    private List<SecurityContext> publicSecurityContexts() {
        return Collections.singletonList(SecurityContext.builder()
                .securityReferences(publicAuth())
                .forPaths(or(
                        PathSelectors.ant("/group/**"),
                        PathSelectors.ant("/user/**"),
                        PathSelectors.ant("/auth/**"),
                        PathSelectors.ant("/service/**")
                ))
                .build());
    }

    private List<SecurityContext> securityContexts() {
        return Collections.singletonList(SecurityContext.builder()
                .securityReferences(auth())
                .forPaths(or(
                        PathSelectors.ant("/group/**"),
                        PathSelectors.ant("/user/**"),
                        PathSelectors.ant("/auth/**"),
                        PathSelectors.ant("/service/**")
                ))
                .build());
    }

    List<SecurityReference> publicAuth() {
        AuthorizationScope[] authorizationScopes = {new AuthorizationScope("global", "accessEverything")};
        return Collections.singletonList(new SecurityReference("Authorization", authorizationScopes));
    }

    List<SecurityReference> auth() {
        AuthorizationScope[] authorizationScopes = {new AuthorizationScope("global", "accessEverything")};
        return Arrays.asList(new SecurityReference("Authorization", authorizationScopes),
                new SecurityReference("admin", authorizationScopes),
                new SecurityReference("admin-key", authorizationScopes));
    }

    /**
     * Sample info for Swagger
     */
    private ApiInfo getApiInfo() {
        return new ApiInfo(
                "Fanap Dinner API",
                "Admin APIs for Fanap Dinner Project",
                "1.0.0",
                "urn:tos",
                new Contact("Dinner", "https://dinner.sakku-khatam.ir", "zmazloom94@gmail.com"),
                "License of API",
                "API license URL", Collections.emptyList());
    }
}
