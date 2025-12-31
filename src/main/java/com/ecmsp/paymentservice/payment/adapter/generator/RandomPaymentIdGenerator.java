package com.ecmsp.paymentservice.payment.adapter.generator;

import com.ecmsp.paymentservice.payment.domain.CorrelationId;
import com.ecmsp.paymentservice.payment.domain.PaymentId;
import com.ecmsp.paymentservice.payment.domain.PaymentIdGenerator;

import java.util.UUID;

class RandomPaymentIdGenerator implements PaymentIdGenerator {

    @Override
    public PaymentId generate(CorrelationId correlationId) {
        return new PaymentId(UUID.randomUUID());
    }
}
