package com.ecmsp.paymentservice.payment.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentToCreate(
        OrderId orderId,
        UserId userId,
        BigDecimal orderTotal,
        Currency currency,
        LocalDateTime requestedAt
) {
    private static final BigDecimal MINIMAL_TOTAL_VALUE = new BigDecimal("0.01");

    public PaymentToCreate {
        if (orderTotal.compareTo(MINIMAL_TOTAL_VALUE) < 0) {
            throw new IllegalArgumentException("Order total must be at least 0.01");
        }
    }
}
