package com.ecmsp.paymentservice.payment.adapter.publisher.kafka;

record KafkaPaymentProcessedSucceededEvent(
        String orderId,
        String paymentId,
        String processedAt
) {
}
