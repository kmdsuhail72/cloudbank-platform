package com.cloudbank.auth.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.hibernate.validator.constraints.time.DurationMin;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "cloudbank.jwt")
public record JwtProperties(

        @NotBlank
        String issuer,

        @NotBlank
        String audience,

        @NotNull
        @DurationMin(seconds = 60)
        Duration accessTokenTtl

) {
}
