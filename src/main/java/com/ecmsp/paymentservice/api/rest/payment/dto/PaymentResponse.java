package com.ecmsp.paymentservice.api.rest.payment.dto;

import com.ecmsp.paymentservice.payment.domain.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private UUID id;
    private UUID orderId;
    private UUID userId;
    private BigDecimal amount;
    private String currency;
    private PaymentStatus status;
    private String paymentLink;
    private LocalDateTime expiresAt;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
} 