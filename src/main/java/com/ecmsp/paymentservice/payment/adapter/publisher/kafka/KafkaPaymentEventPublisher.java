package com.ecmsp.paymentservice.payment.adapter.publisher.kafka;

import com.ecmsp.paymentservice.payment.domain.PaymentEvent;
import com.ecmsp.paymentservice.payment.domain.PaymentEventPublisher;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.time.format.DateTimeFormatter;

@Slf4j
class KafkaPaymentEventPublisher implements PaymentEventPublisher {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final KafkaTemplate<String, KafkaPaymentProcessedSucceededEvent> successKafkaTemplate;
    private final KafkaTemplate<String, KafkaPaymentProcessedFailedEvent> failureKafkaTemplate;

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;


    @Value("${kafka.topic.payment-processed-succeeded}")
    private String paymentProcessedSucceededTopic;

    @Value("${kafka.topic.payment-processed-failed}")
    private String paymentProcessedFailedTopic;

    public KafkaPaymentEventPublisher(KafkaTemplate<String, KafkaPaymentProcessedSucceededEvent> successKafkaTemplate, KafkaTemplate<String, KafkaPaymentProcessedFailedEvent> failureKafkaTemplate, KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.successKafkaTemplate = successKafkaTemplate;
        this.failureKafkaTemplate = failureKafkaTemplate;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
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

                sendEvent(paymentProcessedSucceededTopic, kafkaEvent.paymentId(), kafkaEvent);
            }
            case PaymentEvent.PaymentProcessedFailed paymentProcessedFailed -> {
                KafkaPaymentProcessedFailedEvent kafkaEvent = new KafkaPaymentProcessedFailedEvent(
                        paymentProcessedFailed.orderId().value().toString(),
                        paymentProcessedFailed.paymentId().value().toString(),
                        paymentProcessedFailed.processedAt().format(DATE_FORMATTER)
                );
                sendEvent(paymentProcessedFailedTopic,kafkaEvent.paymentId(), kafkaEvent);
            }
        }

    }

    private void sendEvent(String topic, String key, Object event) {
        try {
            String eventJson = objectMapper.writeValueAsString(event);

//            TODO: add to set correlationId for tracing
//            Message<String> message = MessageBuilder
//                    .withPayload(eventJson)
//                    .setHeader("X-Correlation-Id", correlationId)
//                    .build();

            kafkaTemplate.send(topic, key, eventJson);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize event", e);
        }
    }
}
