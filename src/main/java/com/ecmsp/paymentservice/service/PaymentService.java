package com.ecmsp.paymentservice.service;

import com.ecmsp.paymentservice.dto.CreatePaymentRequest;
import com.ecmsp.paymentservice.dto.PaymentResponse;
import com.ecmsp.paymentservice.entity.Payment;
import com.ecmsp.paymentservice.entity.PaymentEvent;
import com.ecmsp.paymentservice.entity.PaymentEventType;
import com.ecmsp.paymentservice.entity.PaymentStatus;
import com.ecmsp.paymentservice.repository.PaymentEventRepository;
import com.ecmsp.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentEventRepository paymentEventRepository;

    private static final int PAYMENT_EXPIRY_MINUTES = 10;

    @Transactional
    public PaymentResponse createPayment(CreatePaymentRequest request) {
        log.info("Creating payment for order: {}", request.getOrderId());

        Optional<Payment> existingPayment = paymentRepository.findByOrderId(request.getOrderId());
        if (existingPayment.isPresent()) {
            throw new RuntimeException("Payment for order " + request.getOrderId() + " already exists");
        }

        Payment payment = new Payment();
        payment.setOrderId(request.getOrderId());
        payment.setUserId(request.getUserId());
        payment.setAmount(request.getAmount());
        payment.setCurrency(request.getCurrency());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setPaymentLink(generatePaymentLink());
        payment.setExpiresAt(LocalDateTime.now().plusMinutes(PAYMENT_EXPIRY_MINUTES));

        Payment savedPayment = paymentRepository.save(payment);

        createPaymentEvent(savedPayment.getId(), PaymentEventType.PAYMENT_CREATED);
        
        log.info("Payment created with ID: {}", savedPayment.getId());
        return mapToPaymentResponse(savedPayment);
    }

    @Transactional(readOnly = true)
    public Optional<PaymentResponse> getPaymentById(Long id) {
        return paymentRepository.findById(id)
                .map(this::mapToPaymentResponse);
    }

    @Transactional(readOnly = true)
    public Optional<PaymentResponse> getPaymentByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId)
                .map(this::mapToPaymentResponse);
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByUserId(Long userId) {
        return paymentRepository.findByUserId(userId)
                .stream()
                .map(this::mapToPaymentResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public PaymentResponse processPayment(String paymentLink) {
        log.info("Processing payment with link: {}", paymentLink);
        
        Optional<Payment> paymentOpt = paymentRepository.findByPaymentLink(paymentLink);
        if (paymentOpt.isEmpty()) {
            throw new RuntimeException("Payment not found for link: " + paymentLink);
        }

        Payment payment = paymentOpt.get();
        
        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new RuntimeException("Payment is not in PENDING status");
        }

        if (LocalDateTime.now().isAfter(payment.getExpiresAt())) {
            payment.setStatus(PaymentStatus.EXPIRED);
            paymentRepository.save(payment);
            createPaymentEvent(payment.getId(), PaymentEventType.PAYMENT_EXPIRED);
            throw new RuntimeException("Payment has expired");
        }

        payment.setStatus(PaymentStatus.PAID);
        payment.setPaidAt(LocalDateTime.now());
        Payment savedPayment = paymentRepository.save(payment);

        createPaymentEvent(savedPayment.getId(), PaymentEventType.PAYMENT_PAID);
        
        log.info("Payment processed successfully for order: {}", savedPayment.getOrderId());
        return mapToPaymentResponse(savedPayment);
    }

    @Transactional
    public void expirePayments() {
        log.info("Starting payment expiration check");
        
        List<Payment> expiredPayments = paymentRepository.findExpiredPayments(
                PaymentStatus.PENDING, 
                LocalDateTime.now()
        );

        for (Payment payment : expiredPayments) {
            payment.setStatus(PaymentStatus.EXPIRED);
            paymentRepository.save(payment);
            createPaymentEvent(payment.getId(), PaymentEventType.PAYMENT_EXPIRED);
            log.info("Payment expired for order: {}", payment.getOrderId());
        }
        
        log.info("Expired {} payments", expiredPayments.size());
    }

    private String generatePaymentLink() {
        return "payment-" + UUID.randomUUID().toString();
    }

    private void createPaymentEvent(Long paymentId, PaymentEventType eventType) {
        PaymentEvent event = new PaymentEvent();
        event.setPaymentId(paymentId);
        event.setEventType(eventType);
        event.setEventData("Payment event: " + eventType);
        paymentEventRepository.save(event);
        log.debug("Created payment event: {} for payment: {}", eventType, paymentId);
    }

    private PaymentResponse mapToPaymentResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getOrderId(),
                payment.getUserId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getStatus(),
                payment.getPaymentLink(),
                payment.getExpiresAt(),
                payment.getPaidAt(),
                payment.getCreatedAt()
        );
    }
} 