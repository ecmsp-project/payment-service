package com.ecmsp.paymentservice.payment.adapter.publisher.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaPaymentEventPublisher implements PaymentEventPublisher {

    private final KafkaTemplate<String, KafkaPaymentProcessedSucceededEvent> successKafkaTemplate;
    private final KafkaTemplate<String, KafkaPaymentProcessedFailedEvent> failureKafkaTemplate;

    @Value("${kafka.topic.payment-processed-succeeded}")
    private String paymentProcessedSucceededTopic;

    @Value("${kafka.topic.payment-processed-failed}")
    private String paymentProcessedFailedTopic;

    @Override
    public void publishPaymentProcessedSuccess(KafkaPaymentProcessedSucceededEvent event) {
        log.info("Publishing payment processed success event for order: {}", event.orderId());
        successKafkaTemplate.send(paymentProcessedSucceededTopic, event.orderId(), event);
    }

    @Override
    public void publishPaymentProcessedFailure(KafkaPaymentProcessedFailedEvent event) {
        log.info("Publishing payment processed failure event for order: {}", event.orderId());
        failureKafkaTemplate.send(paymentProcessedFailedTopic, event.orderId(), event);
    }
}
