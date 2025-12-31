package com.ecmsp.paymentservice.payment.domain;

import java.time.LocalDateTime;

public record CreatePaymentLink(PaymentId paymentId, String paymentLink, LocalDateTime expiresAt) {
}
