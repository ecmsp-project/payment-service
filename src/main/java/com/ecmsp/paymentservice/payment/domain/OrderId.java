package com.ecmsp.paymentservice.payment.domain;

import java.util.Objects;

public record OrderId(Long value) {
    public OrderId {
        Objects.requireNonNull(value, "Order ID cannot be null");
        if (value <= 0) {
            throw new IllegalArgumentException("Order ID must be positive");
        }
    }
}
