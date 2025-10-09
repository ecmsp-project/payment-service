package com.ecmsp.paymentservice.api.kafka;

public interface PaymentEventPublisher {
    void publishPaymentProcessedSuccess(PaymentProcessedKafkaEventSucceeded event);
    void publishPaymentProcessedFailure(PaymentProcessedKafkaEventFailed event);
}
