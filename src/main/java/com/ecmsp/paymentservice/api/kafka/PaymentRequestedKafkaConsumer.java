package com.ecmsp.paymentservice.api.kafka;

import com.ecmsp.paymentservice.payment.domain.PaymentToCreate;
import com.ecmsp.paymentservice.payment.domain.OrderId;
import com.ecmsp.paymentservice.payment.domain.ClientId;
import com.ecmsp.paymentservice.payment.domain.Currency;
import com.ecmsp.paymentservice.payment.adapter.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentRequestedKafkaConsumer {

    private final PaymentService paymentService;
    private final PaymentEventPublisher paymentEventPublisher;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @KafkaListener(topics = "${kafka.topic.payment-request}")
    public void consume(@Payload PaymentRequestedKafkaEvent paymentRequestedEvent) {
        log.info("Received payment request for order: {}", paymentRequestedEvent.orderId());
        
        try {
            PaymentToCreate paymentToCreate = new PaymentToCreate(
                    new OrderId(UUID.fromString(paymentRequestedEvent.orderId())),
                    new ClientId(UUID.fromString(paymentRequestedEvent.clientId())),
                    paymentRequestedEvent.amount(),
                    Currency.PLN,
                    LocalDateTime.now()
            );

            var paymentResponse = paymentService.createPaymentFromDomain(paymentToCreate);

            PaymentProcessedKafkaEventSucceeded successEvent = new PaymentProcessedKafkaEventSucceeded(
                    paymentRequestedEvent.orderId(),
                    paymentResponse.getId().toString(),
                    LocalDateTime.now().format(DATE_FORMATTER)
            );
            
            paymentEventPublisher.publishPaymentProcessedSuccess(successEvent);
            
            log.info("Payment created successfully for order: {} with payment ID: {}", 
                    paymentRequestedEvent.orderId(), paymentResponse.getId());
            
        } catch (Exception e) {
            log.error("Failed to process payment request for order: {}", paymentRequestedEvent.orderId(), e);

            PaymentProcessedKafkaEventFailed failureEvent = new PaymentProcessedKafkaEventFailed(
                    paymentRequestedEvent.orderId(),
                    null,
                    LocalDateTime.now().format(DATE_FORMATTER)
            );
            
            paymentEventPublisher.publishPaymentProcessedFailure(failureEvent);
        }
    }
}
