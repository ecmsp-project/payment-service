package com.ecmsp.paymentservice.payment.adapter.publisher.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.util.List;


@Slf4j
class OutboxProcessor {

    private final KafkaEventEntityRepository kafkaEventEntityRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public OutboxProcessor(
        KafkaEventEntityRepository kafkaEventEntityRepository,
        KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaEventEntityRepository = kafkaEventEntityRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedDelay = 2000)
    public void processOutboxEvents() {
        try {
            List<KafkaEventEntity> unprocessedEvents = kafkaEventEntityRepository.findByProcessedFalseOrderByCreatedAtAsc();

            if (unprocessedEvents.isEmpty()) {
                return;
            }

            log.debug("Processing {} unprocessed outbox events", unprocessedEvents.size());

            for (KafkaEventEntity event : unprocessedEvents) {
                processEvent(event);
            }

        } catch (Exception e) {
            log.error("Error processing outbox events", e);
        }
    }

    private void processEvent(KafkaEventEntity event) {
        try {
            kafkaTemplate.send(event.getTopic(), event.getKey(), event.getPayload());
            kafkaEventEntityRepository.markAsProcessed(event.getEventId(), LocalDateTime.now());
        } catch (Exception e) {
            log.error("Failed to process outbox event - will retry: id={}", event.getEventId(), e);
        }
    }

}
