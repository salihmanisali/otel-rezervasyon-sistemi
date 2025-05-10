package com.otel.apigateway.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JwtAuthenticationFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private ServerWebExchange exchange;

    @Mock
    private WebFilterChain chain;

    @Mock
    private ServerHttpRequest request;

    @Mock
    private HttpHeaders headers;

    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtUtil);

        when(exchange.getRequest()).thenReturn(request);
        when(request.getHeaders()).thenReturn(headers);
        when(chain.filter(any())).thenReturn(Mono.empty());
    }

    @Test
    void filter_WithValidToken_ShouldSetAuthentication() {
        // Given
        String token = "valid-token";
        when(headers.getFirst(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + token);
        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.extractUsername(token)).thenReturn("user");
        when(jwtUtil.extractRoles(token)).thenReturn(List.of("ROLE_USER"));

        // When
        Mono<Void> result = jwtAuthenticationFilter.filter(exchange, chain);

        // Then
        verify(chain).filter(exchange);
        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void filter_WithInvalidToken_ShouldSetEmptyAuthentication() {
        // Given
        String token = "invalid-token";
        when(headers.getFirst(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + token);
        when(jwtUtil.validateToken(token)).thenReturn(false);

        // When
        Mono<Void> result = jwtAuthenticationFilter.filter(exchange, chain);

        // Then
        verify(chain).filter(exchange);
        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void filter_WithNoToken_ShouldSetEmptyAuthentication() {
        // Given
        when(headers.getFirst(HttpHeaders.AUTHORIZATION)).thenReturn(null);

        // When
        Mono<Void> result = jwtAuthenticationFilter.filter(exchange, chain);

        // Then
        verify(chain).filter(exchange);
        StepVerifier.create(result)
                .verifyComplete();
    }
}