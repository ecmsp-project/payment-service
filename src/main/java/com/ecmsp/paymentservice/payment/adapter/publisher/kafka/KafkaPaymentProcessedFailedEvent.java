package com.ecmsp.paymentservice.payment.adapter.publisher.kafka;

public record KafkaPaymentProcessedFailedEvent(
        String orderId,
        String paymentId,
        String processedAt
) {
}
