package com.ecmsp.paymentservice.payment.adapter.publisher.kafka;

import com.ecmsp.paymentservice.api.kafka.KafkaOrderCreatedEvent;
import com.ecmsp.paymentservice.payment.domain.Payment;
import com.ecmsp.paymentservice.payment.domain.UserId;
import com.ecmsp.paymentservice.payment.domain.Currency;
import com.ecmsp.paymentservice.payment.domain.OrderId;
import com.ecmsp.paymentservice.payment.domain.PaymentToCreate;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component
public class KafkaPaymentEventMapper {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private final Clock clock;

    public KafkaPaymentEventMapper(Clock clock) {
        this.clock = clock;
    }

    public PaymentToCreate mapToPaymentToCreate(KafkaOrderCreatedEvent event) {
        return new PaymentToCreate(
                new OrderId(UUID.fromString(event.orderId())),
                new UserId(UUID.fromString(event.clientId())),
                event.orderTotal(),
                Currency.PLN,
                LocalDateTime.from(clock.instant())
        );
    }

    public KafkaPaymentProcessedSucceededEvent mapToSuccessEvent(Payment payment, String orderId) {
        return new KafkaPaymentProcessedSucceededEvent(
                orderId,
                payment.id().toString(),
                LocalDateTime.from(clock.instant()).format(DATE_FORMATTER)
        );
    }

    public KafkaPaymentProcessedFailedEvent mapToFailureEvent(String orderId) {
        return new KafkaPaymentProcessedFailedEvent(
                orderId,
                null,
                LocalDateTime.from(clock.instant()).format(DATE_FORMATTER)
        );
    }
}
