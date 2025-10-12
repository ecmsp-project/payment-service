package com.ecmsp.paymentservice.payment.adapter.publisher.kafka;

import com.ecmsp.paymentservice.payment.domain.PaymentEvent;
import com.ecmsp.paymentservice.payment.domain.PaymentEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Slf4j
class KafkaPaymentEventPublisher implements PaymentEventPublisher {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final KafkaTemplate<String, KafkaPaymentProcessedSucceededEvent> successKafkaTemplate;
    private final KafkaTemplate<String, KafkaPaymentProcessedFailedEvent> failureKafkaTemplate;

    @Value("${kafka.topic.payment-processed-succeeded}")
    private String paymentProcessedSucceededTopic;

    @Value("${kafka.topic.payment-processed-failed}")
    private String paymentProcessedFailedTopic;

    public KafkaPaymentEventPublisher(KafkaTemplate<String, KafkaPaymentProcessedSucceededEvent> successKafkaTemplate, KafkaTemplate<String, KafkaPaymentProcessedFailedEvent> failureKafkaTemplate) {
        this.successKafkaTemplate = successKafkaTemplate;
        this.failureKafkaTemplate = failureKafkaTemplate;
    }

    @Override
    public void publish(PaymentEvent paymentEvent) {
        switch(paymentEvent){
            case PaymentEvent.PaymentProcessedSucceeded paymentProcessedSucceeded -> {
                KafkaPaymentProcessedSucceededEvent kafkaEvent = new KafkaPaymentProcessedSucceededEvent(
                        paymentProcessedSucceeded.orderId().value().toString(),
                        paymentProcessedSucceeded.paymentId().value().toString(),
                        paymentProcessedSucceeded.processedAt().format(DATE_FORMATTER)

                );
                successKafkaTemplate.send(paymentProcessedSucceededTopic, kafkaEvent.paymentId(), kafkaEvent);
            }
            case PaymentEvent.PaymentProcessedFailed paymentProcessedFailed -> {
                KafkaPaymentProcessedFailedEvent kafkaEvent = new KafkaPaymentProcessedFailedEvent(
                        paymentProcessedFailed.orderId().value().toString(),
                        paymentProcessedFailed.paymentId().value().toString(),
                        paymentProcessedFailed.processedAt().format(DATE_FORMATTER)
                );
                failureKafkaTemplate.send(paymentProcessedFailedTopic,kafkaEvent.paymentId(), kafkaEvent);
            }
        }

    }
}
