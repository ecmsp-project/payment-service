package com.ecmsp.paymentservice.payment.adapter.publisher.kafka;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "kafka_outbox")
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
class KafkaEventEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "event_id")
    private UUID eventId;

    @Column(name = "payload", columnDefinition = "TEXT")
    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    private String payload;

    @Column(name = "key", columnDefinition = "TEXT")
    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    private String key;

    @Column(name = "topic", columnDefinition = "TEXT")
    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    private String topic;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "processed", nullable = false)
    @Builder.Default
    private boolean processed = false;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;
}