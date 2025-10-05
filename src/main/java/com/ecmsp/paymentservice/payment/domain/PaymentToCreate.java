package com.ecmsp.paymentservice.payment.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public record PaymentToCreate(
        OrderId orderId,
        ClientId clientId,
        BigDecimal amount,
        LocalDateTime requestedAt
) {
    public PaymentToCreate {
        Objects.requireNonNull(orderId, "Order ID cannot be null");
        Objects.requireNonNull(clientId, "Client ID cannot be null");
        Objects.requireNonNull(amount, "Amount cannot be null");
        Objects.requireNonNull(requestedAt, "Requested at cannot be null");
        
        if (amount.compareTo(BigDecimal.valueOf(0.01)) < 0) {
            throw new IllegalArgumentException("Amount must be at least 0.01");
        }
    }
}
