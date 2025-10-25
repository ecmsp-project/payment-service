//package com.ecmsp.paymentservice.api.rest.payment;
//
//import com.ecmsp.paymentservice.payment.domain.Payment;
//import com.ecmsp.paymentservice.payment.domain.PaymentFacade;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//import java.util.UUID;
//
//@RestController
//@RequestMapping("/api/v1/payments")
//@RequiredArgsConstructor
//@Slf4j
//public class PaymentController {
//
//    private final PaymentFacade paymentFacade;
//
//    @GetMapping("/order/{orderId}")
//    public ResponseEntity<Payment> getPaymentByOrderId(@PathVariable UUID orderId) {
//        log.info("Received request to get payment by order ID: {}", orderId);
//        return paymentFacade.getPaymentByOrderId(orderId)
//                .map(ResponseEntity::ok)
//                .orElse(ResponseEntity.notFound().build());
//    }
//
//
//    @PostMapping("/{paymentLink}")
//    public ResponseEntity<Payment> processPayment(@PathVariable String paymentLink) {
//        log.info("Received request to process payment with link: {}", paymentLink);
//        try {
//            Payment response = paymentFacade.processPayment(paymentLink);
//            return ResponseEntity.ok(response);
//        } catch (RuntimeException e) {
//            log.error("Error processing payment: {}", e.getMessage());
//            return ResponseEntity.badRequest().build();
//        }
//    }
//
//}