package com.ecmsp.paymentservice.payment.adapter.repository.db;

import com.ecmsp.paymentservice.payment.domain.*;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;

@Component
public class PaymentEntityMapper {

    public Payment mapToPayment(PaymentEntity paymentEntity) {
        return new Payment(
                new PaymentId(paymentEntity.getId()),
                new OrderId(paymentEntity.getOrderId()),
                new UserId(paymentEntity.getUserId()),
                paymentEntity.getOrderTotal(),
                Currency.valueOf(paymentEntity.getCurrency().getCode()),
                paymentEntity.getStatus(),
                paymentEntity.getPaymentLink(),
                paymentEntity.getExpiresAt(),
                paymentEntity.getPaidAt(),
                paymentEntity.getCreatedAt()
        );
    }


    public PaymentEntity mapToPaymentEntity(Payment payment, Clock clock) {
        return new PaymentEntity(
                payment.id().value(),
                payment.orderId().value(),
                payment.userId().value(),
                payment.orderTotal(),
                payment.currency(),
                payment.status(),
                payment.paymentLink(),
                payment.expiresAt(),
                payment.paidAt(),
                payment.createdAt(),
                LocalDateTime.now(clock),
                null  // version - JPA will set to 0 on first persist
        );
    }


}
