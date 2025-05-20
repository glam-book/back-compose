package com.tlback.config.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;

import com.tlback.config.security.tg.TelegramAuthService;
import com.tlback.config.security.tg.TelegramAuthServiceDev;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig implements WebFluxConfigurer {

    @Bean
    @Profile("!no-auth")
    SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http,
            ReactiveAuthenticationManager authenticationManager,
            ServerAuthenticationConverter authenticationConverter) throws Exception {
        var authenticationWebFilter = new AuthenticationWebFilter(authenticationManager);
        authenticationWebFilter.setServerAuthenticationConverter(authenticationConverter);

        System.out.println("CREATING SECURITY WEB FILTER CHAIN !NO_AUTH");
        return http.csrf(c -> c.disable())
                .headers(c -> c.frameOptions(frame -> frame.disable()))
                .requestCache(c -> c.disable())
                .addFilterBefore(authenticationWebFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .authorizeExchange(c -> c.anyExchange().authenticated())
                .httpBasic(c -> c.disable())
                .formLogin(c -> c.disable())
                .logout(c -> c.disable())
                .build();
    }

    @Bean
    @Profile("no-auth")
    SecurityWebFilterChain securityWebFilterChainTest(ServerHttpSecurity http) throws Exception {
        System.out.println("CREATING SECURITY WEB FILTER CHAIN ::: NO_AUTH");
        return http.csrf(c -> c.disable())
                .headers(c -> c.frameOptions(frame -> frame.disable()))
                .requestCache(c -> c.disable())
                .authorizeExchange(c -> c.anyExchange().permitAll())
                .httpBasic(c -> c.disable())
                .formLogin(c -> c.disable())
                .logout(c -> c.disable())
                .build();
    }

    @Override
    public void addCorsMappings(@NonNull CorsRegistry corsRegistry) {
        corsRegistry.addMapping("/**")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedOriginPatterns("*")
                .allowedHeaders("*")
                .maxAge(3600);
    }

    @Bean
    @Profile("dev")
    TelegramAuthServiceDev telegramAuthServiceDev() {
        return new TelegramAuthServiceDev("dev");
    }

    @Bean
    @ConditionalOnMissingBean
    TelegramAuthService telegramAuthService(@Value("${GLAM_TG_BOT_TOKEN}") String botToken) {
        return new TelegramAuthService(botToken);
    }
}
