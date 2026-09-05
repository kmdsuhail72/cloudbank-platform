package com.cloudbank.gateway.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.converter.RsaKeyConverters;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.JwtAudienceValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.io.IOException;
import java.io.InputStream;
import java.security.interfaces.RSAPublicKey;
import java.util.Objects;

@Configuration
@EnableConfigurationProperties(JwtVerificationProperties.class)
public class JwtDecoderConfig {

    @Bean
    public JwtDecoder jwtDecoder(
            JwtVerificationProperties properties
    ) throws IOException {
        try (
                InputStream publicKeyInputStream =
                        properties
                                .publicKey()
                                .getInputStream()
        ) {
            RSAPublicKey publicKey =
                    Objects.requireNonNull(
                            RsaKeyConverters
                                    .x509()
                                    .convert(
                                            publicKeyInputStream
                                    ),
                            "JWT public key could not be parsed"
                    );

            NimbusJwtDecoder jwtDecoder =
                    NimbusJwtDecoder
                            .withPublicKey(publicKey)
                            .signatureAlgorithm(
                                    SignatureAlgorithm.RS256
                            )
                            .build();

            jwtDecoder.setJwtValidator(
                    new DelegatingOAuth2TokenValidator<>(
                            JwtValidators
                                    .createDefaultWithIssuer(
                                            properties.issuer()
                                    ),
                            new JwtAudienceValidator(
                                    properties.audience()
                            )
                    )
            );

            return jwtDecoder;
        }
    }
}
