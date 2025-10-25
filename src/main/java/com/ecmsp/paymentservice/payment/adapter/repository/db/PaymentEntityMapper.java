package com.ecmsp.paymentservice.payment.adapter.repository.db;

import com.ecmsp.paymentservice.payment.domain.*;

import java.time.Clock;
import java.time.LocalDateTime;

class PaymentEntityMapper {



    public Payment toPayment(PaymentEntity paymentEntity) {
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
                paymentEntity.getCreatedAt(),
                paymentEntity.getUpdatedAt()
        );
    }

    public PaymentEntity toPaymentEntity(Payment payment) {
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
                payment.updatedAt()
        );
    }
}
