package com.cloudbank.auth;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class AuthServiceApplicationTests {

    @MockitoBean
    private JwtEncoder jwtEncoder;

    @Test
    void contextLoads() {
    }

}
