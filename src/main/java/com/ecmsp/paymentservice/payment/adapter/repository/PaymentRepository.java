package com.ecmsp.paymentservice.payment.adapter.repository;

import com.ecmsp.paymentservice.payment.adapter.db.PaymentEntity;
import com.ecmsp.paymentservice.payment.domain.PaymentState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {

    Optional<PaymentEntity> findByOrderId(Long orderId);
    
    Optional<PaymentEntity> findByPaymentLink(String paymentLink);
    
    List<PaymentEntity> findByUserId(Long userId);
    
    List<PaymentEntity> findByStatus(PaymentState status);
    
    @Query("SELECT p FROM PaymentEntity p WHERE p.status = :status AND p.expiresAt <= :currentTime")
    List<PaymentEntity> findExpiredPayments(@Param("status") PaymentState status,
                                            @Param("currentTime") LocalDateTime currentTime);
    
    @Query("SELECT p FROM PaymentEntity p WHERE p.status = :status AND p.expiresAt > :currentTime")
    List<PaymentEntity> findActivePayments(@Param("status") PaymentState status,
                                           @Param("currentTime") LocalDateTime currentTime);
} 