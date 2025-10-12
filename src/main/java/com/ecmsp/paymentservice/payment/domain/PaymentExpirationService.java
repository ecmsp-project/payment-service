package com.ecmsp.paymentservice.payment.domain;

import lombok.extern.slf4j.Slf4j;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
public class PaymentExpirationService {

    private final PaymentRepository paymentRepository;
    private final PaymentEventPublisher paymentEventPublisher;
    private final Clock clock;

    public PaymentExpirationService(PaymentRepository paymentRepository, PaymentEventPublisher paymentEventPublisher, Clock clock) {
        this.paymentRepository = paymentRepository;
        this.paymentEventPublisher = paymentEventPublisher;
        this.clock = clock;
    }


    public void expirePayments(){
        log.info("Starting payment expiration check");

        List<Payment> expiredPayments = paymentRepository.findExpiredPayments(
                PaymentStatus.PENDING,
                LocalDateTime.now()
        );

        for (Payment payment : expiredPayments) {
            expire(payment);
            log.info("Payment expired for order: {}", payment.orderId().value());
        }

        log.info("Expired {} payments", expiredPayments.size());
    }


    void expire(Payment payment){
        Payment expiredPayment = new Payment(
                payment.id(),
                payment.orderId(),
                payment.userId(),
                payment.orderTotal(),
                payment.currency(),
                PaymentStatus.EXPIRED,
                payment.paymentLink(),
                payment.expiresAt(),
                payment.paidAt(),
                payment.createdAt(),
                LocalDateTime.now(clock)
        );


        //TODO should be save to outbox table there should be orderId ? what else ... artur put too many fields

        paymentRepository.update(expiredPayment);

        PaymentEvent.PaymentProcessedFailed paymentProcessedFailed = new PaymentEvent.PaymentProcessedFailed(
                payment.orderId(),
                payment.id(),
                LocalDateTime.now(clock)
        );

        paymentEventPublisher.publish(paymentProcessedFailed);
    }

}
