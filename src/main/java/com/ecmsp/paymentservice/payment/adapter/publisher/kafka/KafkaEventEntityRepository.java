package com.ecmsp.paymentservice.payment.adapter.publisher.kafka;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

interface KafkaEventEntityRepository {

    List<KafkaEventEntity> findByProcessedFalseOrderByCreatedAtAsc();

    void deleteProcessedEventsBefore(LocalDateTime before);

    void markAsProcessed(UUID eventId, LocalDateTime processedAt);

    KafkaEventEntity save(KafkaEventEntity entity);

}
