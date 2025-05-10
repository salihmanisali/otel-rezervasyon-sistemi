package com.otel.apigateway.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/api/auth/**").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/hotels/**", "/api/rooms/**").permitAll() // Allow public access to GET operations for hotels and rooms
                        .pathMatchers(HttpMethod.POST, "/api/hotels/**", "/api/rooms/**").hasAuthority("ROLE_ADMIN") // Restrict POST operations to ADMIN
                        .pathMatchers(HttpMethod.PUT, "/api/hotels/**", "/api/rooms/**").hasAuthority("ROLE_ADMIN") // Restrict PUT operations to ADMIN
                        .pathMatchers(HttpMethod.DELETE, "/api/hotels/**", "/api/rooms/**").hasAuthority("ROLE_ADMIN") // Restrict DELETE operations to ADMIN
                        .pathMatchers("/api/reservations/**").authenticated()
                        .pathMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/api-docs/**",
                                "/*/swagger-ui.html",
                                "/*/swagger-ui/**",
                                "/*/api-docs/**",
                                "/hotel-service/swagger-ui.html",
                                "/hotel-service/swagger-ui/**",
                                "/hotel-service/api-docs/**",
                                "/reservation-service/swagger-ui.html",
                                "/reservation-service/swagger-ui/**",
                                "/reservation-service/api-docs/**",
                                "/notification-service/swagger-ui.html",
                                "/notification-service/swagger-ui/**",
                                "/notification-service/api-docs/**"
                        ).permitAll()
                        .anyExchange().authenticated()
                )
                .addFilterAt(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }
}
