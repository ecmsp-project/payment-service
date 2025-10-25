package com.ecmsp.paymentservice.payment.domain;

import java.util.UUID;

public record CorrelationId(
        UUID value
) {
    @Override
    public String toString() {
        return value.toString();
    }

}
