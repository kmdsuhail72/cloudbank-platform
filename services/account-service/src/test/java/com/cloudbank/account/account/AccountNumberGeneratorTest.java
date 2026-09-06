package com.cloudbank.account.account;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccountNumberGeneratorTest {

    private final AccountNumberGenerator generator =
            new AccountNumberGenerator();

    @Test
    void shouldGenerateExpectedAccountNumberFormat() {

        String accountNumber =
                generator.generate();

        assertTrue(
                accountNumber.matches(
                        "CB[0-9A-F]{32}"
                )
        );
    }

    @Test
    void shouldGenerateDifferentAccountNumbers() {

        String first =
                generator.generate();

        String second =
                generator.generate();

        assertNotEquals(
                first,
                second
        );
    }
}
