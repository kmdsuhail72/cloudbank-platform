package com.cloudbank.auth.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.FileSystemResource;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class JwtConfigTest {

    @Test
    void shouldSignAndVerifyJwtWithRs256UsingGeneratedKeyPair(
            @TempDir Path tempDirectory
    ) throws Exception {
        KeyPairGenerator keyPairGenerator =
                KeyPairGenerator.getInstance("RSA");

        keyPairGenerator.initialize(2048);

        KeyPair keyPair =
                keyPairGenerator.generateKeyPair();

        Path privateKeyPath =
                tempDirectory.resolve("jwt-private.pem");

        Path publicKeyPath =
                tempDirectory.resolve("jwt-public.pem");

        writePem(
                privateKeyPath,
                "PRIVATE KEY",
                keyPair.getPrivate().getEncoded()
        );

        writePem(
                publicKeyPath,
                "PUBLIC KEY",
                keyPair.getPublic().getEncoded()
        );

        JwtSigningProperties signingProperties =
                new JwtSigningProperties(
                        new FileSystemResource(
                                privateKeyPath.toFile()
                        ),
                        new FileSystemResource(
                                publicKeyPath.toFile()
                        )
                );

        JwtEncoder jwtEncoder =
                new JwtConfig()
                        .jwtEncoder(signingProperties);

        Instant issuedAt =
                Instant.now();

        JwtClaimsSet claims =
                JwtClaimsSet.builder()
                        .issuer("cloudbank-auth-service")
                        .subject("test-user-id")
                        .issuedAt(issuedAt)
                        .expiresAt(issuedAt.plusSeconds(300))
                        .id("test-jti")
                        .build();

        Jwt encodedJwt =
                jwtEncoder.encode(
                        JwtEncoderParameters.from(claims)
                );

        RSAPublicKey publicKey =
                (RSAPublicKey) keyPair.getPublic();

        NimbusJwtDecoder jwtDecoder =
                NimbusJwtDecoder
                        .withPublicKey(publicKey)
                        .signatureAlgorithm(
                                SignatureAlgorithm.RS256
                        )
                        .build();

        Jwt decodedJwt =
                jwtDecoder.decode(
                        encodedJwt.getTokenValue()
                );

        assertFalse(
                encodedJwt.getTokenValue().isBlank()
        );

        assertEquals(
                "RS256",
                decodedJwt.getHeaders().get("alg")
        );

        assertEquals(
                "test-user-id",
                decodedJwt.getSubject()
        );

        assertEquals(
                "cloudbank-auth-service",
                decodedJwt.getClaims().get("iss")
        );

        assertEquals(
                "test-jti",
                decodedJwt.getId()
        );
    }

    private void writePem(
            Path path,
            String type,
            byte[] encodedKey
    ) throws IOException {
        String encoded =
                Base64.getMimeEncoder(
                                64,
                                new byte[]{'\n'}
                        )
                        .encodeToString(encodedKey);

        String pem =
                "-----BEGIN " + type + "-----\n"
                        + encoded
                        + "\n-----END " + type + "-----\n";

        Files.writeString(
                path,
                pem
        );
    }
}
