package com.ecmsp.paymentservice.payment.adapter.repository.db;

import com.ecmsp.paymentservice.payment.domain.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DbPaymentEntityRepository extends JpaRepository<PaymentEntity, UUID> {

    Optional<PaymentEntity> findByOrderId(UUID orderId);

    Optional<PaymentEntity> findByPaymentLink(String paymentLink);

    List<PaymentEntity> findByUserId(UUID userId);
    
    List<PaymentEntity> findByStatus(PaymentStatus status);
    
    @Query("SELECT p FROM PaymentEntity p WHERE p.status = :status AND p.expiresAt <= :currentTime")
    List<PaymentEntity> findExpiredPayments(@Param("status") PaymentStatus status,
                                            @Param("currentTime") LocalDateTime currentTime);
    
    @Query("SELECT p FROM PaymentEntity p WHERE p.status = :status AND p.expiresAt > :currentTime")
    List<PaymentEntity> findActivePayments(@Param("status") PaymentStatus status,
                                           @Param("currentTime") LocalDateTime currentTime);
} 