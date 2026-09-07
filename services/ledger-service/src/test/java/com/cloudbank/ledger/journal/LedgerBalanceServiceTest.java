package com.cloudbank.ledger.journal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LedgerBalanceServiceTest {

    @Mock
    private LedgerPostingRepository postingRepository;

    private LedgerBalanceService service;

    @BeforeEach
    void setUp() {
        service =
                new LedgerBalanceService(
                        postingRepository
                );
    }

    @Test
    void shouldReturnDerivedPostedBalance() {
        UUID accountId =
                UUID.randomUUID();

        when(
                postingRepository
                        .calculatePostedBalance(
                                accountId,
                                "INR"
                        )
        ).thenReturn(
                new BigDecimal(
                        "60.0000"
                )
        );

        LedgerBalanceResult result =
                service.getPostedBalance(
                        accountId,
                        "inr"
                );

        assertEquals(
                accountId,
                result.accountId()
        );

        assertEquals(
                "INR",
                result.currency()
        );

        assertEquals(
                new BigDecimal(
                        "60.0000"
                ),
                result.postedBalance()
        );

        verify(
                postingRepository
        ).calculatePostedBalance(
                accountId,
                "INR"
        );
    }

    @Test
    void shouldReturnZeroWhenNoPostingsExist() {
        UUID accountId =
                UUID.randomUUID();

        when(
                postingRepository
                        .calculatePostedBalance(
                                accountId,
                                "INR"
                        )
        ).thenReturn(
                BigDecimal.ZERO
        );

        LedgerBalanceResult result =
                service.getPostedBalance(
                        accountId,
                        "INR"
                );

        assertEquals(
                new BigDecimal(
                        "0.0000"
                ),
                result.postedBalance()
        );
    }

    @Test
    void shouldRejectInvalidCurrency() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.getPostedBalance(
                        UUID.randomUUID(),
                        "RUPEE"
                )
        );
    }

    @Test
    void shouldRejectMissingAccountId() {
        assertThrows(
                NullPointerException.class,
                () -> service.getPostedBalance(
                        null,
                        "INR"
                )
        );
    }
}
