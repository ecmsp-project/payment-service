package com.ecmsp.paymentservice.payment.domain;

import com.ecmsp.paymentservice.api.rest.payment.dto.CreatePaymentDto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentFacade {

    Payment createPayment(PaymentToCreate paymentToCreate);

    Payment processPaymentRequest(PaymentToCreate paymentToCreate);

    Optional<Payment> getPaymentById(UUID id);

    Optional<Payment> getPaymentByOrderId(UUID orderId);

    List<Payment> getPaymentsByUserId(UUID userId);

    Payment processPayment(String paymentLink);

    void expirePayments();
}
