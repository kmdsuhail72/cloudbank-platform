package com.cloudbank.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtDecoder jwtDecoder
    ) throws Exception {
        http
                .csrf(
                        csrf ->
                                csrf.disable()
                )
                .sessionManagement(
                        session ->
                                session.sessionCreationPolicy(
                                        SessionCreationPolicy.STATELESS
                                )
                )
                .authorizeHttpRequests(
                        authorization ->
                                authorization
                                        .requestMatchers(
                                                HttpMethod.GET,
                                                "/actuator/health"
                                        )
                                        .permitAll()
                                        .requestMatchers(
                                                HttpMethod.POST,
                                                "/api/v1/auth/login",
                                                "/api/v1/auth/register"
                                        )
                                        .permitAll()
                                        .requestMatchers(
                                                "/api/v1/**"
                                        )
                                        .authenticated()
                                        .anyRequest()
                                        .denyAll()
                )
                .oauth2ResourceServer(
                        oauth2 ->
                                oauth2.jwt(
                                        jwt ->
                                                jwt.decoder(
                                                        jwtDecoder
                                                )
                                )
                );

        return http.build();
    }
}
