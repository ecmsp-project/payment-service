package com.ecmsp.paymentservice.repository;

import com.ecmsp.paymentservice.entity.Payment;
import com.ecmsp.paymentservice.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByOrderId(Long orderId);
    
    Optional<Payment> findByPaymentLink(String paymentLink);
    
    List<Payment> findByUserId(Long userId);
    
    List<Payment> findByStatus(PaymentStatus status);
    
    @Query("SELECT p FROM Payment p WHERE p.status = :status AND p.expiresAt <= :currentTime")
    List<Payment> findExpiredPayments(@Param("status") PaymentStatus status, 
                                     @Param("currentTime") LocalDateTime currentTime);
    
    @Query("SELECT p FROM Payment p WHERE p.status = :status AND p.expiresAt > :currentTime")
    List<Payment> findActivePayments(@Param("status") PaymentStatus status, 
                                    @Param("currentTime") LocalDateTime currentTime);
} 