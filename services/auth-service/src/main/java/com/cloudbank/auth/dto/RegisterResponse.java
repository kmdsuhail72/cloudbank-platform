package com.cloudbank.auth.dto;

public record RegisterResponse(
        String userId,
        String email,
        String role,
        String message
) {
}
