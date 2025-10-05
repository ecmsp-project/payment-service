package com.ecmsp.paymentservice.payment.domain;

import java.util.Objects;

public record ClientId(Long value) {
    public ClientId {
        Objects.requireNonNull(value, "Client ID cannot be null");
        if (value <= 0) {
            throw new IllegalArgumentException("Client ID must be positive");
        }
    }
}
