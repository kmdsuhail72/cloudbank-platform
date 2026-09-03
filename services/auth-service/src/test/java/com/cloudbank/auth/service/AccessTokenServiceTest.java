package com.cloudbank.auth.service;

import com.cloudbank.auth.config.JwtProperties;
import com.cloudbank.auth.model.AuthUser;
import com.cloudbank.auth.model.UserRole;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AccessTokenServiceTest {

    @Test
    void shouldIssueAccessTokenWithIdentityAndStandardClaims() {
        JwtEncoder jwtEncoder =
                mock(JwtEncoder.class);

        JwtProperties jwtProperties =
                new JwtProperties(
                        "cloudbank-auth-service",
                        "cloudbank-api",
                        Duration.ofMinutes(15)
                );

        Instant now =
                Instant.parse("2026-09-03T08:00:00Z");

        Clock clock =
                Clock.fixed(
                        now,
                        ZoneOffset.UTC
                );

        AccessTokenService accessTokenService =
                new AccessTokenService(
                        jwtEncoder,
                        jwtProperties,
                        clock
                );

        UUID userId =
                UUID.fromString(
                        "11111111-2222-3333-4444-555555555555"
                );

        AuthUser authUser =
                mock(AuthUser.class);

        when(authUser.getId())
                .thenReturn(userId);

        when(authUser.getEmail())
                .thenReturn("user@example.com");

        when(authUser.getRole())
                .thenReturn(UserRole.CUSTOMER);

        Jwt encodedJwt =
                new Jwt(
                        "encoded.jwt.value",
                        now,
                        now.plus(Duration.ofMinutes(15)),
                        Map.of(
                                "alg",
                                "RS256"
                        ),
                        Map.of(
                                "sub",
                                userId.toString()
                        )
                );

        when(jwtEncoder.encode(
                any(JwtEncoderParameters.class)
        )).thenReturn(encodedJwt);

        String tokenValue =
                accessTokenService.issue(authUser);

        assertEquals(
                "encoded.jwt.value",
                tokenValue
        );

        ArgumentCaptor<JwtEncoderParameters> parametersCaptor =
                ArgumentCaptor.forClass(
                        JwtEncoderParameters.class
                );

        verify(jwtEncoder)
                .encode(parametersCaptor.capture());

        Map<String, Object> claims =
                parametersCaptor
                        .getValue()
                        .getClaims()
                        .getClaims();

        assertEquals(
                "cloudbank-auth-service",
                claims.get("iss")
        );

        assertEquals(
                userId.toString(),
                claims.get("sub")
        );

        assertEquals(
                List.of("cloudbank-api"),
                claims.get("aud")
        );

        assertEquals(
                now,
                claims.get("iat")
        );

        assertEquals(
                now.plus(Duration.ofMinutes(15)),
                claims.get("exp")
        );

        assertEquals(
                "user@example.com",
                claims.get("email")
        );

        assertEquals(
                "CUSTOMER",
                claims.get("role")
        );

        assertNotNull(
                claims.get("jti")
        );
    }
}
