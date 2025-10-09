package com.ecmsp.paymentservice.api.kafka;

import java.math.BigDecimal;

public record PaymentRequestedKafkaEvent(
        String orderId,
        String clientId,
        BigDecimal amount,
        String requestedAt
) {
}
