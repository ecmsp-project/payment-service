package com.ecmsp.paymentservice.payment.domain;

import java.time.LocalDateTime;

public sealed interface PaymentEvent {

      record PaymentProcessedSucceeded(
            OrderId orderId,
            PaymentId paymentId,
            LocalDateTime processedAt
    ) implements PaymentEvent {}

    record PaymentProcessedFailed(
            OrderId orderId,
            PaymentId paymentId,
            LocalDateTime processedAt
    ) implements PaymentEvent {}
}
