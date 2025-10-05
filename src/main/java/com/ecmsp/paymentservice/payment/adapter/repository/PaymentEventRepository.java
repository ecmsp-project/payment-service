package com.ecmsp.paymentservice.payment.adapter.repository;

import com.ecmsp.paymentservice.payment.domain.EventStatus;
import com.ecmsp.paymentservice.payment.domain.PaymentEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentEventRepository extends JpaRepository<PaymentEvent, Long> {

    List<PaymentEvent> findByStatus(EventStatus status);
    
    List<PaymentEvent> findByPaymentId(Long paymentId);
    
    List<PaymentEvent> findByStatusAndRetryCountLessThan(EventStatus status, Integer maxRetryCount);
} 