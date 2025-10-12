package com.ecmsp.paymentservice.api.kafka;

import com.ecmsp.paymentservice.payment.domain.Currency;
import com.ecmsp.paymentservice.payment.domain.OrderId;
import com.ecmsp.paymentservice.payment.domain.PaymentToCreate;
import com.ecmsp.paymentservice.payment.domain.UserId;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

record KafkaOrderCreatedEvent(
        String orderId,
        String clientId,
        BigDecimal orderTotal,
        String requestedAt
) {
    public static PaymentToCreate toPayment(KafkaOrderCreatedEvent kafkaOrderCreatedEvent) {
        return new PaymentToCreate(
                new OrderId(UUID.fromString(kafkaOrderCreatedEvent.orderId)),
                new UserId(UUID.fromString(kafkaOrderCreatedEvent.clientId)),
                kafkaOrderCreatedEvent.orderTotal,
                Currency.PLN, //TODO change it later
                LocalDateTime.parse(kafkaOrderCreatedEvent.requestedAt)
        );
    }
}
