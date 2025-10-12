package com.ecmsp.paymentservice.payment.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

class TestPaymentRepository implements PaymentRepository {

    private final Map<PaymentId, Payment> payments;

    public TestPaymentRepository(List<Payment> payments) {
        this.payments = new ConcurrentHashMap<>();
        payments.forEach(payment -> this.payments.put(payment.id(), payment));
    }

    @Override
    public void create(Payment payment) {
        if (payments.containsKey(payment.id())) {
            throw new RuntimeException("Payment with id " + payment.id() + " already exists");
        }
        payments.put(payment.id(), payment);
    }

    @Override
    public void update(Payment payment) {
        if (!payments.containsKey(payment.id())) {
            throw new RuntimeException("Payment with id " + payment.id() + " not found");
        }
        payments.put(payment.id(), payment);
    }

    @Override
    public Optional<Payment> findById(PaymentId paymentId) {
        return Optional.ofNullable(payments.get(paymentId));
    }

    @Override
    public Optional<Payment> findByOrderId(OrderId orderId) {
        return payments.values().stream()
                .filter(payment -> payment.orderId().equals(orderId))
                .findFirst();
    }

    @Override
    public Optional<Payment> findByPaymentLink(String paymentLink) {
        return payments.values().stream()
                .filter(payment -> payment.paymentLink().equals(paymentLink))
                .findFirst();
    }

    @Override
    public List<Payment> findByUserId(UserId userId) {
        return payments.values().stream()
                .filter(payment -> payment.userId().equals(userId))
                .toList();
    }

    @Override
    public List<Payment> findExpiredPayments(PaymentStatus status, LocalDateTime currentTime) {
        return payments.values().stream()
                .filter(payment -> payment.status() == status)
                .filter(payment -> payment.expiresAt().isBefore(currentTime))
                .toList();
    }

    @Override
    public List<Payment> findByStatus(PaymentStatus status) {
        return payments.values().stream()
                .filter(payment -> payment.status() == status)
                .toList();
    }

    @Override
    public List<Payment> findActivePayments(PaymentStatus status, LocalDateTime currentTime) {
        return payments.values().stream()
                .filter(payment -> payment.status() == status)
                .filter(payment -> payment.expiresAt().isAfter(currentTime))
                .toList();
    }
}
