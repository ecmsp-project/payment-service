package com.ecmsp.paymentservice.payment.adapter.publisher.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
@ConditionalOnProperty(
        prefix = "payment.event-publisher",
        name = "type",
        havingValue = "kafka"
)
class KafkaPaymentEventPublisherConfiguration {

    @Bean
    public KafkaPaymentEventPublisher kafkaPaymentEventPublisher(
            KafkaTemplate<String, KafkaPaymentProcessedSucceededEvent> paymentProcessedSucceededaKafkaTemplate,
            KafkaTemplate<String, KafkaPaymentProcessedFailedEvent> paymentProcessedFailedKafkaTemplate,
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper) {
        return new KafkaPaymentEventPublisher(paymentProcessedSucceededaKafkaTemplate, paymentProcessedFailedKafkaTemplate, kafkaTemplate, objectMapper);
    }

}
