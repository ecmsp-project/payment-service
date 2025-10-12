package com.ecmsp.paymentservice.payment.domain;

import java.util.List;
import java.util.Optional;

public interface PaymentFacade {

    //TODO for now correlationId would be only for kafka communication to trace through logs but should be added to gRPC too to see whole path -> or should I remove this feature

    Payment createPayment(PaymentToCreate paymentToCreate, Context context);
    Payment processPayment(String paymentLink);

    Optional<Payment> getPaymentById(PaymentId paymentId);
    Optional<Payment> getPaymentByOrderId(OrderId orderId);
    List<Payment> getPaymentsByUserId(UserId userId);






}
