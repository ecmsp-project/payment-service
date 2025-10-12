package com.ecmsp.paymentservice.payment.domain;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
public class DefaultPaymentFacade implements PaymentFacade {


    private final PaymentRepository paymentRepository;
    private final PaymentExpirationService paymentExpirationService;
    private final PaymentEventPublisher paymentEventPublisher;

    private final PaymentIdGenerator paymentIdGenerator;
    private final Clock clock;

    private static final int PAYMENT_EXPIRY_MINUTES = 10;

    public DefaultPaymentFacade(PaymentRepository paymentRepository, PaymentExpirationService paymentExpirationService, PaymentEventPublisher paymentEventPublisher, PaymentIdGenerator paymentIdGenerator, Clock clock) {
        this.paymentRepository = paymentRepository;
        this.paymentExpirationService = paymentExpirationService;
        this.paymentEventPublisher = paymentEventPublisher;
        this.paymentIdGenerator = paymentIdGenerator;
        this.clock = clock;
    }


    @Override
    public Payment createPayment(PaymentToCreate paymentToCreate, Context context) {
        log.info("Creating payment for order: {}", paymentToCreate.orderId().value());

        PaymentId paymentId = paymentIdGenerator.generate(context.correlationId());
        String paymentLink = "payment-" + paymentId.value();

        Payment payment = new Payment(
                paymentId,
                paymentToCreate.orderId(),
                paymentToCreate.userId(),
                paymentToCreate.orderTotal(),
                paymentToCreate.currency(),
                PaymentStatus.PENDING,
                paymentLink,
                LocalDateTime.now(clock).plusMinutes(PAYMENT_EXPIRY_MINUTES),
                null,
                LocalDateTime.now(clock),
                null
        );

        paymentRepository.create(payment);

        return payment;
    }

    @Transactional
    @Override
    public Payment processPayment(String paymentLink) {

        Payment payment = paymentRepository.findByPaymentLink(paymentLink).orElseThrow(() -> new RuntimeException("Payment not found for link: " + paymentLink));

        if (payment.status() != PaymentStatus.PENDING) {
            throw new RuntimeException("Payment is not in PENDING status");
        }

        if(LocalDateTime.now(clock).isAfter(payment.expiresAt())) {
            paymentExpirationService.expire(payment);
            throw new RuntimeException("Payment has expired");
        }

        Payment paidPayment = new Payment(
                payment.id(),
                payment.orderId(),
                payment.userId(),
                payment.orderTotal(),
                payment.currency(),
                PaymentStatus.PAID,
                payment.paymentLink(),
                payment.expiresAt(),
                LocalDateTime.now(clock),
                payment.createdAt(),
                LocalDateTime.now(clock)
        );

        //TODO save to outbox table
        paymentRepository.update(paidPayment);
        PaymentEvent.PaymentProcessedSucceeded paymentProcessedSucceededEvent = new PaymentEvent.PaymentProcessedSucceeded(
                payment.orderId(),
                payment.id(),
                LocalDateTime.now(clock)
        );

        paymentEventPublisher.publish(paymentProcessedSucceededEvent);
        return paidPayment;
    }


    @Override
    public Optional<Payment> getPaymentById(PaymentId paymentId) {
        return paymentRepository.findById(paymentId);
    }

    @Override
    public Optional<Payment> getPaymentByOrderId(OrderId orderId){
        return paymentRepository.findByOrderId(orderId);
    }

    @Override
    public List<Payment> getPaymentsByUserId(UserId userId) {
        return paymentRepository.findByUserId(userId);
    }

}
