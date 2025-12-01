package com.ecmsp.paymentservice.payment.adapter.publisher.kafka;

import com.ecmsp.paymentservice.payment.domain.PaymentEventPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
@ConditionalOnProperty(
    prefix = "payment.event-publisher",
    name = "type",
    havingValue = "kafka")

class KafkaPaymentEventPublisherConfiguration {

    @Configuration
    @ConditionalOnProperty(
            name = "payment.event-publisher.outbox-type",
            havingValue = "db"
    )
    @Import({DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
    @EnableJpaRepositories(basePackages = "com.ecmsp.paymentservice.payment.adapter.publisher.kafka")
    @EntityScan(basePackages = "com.ecmsp.paymentservice.payment.adapter.publisher.kafka")
    static class JpaConfiguration {
    }


    @Bean
    Outbox outbox(
        KafkaEventEntityRepository kafkaEventEntityRepository,
        @Value("${kafka.topic.payment-processed-succeeded}") String paymentProcessedSucceededTopic,
        @Value("${kafka.topic.payment-processed-failed}") String paymentProcessedFailedTopic,
        ObjectMapper objectMapper
    ) {
        return new Outbox(kafkaEventEntityRepository, objectMapper, paymentProcessedSucceededTopic, paymentProcessedFailedTopic);
    }

    @Bean
    OutboxProcessor outboxProcessor(
        KafkaEventEntityRepository kafkaEventEntityRepository,
        KafkaTemplate<String, String> kafkaTemplate
    ) {
        return new OutboxProcessor(kafkaEventEntityRepository, kafkaTemplate);
    }

    @Bean
    PaymentEventPublisher paymentEventPublisher(Outbox outbox) {
        return new KafkaPaymentEventPublisher(outbox);
    }

}
