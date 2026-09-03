package com.cloudbank.auth.service;

import com.cloudbank.auth.config.JwtProperties;
import com.cloudbank.auth.model.AuthUser;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class AccessTokenService {

    private final JwtEncoder jwtEncoder;
    private final JwtProperties jwtProperties;
    private final Clock clock;

    public AccessTokenService(
            JwtEncoder jwtEncoder,
            JwtProperties jwtProperties,
            Clock clock
    ) {
        this.jwtEncoder = jwtEncoder;
        this.jwtProperties = jwtProperties;
        this.clock = clock;
    }

    public String issue(AuthUser authUser) {
        Instant issuedAt =
                clock.instant();

        Instant expiresAt =
                issuedAt.plus(
                        jwtProperties.accessTokenTtl()
                );

        JwtClaimsSet claims =
                JwtClaimsSet.builder()
                        .issuer(jwtProperties.issuer())
                        .subject(authUser.getId().toString())
                        .audience(List.of(
                                jwtProperties.audience()
                        ))
                        .issuedAt(issuedAt)
                        .expiresAt(expiresAt)
                        .id(UUID.randomUUID().toString())
                        .claim(
                                "email",
                                authUser.getEmail()
                        )
                        .claim(
                                "role",
                                authUser.getRole().name()
                        )
                        .build();

        return jwtEncoder
                .encode(
                        JwtEncoderParameters.from(claims)
                )
                .getTokenValue();
    }
}
