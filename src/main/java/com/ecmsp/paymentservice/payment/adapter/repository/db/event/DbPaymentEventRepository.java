package com.ecmsp.paymentservice.payment.adapter.repository.db.event;

import com.ecmsp.paymentservice.payment.domain.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;


//TODO CHANGE NAME - how it should be used - it's for outbox pattern table
@Repository
public interface DbPaymentEventRepository extends JpaRepository<PaymentEventEntity, UUID> {

    List<PaymentEventEntity> findByStatus(PaymentStatus status);
    
    List<PaymentEventEntity> findByPaymentId(UUID paymentId);
    
    List<PaymentEventEntity> findByStatusAndRetryCountLessThan(PaymentStatus status, Integer maxRetryCount);
} 