package com.cloudbank.gateway.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers =
                SecurityConfigTest.SecurityProbeController.class
)
@Import({
        SecurityConfig.class,
        SecurityConfigTest.SecurityProbeController.class
})
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @BeforeEach
    void configureJwtDecoder() {
        Instant issuedAt =
                Instant.now();

        Jwt jwt =
                Jwt.withTokenValue("valid-token")
                        .header(
                                "alg",
                                "RS256"
                        )
                        .subject("test-user-id")
                        .issuedAt(issuedAt)
                        .expiresAt(
                                issuedAt.plusSeconds(300)
                        )
                        .build();

        when(
                jwtDecoder.decode("valid-token")
        ).thenReturn(jwt);
    }

    @Test
    void shouldAllowLoginWithoutBearerToken()
            throws Exception {
        mockMvc.perform(
                        post(
                                "/api/v1/auth/login"
                        )
                )
                .andExpect(
                        status().isOk()
                );
    }

    @Test
    void shouldAllowRegistrationWithoutBearerToken()
            throws Exception {
        mockMvc.perform(
                        post(
                                "/api/v1/auth/register"
                        )
                )
                .andExpect(
                        status().isOk()
                );
    }

    @Test
    void shouldRequireAuthenticationForApiRequest()
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
    void shouldAllowApiRequestWithValidBearerToken()
            throws Exception {
        mockMvc.perform(
                        get(
                                "/api/v1/customers/me"
                        )
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        "Bearer valid-token"
                                )
                )
                .andExpect(
                        status().isOk()
                );
    }

    @Test
    void shouldNotTreatGetLoginAsPublic()
            throws Exception {
        mockMvc.perform(
                        get(
                                "/api/v1/auth/login"
                        )
                )
                .andExpect(
                        status().isUnauthorized()
                );
    }

    @RestController
    static class SecurityProbeController {

        @PostMapping("/api/v1/auth/login")
        String login() {
            return "login";
        }

        @PostMapping("/api/v1/auth/register")
        String register() {
            return "register";
        }

        @GetMapping("/api/v1/customers/me")
        String customer() {
            return "customer";
        }

        @GetMapping("/api/v1/auth/login")
        String loginWithWrongMethod() {
            return "login";
        }
    }
}
