package com.example.taskmanager.dto;

/**
 * Response returned after a successful login or registration.
 */
public record AuthResponse(
        String token,
        String username
) {
}
