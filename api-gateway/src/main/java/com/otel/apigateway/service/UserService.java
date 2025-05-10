package com.otel.apigateway.service;

import com.otel.apigateway.model.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserService {

    // In a real application, this would be stored in a database
    private final Map<String, User> users = new HashMap<>();
    private final PasswordEncoder passwordEncoder;

    public UserService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;

        // Initialize with some test users
        users.put("admin", User.builder()
                .id(1L)
                .username("admin")
                .password(passwordEncoder.encode("password"))
                .roles(List.of("ROLE_ADMIN"))
                .build());

        users.put("user", User.builder()
                .id(2L)
                .username("user")
                .password(passwordEncoder.encode("password"))
                .roles(List.of("ROLE_USER"))
                .build());
    }

    public Mono<User> findByUsername(String username) {
        return Mono.justOrEmpty(users.get(username));
    }
}
