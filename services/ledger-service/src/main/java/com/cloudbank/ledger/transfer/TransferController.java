package com.cloudbank.ledger.transfer;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
public class TransferController {

    private final TransferService transferService;

    public TransferController(
            TransferService transferService
    ) {
        this.transferService =
                Objects.requireNonNull(
                        transferService
                );
    }

    @PostMapping(
            "/api/v1/transfers"
    )
    public ResponseEntity<TransferResult> transfer(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody TransferRequest request
    ) {
        if (request == null) {
            throw new InvalidTransferException(
                    "Transfer request is required"
            );
        }

        TransferResult result =
                transferService.transfer(
                        jwt.getTokenValue(),
                        request.toCommand()
                );

        return ResponseEntity.ok(
                result
        );
    }
}
