package com.ecmsp.paymentservice.payment.adapter.generator;

import com.ecmsp.paymentservice.payment.domain.CorrelationId;
import com.ecmsp.paymentservice.payment.domain.PaymentId;
import com.ecmsp.paymentservice.payment.domain.PaymentIdGenerator;

class FixedPaymentIdGenerator implements PaymentIdGenerator {
    @Override
    public PaymentId generate(CorrelationId correlationId) {
        return new PaymentId(correlationId.value());
    }
}
