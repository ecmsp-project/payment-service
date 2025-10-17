package com.ecmsp.paymentservice.api.kafka;

import com.ecmsp.paymentservice.payment.domain.Context;
import com.ecmsp.paymentservice.payment.domain.CorrelationId;
import com.ecmsp.paymentservice.payment.domain.PaymentFacade;
import com.ecmsp.paymentservice.payment.domain.PaymentToCreate;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
class KafkaOrderCreatedEventConsumer {

    private final PaymentFacade paymentFacade;
    private final ObjectMapper objectMapper;

    public KafkaOrderCreatedEventConsumer(PaymentFacade paymentFacade, ObjectMapper objectMapper) {
        this.paymentFacade = paymentFacade;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "${kafka.topic.order-created}")
    public void consume(@Payload String createdOrderEventJson, @Header(value = "X-Correlation-Id", required = false) String correlationId) throws JsonProcessingException {
        KafkaOrderCreatedEvent createdOrderEvent = objectMapper.readValue(createdOrderEventJson, KafkaOrderCreatedEvent.class);
        log.info("Received payment request for order: {}", createdOrderEvent.orderId());

        try {
            // Handle null correlation ID
            String effectiveCorrelationId = correlationId != null ? correlationId : UUID.randomUUID().toString();
            MDC.put("correlationId", effectiveCorrelationId);
            log.info("Processing orderCreated event - CorrelationID: {}", effectiveCorrelationId);

            PaymentToCreate paymentToCreate = KafkaOrderCreatedEvent.toPayment(createdOrderEvent);
            Context context = new Context(new CorrelationId(UUID.fromString(effectiveCorrelationId)));
            paymentFacade.createPayment(paymentToCreate, context);

            MDC.clear();
            log.info("Finished processing orderCreated event - CorrelationID: {}", effectiveCorrelationId);

        } catch (Exception e) {
            log.error("Failed to consume payment request for order: {}", createdOrderEvent.orderId(), e);
        }
    }
}
