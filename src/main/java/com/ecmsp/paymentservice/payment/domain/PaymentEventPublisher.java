package com.ecmsp.paymentservice.payment.domain;

public interface PaymentEventPublisher {
    void publish(PaymentEvent paymentEvent);
}
