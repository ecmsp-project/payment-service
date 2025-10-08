package com.ecmsp.paymentservice.payment.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public record PaymentToCreate(
        OrderId orderId,
        ClientId clientId,
        BigDecimal amount,
        Currency currency,
        LocalDateTime requestedAt
) {
    private static final BigDecimal MINIMAL_TOTAL_VALUE = new BigDecimal("0.01");

    public PaymentToCreate {
        if (amount.compareTo(MINIMAL_TOTAL_VALUE) < 0) {
            throw new IllegalArgumentException("Amount must be at least 0.01");
        }
    }
}
