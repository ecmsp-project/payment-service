package com.ecmsp.paymentservice.api.rest.payment.dto;

import java.math.BigDecimal;
import java.util.UUID;


public record CreatePaymentDto(
        UUID orderId,
        UUID userId,
        BigDecimal orderTotal,
        String currency
){};
