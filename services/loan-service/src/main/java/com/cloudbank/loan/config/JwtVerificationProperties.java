package com.cloudbank.loan.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

@ConfigurationProperties(prefix = "cloudbank.jwt")
public record JwtVerificationProperties(
        String issuer,
        String audience,
        Resource publicKey
) {
}
