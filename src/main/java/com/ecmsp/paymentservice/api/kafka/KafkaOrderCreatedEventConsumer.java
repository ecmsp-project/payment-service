package com.ecmsp.paymentservice.api.kafka;

import com.ecmsp.paymentservice.payment.adapter.publisher.kafka.KafkaPaymentEventMapper;
import com.ecmsp.paymentservice.payment.domain.PaymentFacade;
import com.ecmsp.paymentservice.payment.domain.PaymentToCreate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaOrderCreatedEventConsumer {

    private final PaymentFacade paymentFacade;
    private final KafkaPaymentEventMapper kafkaEventMapper;

    @KafkaListener(topics = "${kafka.topic.order-created}")
    public void consume(@Payload KafkaOrderCreatedEvent createdOrderEvent) {
        log.info("Received payment request for order: {}", createdOrderEvent.orderId());

        try {
            PaymentToCreate paymentToCreate = kafkaEventMapper.mapToPaymentToCreate(createdOrderEvent);
            paymentFacade.processPaymentRequest(paymentToCreate);

            log.info("Payment request consumed and processed for order: {}", createdOrderEvent.orderId());

        } catch (Exception e) {
            log.error("Failed to consume payment request for order: {}", createdOrderEvent.orderId(), e);
        }
    }
}
