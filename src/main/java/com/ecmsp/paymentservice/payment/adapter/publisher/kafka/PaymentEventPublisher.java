package com.ecmsp.paymentservice.payment.adapter.publisher.kafka;

public interface PaymentEventPublisher {
    void publishPaymentProcessedSuccess(KafkaPaymentProcessedSucceededEvent event);
    void publishPaymentProcessedFailure(KafkaPaymentProcessedFailedEvent event);
}
