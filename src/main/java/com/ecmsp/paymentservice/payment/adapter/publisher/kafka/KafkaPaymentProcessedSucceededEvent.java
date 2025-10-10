package com.ecmsp.paymentservice.payment.adapter.publisher.kafka;

public record KafkaPaymentProcessedSucceededEvent(
        String orderId,
        String paymentId,
        String processedAt
) {
}
