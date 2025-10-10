package com.ecmsp.paymentservice.api.kafka;

import java.math.BigDecimal;

public record KafkaOrderCreatedEvent(
        String orderId,
        String clientId,
        BigDecimal orderTotal,
        String requestedAt
) {
}
