package com.ecmsp.paymentservice.payment.adapter.publisher.kafka;

import com.ecmsp.paymentservice.payment.domain.PaymentEvent;
import com.ecmsp.paymentservice.payment.domain.PaymentEventPublisher;

class KafkaPaymentEventPublisher implements PaymentEventPublisher {

    private final Outbox outbox;

    public KafkaPaymentEventPublisher(Outbox outbox) {
        this.outbox = outbox;
    }

    @Override
    public void publish(PaymentEvent event) {
        outbox.publish(event);
    }
}
