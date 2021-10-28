package fanap.dinner.security;

import fanap.dinner.user.UserManager;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

@EnableWebSecurity
@RequiredArgsConstructor
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {


    @Value("${doc.private.username}")
    private String privateUsername;

    @Value("${doc.private.password}")
    private String privatePassword;

    private final UserManager userService;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .anonymous()
                .and()
                .authorizeRequests()
                .antMatchers( "/configuration/ui", "/swagger-resources",
                        "/configuration/security", "/swagger-ui.html", "/webjars/**").permitAll()
                .antMatchers(HttpMethod.GET, "/images/**", "/web/**", "/v1/**", "/api/**").permitAll()
                .and()
                .addFilter(new AuthorizationFilter(authenticationManager(), userService))
                .headers()
                .xssProtection()
                .and()
                .contentSecurityPolicy("script-src 'self'");

        http.authorizeRequests()
                .regexMatchers("\\/v2/api-docs\\?group=private(&.*|$)").authenticated()
                .and()
                .httpBasic();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication()
                .withUser(privateUsername)
                .password(bCryptPasswordEncoder().encode(privatePassword))
                .roles("SWAGGER_ADMIN");
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Collections.singletonList("*"));
        configuration.setAllowedMethods(Collections.singletonList("*"));
        configuration.setAllowedHeaders(Collections.singletonList("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}
