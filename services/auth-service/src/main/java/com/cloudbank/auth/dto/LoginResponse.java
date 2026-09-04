package com.cloudbank.auth.dto;

public record LoginResponse(
        String accessToken,
        String tokenType
) {
}
