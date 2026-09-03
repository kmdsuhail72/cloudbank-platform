package com.cloudbank.auth.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.converter.RsaKeyConverters;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.io.IOException;
import java.io.InputStream;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Clock;
import java.util.Objects;

@Configuration
@EnableConfigurationProperties({
        JwtProperties.class,
        JwtSigningProperties.class
})
public class JwtConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public JwtEncoder jwtEncoder(
            JwtSigningProperties signingProperties
    ) throws IOException {
        try (
                InputStream privateKeyInputStream =
                        signingProperties.privateKey().getInputStream();

                InputStream publicKeyInputStream =
                        signingProperties.publicKey().getInputStream()
        ) {
            RSAPrivateKey privateKey =
                    Objects.requireNonNull(
                            RsaKeyConverters
                                    .pkcs8()
                                    .convert(privateKeyInputStream),
                            "JWT private key could not be parsed"
                    );

            RSAPublicKey publicKey =
                    Objects.requireNonNull(
                            RsaKeyConverters
                                    .x509()
                                    .convert(publicKeyInputStream),
                            "JWT public key could not be parsed"
                    );

            return NimbusJwtEncoder
                    .withKeyPair(
                            publicKey,
                            privateKey
                    )
                    .algorithm(SignatureAlgorithm.RS256)
                    .build();
        }
    }
}
