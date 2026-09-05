package com.cloudbank.customer.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = SecurityProbeController.class
)
@Import(SecurityConfig.class)
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @BeforeEach
    void setUp() {
        Instant now = Instant.now();

        Jwt validJwt =
                Jwt.withTokenValue(
                                "valid-token"
                        )
                        .header(
                                "alg",
                                "RS256"
                        )
                        .subject(
                                "11111111-1111-1111-1111-111111111111"
                        )
                        .issuedAt(
                                now
                        )
                        .expiresAt(
                                now.plusSeconds(900)
                        )
                        .claim(
                                "role",
                                "CUSTOMER"
                        )
                        .build();

        when(
                jwtDecoder.decode(
                        "valid-token"
                )
        ).thenReturn(
                validJwt
        );
    }

    @Test
    void shouldAllowHealthWithoutToken()
            throws Exception {

        mockMvc.perform(
                        get(
                                "/actuator/health"
                        )
                )
                .andExpect(
                        status().isOk()
                );
    }

    @Test
    void shouldRejectCustomerApiWithoutToken()
            throws Exception {

        mockMvc.perform(
                        get(
                                "/api/v1/customers/me"
                        )
                )
                .andExpect(
                        status().isUnauthorized()
                );
    }

    @Test
    void shouldAllowCustomerApiWithValidBearerToken()
            throws Exception {

        mockMvc.perform(
                        get(
                                "/api/v1/customers/me"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer valid-token"
                                )
                )
                .andExpect(
                        status().isOk()
                );
    }

    @Test
    void shouldRejectInvalidBearerToken()
            throws Exception {

        when(
                jwtDecoder.decode(
                        "invalid-token"
                )
        ).thenThrow(
                new BadJwtException(
                        "invalid token"
                )
        );

        mockMvc.perform(
                        get(
                                "/api/v1/customers/me"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer invalid-token"
                                )
                )
                .andExpect(
                        status().isUnauthorized()
                );
    }

    @Test
    void shouldRejectDirectErrorRequest()
            throws Exception {

        mockMvc.perform(
                        get(
                                "/error"
                        )
                )
                .andExpect(
                        status().isUnauthorized()
                );
    }

    @Test
    void shouldDenyUnrelatedPathEvenWithValidToken()
            throws Exception {

        mockMvc.perform(
                        get(
                                "/internal/probe"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer valid-token"
                                )
                )
                .andExpect(
                        status().isForbidden()
                );
    }
}
