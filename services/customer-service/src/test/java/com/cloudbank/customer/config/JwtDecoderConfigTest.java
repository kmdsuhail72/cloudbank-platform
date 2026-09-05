package com.cloudbank.customer.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.FileSystemResource;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtDecoderConfigTest {

    @Test
    void shouldDecodeRs256TokenWithExpectedIssuerAndAudience(
            @TempDir Path tempDirectory
    ) throws Exception {
        KeyPairGenerator keyPairGenerator =
                KeyPairGenerator.getInstance("RSA");

        keyPairGenerator.initialize(2048);

        KeyPair keyPair =
                keyPairGenerator.generateKeyPair();

        RSAPublicKey publicKey =
                (RSAPublicKey) keyPair.getPublic();

        RSAPrivateKey privateKey =
                (RSAPrivateKey) keyPair.getPrivate();

        Path publicKeyPath =
                tempDirectory.resolve("jwt-public.pem");

        writePublicKeyPem(
                publicKeyPath,
                publicKey
        );

        JwtVerificationProperties properties =
                new JwtVerificationProperties(
                        "https://auth.cloudbank.test",
                        "cloudbank-api",
                        new FileSystemResource(
                                publicKeyPath.toFile()
                        )
                );

        JwtDecoder jwtDecoder =
                new JwtDecoderConfig()
                        .jwtDecoder(properties);

        JwtEncoder jwtEncoder =
                NimbusJwtEncoder
                        .withKeyPair(
                                publicKey,
                                privateKey
                        )
                        .algorithm(
                                SignatureAlgorithm.RS256
                        )
                        .build();

        Instant issuedAt =
                Instant.now();

        JwtClaimsSet claims =
                JwtClaimsSet.builder()
                        .issuer(
                                "https://auth.cloudbank.test"
                        )
                        .subject("test-user-id")
                        .audience(
                                List.of("cloudbank-api")
                        )
                        .issuedAt(issuedAt)
                        .expiresAt(
                                issuedAt.plusSeconds(300)
                        )
                        .id("test-jti")
                        .build();

        String token =
                jwtEncoder
                        .encode(
                                JwtEncoderParameters.from(
                                        claims
                                )
                        )
                        .getTokenValue();

        Jwt decodedJwt =
                jwtDecoder.decode(token);

        assertEquals(
                "test-user-id",
                decodedJwt.getSubject()
        );

        assertEquals(
                "https://auth.cloudbank.test",
                decodedJwt.getIssuer().toString()
        );

        assertEquals(
                List.of("cloudbank-api"),
                decodedJwt.getAudience()
        );
    }

    @Test
    void shouldRejectTokenWithUnexpectedIssuer(
            @TempDir Path tempDirectory
    ) throws Exception {
        KeyPairGenerator keyPairGenerator =
                KeyPairGenerator.getInstance("RSA");

        keyPairGenerator.initialize(2048);

        KeyPair keyPair =
                keyPairGenerator.generateKeyPair();

        RSAPublicKey publicKey =
                (RSAPublicKey) keyPair.getPublic();

        RSAPrivateKey privateKey =
                (RSAPrivateKey) keyPair.getPrivate();

        Path publicKeyPath =
                tempDirectory.resolve("jwt-public.pem");

        writePublicKeyPem(
                publicKeyPath,
                publicKey
        );

        JwtVerificationProperties properties =
                new JwtVerificationProperties(
                        "https://auth.cloudbank.test",
                        "cloudbank-api",
                        new FileSystemResource(
                                publicKeyPath.toFile()
                        )
                );

        JwtDecoder jwtDecoder =
                new JwtDecoderConfig()
                        .jwtDecoder(properties);

        JwtEncoder jwtEncoder =
                NimbusJwtEncoder
                        .withKeyPair(
                                publicKey,
                                privateKey
                        )
                        .algorithm(
                                SignatureAlgorithm.RS256
                        )
                        .build();

        Instant issuedAt =
                Instant.now();

        JwtClaimsSet claims =
                JwtClaimsSet.builder()
                        .issuer(
                                "https://wrong.cloudbank.test"
                        )
                        .subject("test-user-id")
                        .audience(
                                List.of("cloudbank-api")
                        )
                        .issuedAt(issuedAt)
                        .expiresAt(
                                issuedAt.plusSeconds(300)
                        )
                        .id("test-jti")
                        .build();

        String token =
                jwtEncoder
                        .encode(
                                JwtEncoderParameters.from(
                                        claims
                                )
                        )
                        .getTokenValue();

        assertThrows(
                JwtException.class,
                () -> jwtDecoder.decode(token)
        );
    }

    @Test
    void shouldRejectTokenWithUnexpectedAudience(
            @TempDir Path tempDirectory
    ) throws Exception {
        KeyPairGenerator keyPairGenerator =
                KeyPairGenerator.getInstance("RSA");

        keyPairGenerator.initialize(2048);

        KeyPair keyPair =
                keyPairGenerator.generateKeyPair();

        RSAPublicKey publicKey =
                (RSAPublicKey) keyPair.getPublic();

        RSAPrivateKey privateKey =
                (RSAPrivateKey) keyPair.getPrivate();

        Path publicKeyPath =
                tempDirectory.resolve("jwt-public.pem");

        writePublicKeyPem(
                publicKeyPath,
                publicKey
        );

        JwtVerificationProperties properties =
                new JwtVerificationProperties(
                        "https://auth.cloudbank.test",
                        "cloudbank-api",
                        new FileSystemResource(
                                publicKeyPath.toFile()
                        )
                );

        JwtDecoder jwtDecoder =
                new JwtDecoderConfig()
                        .jwtDecoder(properties);

        JwtEncoder jwtEncoder =
                NimbusJwtEncoder
                        .withKeyPair(
                                publicKey,
                                privateKey
                        )
                        .algorithm(
                                SignatureAlgorithm.RS256
                        )
                        .build();

        Instant issuedAt =
                Instant.now();

        JwtClaimsSet claims =
                JwtClaimsSet.builder()
                        .issuer(
                                "https://auth.cloudbank.test"
                        )
                        .subject("test-user-id")
                        .audience(
                                List.of("wrong-api")
                        )
                        .issuedAt(issuedAt)
                        .expiresAt(
                                issuedAt.plusSeconds(300)
                        )
                        .id("test-jti")
                        .build();

        String token =
                jwtEncoder
                        .encode(
                                JwtEncoderParameters.from(
                                        claims
                                )
                        )
                        .getTokenValue();

        assertThrows(
                JwtException.class,
                () -> jwtDecoder.decode(token)
        );
    }

    private void writePublicKeyPem(
            Path path,
            RSAPublicKey publicKey
    ) throws Exception {
        String encoded =
                Base64.getMimeEncoder(
                                64,
                                new byte[]{'\n'}
                        )
                        .encodeToString(
                                publicKey.getEncoded()
                        );

        String pem =
                "-----BEGIN PUBLIC KEY-----\n"
                        + encoded
                        + "\n-----END PUBLIC KEY-----\n";

        Files.writeString(
                path,
                pem
        );
    }
}
