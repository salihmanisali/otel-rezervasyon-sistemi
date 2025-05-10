package com.otel.apigateway.security;

import com.otel.apigateway.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private final String SECRET = "testSecretKeyForJwtThatIsLongEnoughForTheAlgorithm";
    private final Long EXPIRATION = 3600000L; // 1 hour
    private JwtUtil jwtUtil;
    private User testUser;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", SECRET);
        ReflectionTestUtils.setField(jwtUtil, "expirationTime", EXPIRATION);

        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .password("password")
                .roles(Arrays.asList("ROLE_USER", "ROLE_ADMIN"))
                .build();
    }

    @Test
    void generateToken_ShouldCreateValidToken() {
        // When
        String token = jwtUtil.generateToken(testUser);

        // Then
        assertNotNull(token);
        assertTrue(token.length() > 0);
    }

    @Test
    void extractUsername_ShouldReturnCorrectUsername() {
        // Given
        String token = jwtUtil.generateToken(testUser);

        // When
        String username = jwtUtil.extractUsername(token);

        // Then
        assertEquals("testuser", username);
    }

    @Test
    void extractRoles_ShouldReturnCorrectRoles() {
        // Given
        String token = jwtUtil.generateToken(testUser);

        // When
        List<String> roles = jwtUtil.extractRoles(token);

        // Then
        assertEquals(2, roles.size());
        assertTrue(roles.contains("ROLE_USER"));
        assertTrue(roles.contains("ROLE_ADMIN"));
    }

    @Test
    void validateToken_WithValidToken_ShouldReturnTrue() {
        // Given
        String token = jwtUtil.generateToken(testUser);

        // When
        boolean isValid = jwtUtil.validateToken(token);

        // Then
        assertTrue(isValid);
    }

    @Test
    void validateToken_WithInvalidToken_ShouldReturnFalse() {
        // Given
        String invalidToken = "invalidToken";

        // When
        boolean isValid = jwtUtil.validateToken(invalidToken);

        // Then
        assertFalse(isValid);
    }

    @Test
    void validateToken_WithExpiredToken_ShouldReturnFalse() {
        // Given
        // Set a very short expiration time for this test
        ReflectionTestUtils.setField(jwtUtil, "expirationTime", 1L); // 1 millisecond
        String token = jwtUtil.generateToken(testUser);

        // Wait for the token to expire
        try {
            Thread.sleep(10); // Sleep for 10 milliseconds
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // When
        boolean isValid = jwtUtil.validateToken(token);

        // Then
        assertFalse(isValid);
    }
}