package com.ecmsp.paymentservice.api.rest.payment;

import com.ecmsp.paymentservice.api.rest.payment.dto.CreatePaymentDto;
import com.ecmsp.paymentservice.payment.domain.Payment;
import com.ecmsp.paymentservice.payment.domain.PaymentFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentFacade paymentFacade;

//    @PostMapping
//    public ResponseEntity<Payment> createPayment(@Valid @RequestBody CreatePaymentDto request) {
//        log.info("Received request to create payment for order: {}", request.orderId());
//        Payment response = paymentFacade.createPayment(request);
//        return ResponseEntity.status(HttpStatus.CREATED).body(response);
//    }

    @GetMapping("/{id}")
    public ResponseEntity<Payment> getPaymentById(@PathVariable UUID id) {
        log.info("Received request to get payment by ID: {}", id);
        return paymentFacade.getPaymentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<Payment> getPaymentByOrderId(@PathVariable UUID orderId) {
        log.info("Received request to get payment by order ID: {}", orderId);
        return paymentFacade.getPaymentByOrderId(orderId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Payment>> getPaymentsByUserId(@PathVariable UUID userId) {
        log.info("Received request to get payments by user ID: {}", userId);
        List<Payment> payments = paymentFacade.getPaymentsByUserId(userId);
        return ResponseEntity.ok(payments);
    }

    @PostMapping("/process/{paymentLink}")
    public ResponseEntity<Payment> processPayment(@PathVariable String paymentLink) {
        log.info("Received request to process payment with link: {}", paymentLink);
        try {
            Payment response = paymentFacade.processPayment(paymentLink);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Error processing payment: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/expire")
    public ResponseEntity<Void> expirePayments() {
        log.info("Received request to expire payments");
        paymentFacade.expirePayments();
        return ResponseEntity.ok().build();
    }
} 