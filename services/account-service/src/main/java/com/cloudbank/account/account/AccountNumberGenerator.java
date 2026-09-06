package com.cloudbank.account.account;

import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.UUID;

@Component
public class AccountNumberGenerator {

    public String generate() {
        return "CB"
                + UUID.randomUUID()
                .toString()
                .replace(
                        "-",
                        ""
                )
                .toUpperCase(
                        Locale.ROOT
                );
    }
}
