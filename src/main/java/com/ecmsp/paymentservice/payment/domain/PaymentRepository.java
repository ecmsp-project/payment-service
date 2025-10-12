package com.ecmsp.paymentservice.payment.domain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository {

    void create(Payment payment);

    void update(Payment payment);

    Optional<Payment> findById(PaymentId paymentId);

    Optional<Payment> findByOrderId(OrderId orderId);

    Optional<Payment> findByPaymentLink(String paymentLink);

    List<Payment> findByUserId(UserId userId);


    List<Payment> findExpiredPayments(PaymentStatus status, LocalDateTime currentTime);

    List<Payment> findByStatus(PaymentStatus status);
    List<Payment> findActivePayments(PaymentStatus status, LocalDateTime currentTime);



}
