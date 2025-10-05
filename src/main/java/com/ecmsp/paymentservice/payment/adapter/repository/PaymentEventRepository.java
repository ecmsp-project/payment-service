package com.ecmsp.paymentservice.payment.adapter.repository;

import com.ecmsp.paymentservice.payment.domain.PaymentState;
import com.ecmsp.paymentservice.payment.adapter.db.PaymentEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentEventRepository extends JpaRepository<PaymentEventEntity, Long> {

    List<PaymentEventEntity> findByStatus(PaymentState status);
    
    List<PaymentEventEntity> findByPaymentId(Long paymentId);
    
    List<PaymentEventEntity> findByStatusAndRetryCountLessThan(PaymentState status, Integer maxRetryCount);
} 