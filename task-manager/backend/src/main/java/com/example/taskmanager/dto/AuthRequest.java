package com.example.taskmanager.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Credentials submitted on login.
 */
public record AuthRequest(

        @NotBlank(message = "Username must not be blank")
        String username,

        @NotBlank(message = "Password must not be blank")
        String password
) {
}
