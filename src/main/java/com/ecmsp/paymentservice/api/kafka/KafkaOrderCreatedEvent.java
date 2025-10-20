package com.ecmsp.paymentservice.api.kafka;

import com.ecmsp.paymentservice.payment.domain.Currency;
import com.ecmsp.paymentservice.payment.domain.OrderId;
import com.ecmsp.paymentservice.payment.domain.PaymentToCreate;
import com.ecmsp.paymentservice.payment.domain.UserId;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public record KafkaOrderCreatedEvent(
        String orderId,
        String clientId,
        BigDecimal orderTotal,
        String requestedAt
) {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;


    public static PaymentToCreate toPayment(KafkaOrderCreatedEvent kafkaOrderCreatedEvent) {
        return new PaymentToCreate(
                new OrderId(UUID.fromString(kafkaOrderCreatedEvent.orderId)),
                new UserId(UUID.fromString(kafkaOrderCreatedEvent.clientId)),
                kafkaOrderCreatedEvent.orderTotal,
                Currency.PLN, //TODO change it later
                LocalDateTime.parse(kafkaOrderCreatedEvent.requestedAt, DATE_FORMATTER)
        );
    }
}
