package com.ecmsp.paymentservice.payment.adapter.publisher.kafka;

import com.ecmsp.paymentservice.payment.domain.PaymentEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
class Outbox {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final KafkaEventEntityRepository kafkaEventEntityRepository;
    private final ObjectMapper objectMapper;

    private final String paymentProcessedSucceededTopic;
    private final String paymentProcessedFailedTopic;

    public Outbox(
        KafkaEventEntityRepository kafkaEventEntityRepository,
        ObjectMapper objectMapper,
        String paymentProcessedSucceededTopic,
        String paymentProcessedFailedTopic
    ) {
        this.kafkaEventEntityRepository = kafkaEventEntityRepository;
        this.objectMapper = objectMapper;
        this.paymentProcessedSucceededTopic = paymentProcessedSucceededTopic;
        this.paymentProcessedFailedTopic = paymentProcessedFailedTopic;
    }

    public void publish(PaymentEvent event) {
        try {
            doPublish(event);
        } catch (Exception e) {
            log.error("Failed to publish event to outbox.", e);
        }

    }

    private void doPublish(PaymentEvent event) throws Exception {
        var eventToSave = switch (event) {
            case PaymentEvent.PaymentProcessedSucceeded paymentProcessedSucceeded -> {
                KafkaPaymentProcessedSucceededEvent kafkaEvent = new KafkaPaymentProcessedSucceededEvent(
                    paymentProcessedSucceeded.orderId().value().toString(),
                    paymentProcessedSucceeded.paymentId().value().toString(),
                    paymentProcessedSucceeded.processedAt().format(DATE_FORMATTER)
                );

                yield KafkaEventEntity.builder()
                    .topic(paymentProcessedSucceededTopic)
                    .key(kafkaEvent.paymentId())
                    .payload(objectMapper.writeValueAsString(kafkaEvent))
                    .createdAt(LocalDateTime.now())
                    .build();
            }
            case PaymentEvent.PaymentProcessedFailed paymentProcessedFailed -> {
                KafkaPaymentProcessedFailedEvent kafkaEvent = new KafkaPaymentProcessedFailedEvent(
                    paymentProcessedFailed.orderId().value().toString(),
                    paymentProcessedFailed.paymentId().value().toString(),
                    paymentProcessedFailed.processedAt().format(DATE_FORMATTER)
                );

                yield KafkaEventEntity.builder()
                    .topic(paymentProcessedFailedTopic)
                    .key(kafkaEvent.paymentId())
                    .payload(objectMapper.writeValueAsString(kafkaEvent))
                    .createdAt(LocalDateTime.now())
                    .build();
            }
        };

        kafkaEventEntityRepository.save(eventToSave);
    }

}
