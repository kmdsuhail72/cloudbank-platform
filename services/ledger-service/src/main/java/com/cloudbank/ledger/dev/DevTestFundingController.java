package com.cloudbank.ledger.dev;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
@ConditionalOnProperty(
        prefix = "cloudbank.dev-test-funding",
        name = "enabled",
        havingValue = "true"
)
public class DevTestFundingController {

    private final DevTestFundingService fundingService;

    public DevTestFundingController(
            DevTestFundingService fundingService
    ) {
        this.fundingService =
                Objects.requireNonNull(
                        fundingService
                );
    }

    @PostMapping(
            "/api/v1/dev/test-funding"
    )
    public ResponseEntity<DevTestFundingResult> fund(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody DevTestFundingRequest request
    ) {
        if (jwt == null) {
            throw new DevTestFundingException(
                    "Authentication is required"
            );
        }

        return ResponseEntity.ok(
                fundingService.fund(
                        jwt.getTokenValue(),
                        request
                )
        );
    }
}
