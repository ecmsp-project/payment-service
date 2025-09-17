package com.ecmsp.paymentservice.dto;

import com.ecmsp.paymentservice.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    private Long id;
    private Long orderId;
    private Long userId;
    private BigDecimal amount;
    private String currency;
    private PaymentStatus status;
    private String paymentLink;
    private LocalDateTime expiresAt;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
} 