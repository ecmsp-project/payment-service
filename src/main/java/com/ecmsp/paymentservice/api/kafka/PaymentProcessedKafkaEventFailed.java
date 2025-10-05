package com.ecmsp.paymentservice.api.kafka;

public record PaymentProcessedKafkaEventFailed(
        String orderId,
        String paymentId,
        String processedAt
) {
}
