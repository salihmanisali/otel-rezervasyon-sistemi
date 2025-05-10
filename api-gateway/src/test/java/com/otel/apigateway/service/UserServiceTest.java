package com.otel.apigateway.service;

import com.otel.apigateway.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService userService;

    @BeforeEach
    void setUp() {
        // Mock the password encoder to return the input string (for testing simplicity)
        when(passwordEncoder.encode(anyString())).thenAnswer(invocation -> "encoded_" + invocation.getArgument(0));

        userService = new UserService(passwordEncoder);
    }

    @Test
    void findByUsername_WhenUserExists_ShouldReturnUser() {
        // When
        Mono<User> result = userService.findByUsername("admin");

        // Then
        StepVerifier.create(result)
                .expectNextMatches(user ->
                        user.getUsername().equals("admin") &&
                                user.getPassword().startsWith("encoded_") &&
                                user.getRoles().contains("ROLE_ADMIN")
                )
                .verifyComplete();
    }

    @Test
    void findByUsername_WhenUserDoesNotExist_ShouldReturnEmptyMono() {
        // When
        Mono<User> result = userService.findByUsername("nonexistentuser");

        // Then
        StepVerifier.create(result)
                .verifyComplete();
    }
}
