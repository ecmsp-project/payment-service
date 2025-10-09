package com.ecmsp.paymentservice.api.kafka;

public record PaymentProcessedKafkaEventSucceeded(
        String orderId,
        String paymentId,
        String processedAt
) {
}
