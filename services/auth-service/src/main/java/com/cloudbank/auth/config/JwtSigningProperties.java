package com.cloudbank.auth.config;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "cloudbank.jwt.signing")
public record JwtSigningProperties(

        @NotNull
        Resource privateKey,

        @NotNull
        Resource publicKey

) {
}
