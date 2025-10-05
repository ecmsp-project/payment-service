package com.ecmsp.paymentservice.api.rest.payment;

import com.ecmsp.paymentservice.api.rest.payment.dto.CreatePaymentRequest;
import com.ecmsp.paymentservice.api.rest.payment.dto.PaymentResponse;
import com.ecmsp.paymentservice.payment.adapter.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody CreatePaymentRequest request) {
        log.info("Received request to create payment for order: {}", request.getOrderId());
        PaymentResponse response = paymentService.createPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPaymentById(@PathVariable Long id) {
        log.info("Received request to get payment by ID: {}", id);
        return paymentService.getPaymentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentResponse> getPaymentByOrderId(@PathVariable Long orderId) {
        log.info("Received request to get payment by order ID: {}", orderId);
        return paymentService.getPaymentByOrderId(orderId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByUserId(@PathVariable Long userId) {
        log.info("Received request to get payments by user ID: {}", userId);
        List<PaymentResponse> payments = paymentService.getPaymentsByUserId(userId);
        return ResponseEntity.ok(payments);
    }

    @PostMapping("/process/{paymentLink}")
    public ResponseEntity<PaymentResponse> processPayment(@PathVariable String paymentLink) {
        log.info("Received request to process payment with link: {}", paymentLink);
        try {
            PaymentResponse response = paymentService.processPayment(paymentLink);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Error processing payment: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/expire")
    public ResponseEntity<Void> expirePayments() {
        log.info("Received request to expire payments");
        paymentService.expirePayments();
        return ResponseEntity.ok().build();
    }
} 