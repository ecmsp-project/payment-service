package com.ecmsp.paymentservice.payment.adapter.publisher.kafka;

record KafkaPaymentProcessedFailedEvent(
        String orderId,
        String paymentId,
        String processedAt
) {
}
