package br.com.frankefelipe5.calmail.api.security;

import br.com.frankefelipe5.calmail.api.exception.SecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) {
        try {
            httpSecurity
                    .csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(auth -> auth.requestMatchers("/api/auth/**")
                            .permitAll()
                            .requestMatchers("/api/responses/**")
                            .authenticated()
                            .requestMatchers("/**")
                            .permitAll())
                    .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
            return httpSecurity.build();
        } catch (Exception exception) {
            logger.error("security config error", exception);
            throw new SecurityException(exception);
        }
    }
}
