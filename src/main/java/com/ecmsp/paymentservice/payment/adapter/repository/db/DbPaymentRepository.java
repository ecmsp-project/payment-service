package com.ecmsp.paymentservice.payment.adapter.repository.db;

import com.ecmsp.paymentservice.payment.domain.*;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

class DbPaymentRepository implements PaymentRepository {

    private final PaymentEntityRepository paymentEntityRepository;
    private final PaymentEntityMapper paymentEntityMapper;


    public DbPaymentRepository(PaymentEntityRepository paymentEntityRepository) {
        this.paymentEntityRepository = paymentEntityRepository;
        this.paymentEntityMapper = new PaymentEntityMapper();

    }

    @Override
    public Optional<Payment> findById(PaymentId paymentId) {
        return paymentEntityRepository.findById(paymentId.value())
                .map(paymentEntityMapper::toPayment);
    }

    @Override
    public Optional<Payment> findByOrderId(OrderId orderId) {
        return paymentEntityRepository.findByOrderId(orderId.value())
                .map(paymentEntityMapper::toPayment);
    }

    @Override
    public Optional<Payment> findByPaymentLink(String paymentLink) {
        return paymentEntityRepository.findByPaymentLink(paymentLink)
                .map(paymentEntityMapper::toPayment);
    }

    @Override
    public List<Payment> findByUserId(UserId userId) {
        return paymentEntityRepository.findByUserId(userId.value())
                .stream()
                .map(paymentEntityMapper::toPayment)
                .toList();
    }

    @Override
    public List<Payment> findByStatus(PaymentStatus status) {
        return paymentEntityRepository.findByStatus(status)
                .stream()
                .map(paymentEntityMapper::toPayment)
                .toList();
    }

    @Override
    public List<Payment> findExpiredPayments(PaymentStatus status, LocalDateTime currentTime) {
        return paymentEntityRepository.findExpiredPayments(status, currentTime)
                .stream()
                .map(paymentEntityMapper::toPayment)
                .toList();
    }

    @Override
    public List<Payment> findActivePayments(PaymentStatus status, LocalDateTime currentTime) {
        return paymentEntityRepository.findActivePayments(status, currentTime)
                .stream()
                .map(paymentEntityMapper::toPayment)
                .toList();
    }

    @Override
    public void create(Payment payment) {
        if(paymentEntityRepository.existsById(payment.id().value())) {
            throw new RuntimeException("Payment for order " + payment.orderId().value() + " already exists");
        }
        paymentEntityRepository.save(paymentEntityMapper.toPaymentEntity(payment));
    }

    @Override
    public void update(Payment payment) {
        if(paymentEntityRepository.existsById(payment.id().value())) {
            throw new RuntimeException("Payment not found");
        }
        paymentEntityRepository.save(paymentEntityMapper.toPaymentEntity(payment));

    }
}
