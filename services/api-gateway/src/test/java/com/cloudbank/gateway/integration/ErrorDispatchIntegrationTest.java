package com.cloudbank.gateway.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@SpringBootTest(
        webEnvironment =
                SpringBootTest.WebEnvironment.RANDOM_PORT
)
class ErrorDispatchIntegrationTest {

    @LocalServerPort
    private int port;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    private final HttpClient httpClient =
            HttpClient.newHttpClient();

    @BeforeEach
    void configureJwtDecoder() {
        Instant now = Instant.now();

        Jwt jwt =
                Jwt.withTokenValue("valid-token")
                        .header(
                                "alg",
                                "RS256"
                        )
                        .subject(
                                "integration-user"
                        )
                        .issuedAt(now)
                        .expiresAt(
                                now.plusSeconds(900)
                        )
                        .claim(
                                "role",
                                "CUSTOMER"
                        )
                        .build();

        when(
                jwtDecoder.decode("valid-token")
        ).thenReturn(jwt);
    }

    @Test
    void shouldReturnNotFoundAfterAuthenticatingMissingApiRoute()
            throws Exception {
        HttpRequest request =
                HttpRequest.newBuilder(
                                URI.create(
                                        "http://localhost:"
                                                + port
                                                + "/api/v1/nonexistent-route"
                                )
                        )
                        .header(
                                "Authorization",
                                "Bearer valid-token"
                        )
                        .GET()
                        .build();

        HttpResponse<String> response =
                httpClient.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                );

        assertEquals(
                404,
                response.statusCode()
        );
    }
}
