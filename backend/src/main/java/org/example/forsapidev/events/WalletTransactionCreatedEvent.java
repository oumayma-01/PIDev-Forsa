package org.example.forsapidev.events;

import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
public class WalletTransactionCreatedEvent {
    private final Long clientId;
    private final String transactionType;
    private final Double amount;
}
