package com.ecmsp.paymentservice.payment.domain;

public interface PaymentIdGenerator {
    PaymentId generate(CorrelationId correlationId);
}
