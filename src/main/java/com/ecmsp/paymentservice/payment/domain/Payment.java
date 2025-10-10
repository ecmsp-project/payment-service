package com.ecmsp.paymentservice.payment.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Payment(
    PaymentId id,
    OrderId orderId,
    UserId userId,
    BigDecimal orderTotal,
    Currency currency,
    PaymentStatus status,
    String paymentLink,
    LocalDateTime expiresAt,
    LocalDateTime paidAt,
    LocalDateTime createdAt
) {} 