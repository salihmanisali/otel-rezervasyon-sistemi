package com.otel.apigateway.controller;

import com.otel.apigateway.model.User;
import com.otel.apigateway.security.JwtUtil;
import com.otel.apigateway.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthController authController;

    private WebTestClient webTestClient;
    private User testUser;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToController(authController).build();

        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .password("password")
                .roles(List.of("ROLE_USER"))
                .build();
    }

    @Test
    void login_WithValidCredentials_ShouldReturnToken() {
        // Given
        Map<String, String> credentials = new HashMap<>();
        credentials.put("username", "testuser");
        credentials.put("password", "password");

        when(userService.findByUsername("testuser")).thenReturn(Mono.just(testUser));
        when(passwordEncoder.matches("password", testUser.getPassword())).thenReturn(true);
        when(jwtUtil.generateToken(any(User.class))).thenReturn("test-jwt-token");

        // When & Then
        webTestClient.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(credentials)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.token").isEqualTo("test-jwt-token");
    }

    @Test
    void login_WithInvalidUsername_ShouldReturnUnauthorized() {
        // Given
        Map<String, String> credentials = new HashMap<>();
        credentials.put("username", "nonexistentuser");
        credentials.put("password", "password");

        when(userService.findByUsername("nonexistentuser")).thenReturn(Mono.empty());

        // When & Then
        webTestClient.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(credentials)
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody().isEmpty();
    }

    @Test
    void login_WithInvalidPassword_ShouldReturnUnauthorized() {
        // Given
        Map<String, String> credentials = new HashMap<>();
        credentials.put("username", "testuser");
        credentials.put("password", "wrongpassword");

        when(userService.findByUsername("testuser")).thenReturn(Mono.just(testUser));
        when(passwordEncoder.matches("wrongpassword", testUser.getPassword())).thenReturn(false);

        // When & Then
        webTestClient.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(credentials)
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody().isEmpty();
    }
}
