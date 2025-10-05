package com.ecmsp.paymentservice.payment.adapter.service;

import com.ecmsp.paymentservice.api.rest.payment.dto.CreatePaymentRequest;
import com.ecmsp.paymentservice.api.rest.payment.dto.PaymentResponse;
import com.ecmsp.paymentservice.payment.adapter.db.PaymentEntity;
import com.ecmsp.paymentservice.payment.adapter.db.PaymentEventEntity;
import com.ecmsp.paymentservice.payment.domain.PaymentState;
import com.ecmsp.paymentservice.payment.domain.PaymentToCreate;
import com.ecmsp.paymentservice.payment.adapter.repository.PaymentEventRepository;
import com.ecmsp.paymentservice.payment.adapter.repository.PaymentRepository;
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

        Optional<PaymentEntity> existingPayment = paymentRepository.findByOrderId(request.getOrderId());
        if (existingPayment.isPresent()) {
            throw new RuntimeException("Payment for order " + request.getOrderId() + " already exists");
        }

        PaymentEntity paymentEntity = new PaymentEntity();
        paymentEntity.setOrderId(request.getOrderId());
        paymentEntity.setUserId(request.getUserId());
        paymentEntity.setAmount(request.getAmount());
        paymentEntity.setCurrency(request.getCurrency());
        paymentEntity.setStatus(PaymentState.PENDING);
        paymentEntity.setPaymentLink(generatePaymentLink());
        paymentEntity.setExpiresAt(LocalDateTime.now().plusMinutes(PAYMENT_EXPIRY_MINUTES));

        PaymentEntity savedPaymentEntity = paymentRepository.save(paymentEntity);

        createPaymentEvent(savedPaymentEntity.getId(), PaymentState.CREATED);
        
        log.info("Payment created with ID: {}", savedPaymentEntity.getId());
        return mapToPaymentResponse(savedPaymentEntity);
    }

    @Transactional
    public PaymentResponse createPaymentFromDomain(PaymentToCreate paymentToCreate) {
        log.info("Creating payment for order: {}", paymentToCreate.orderId().value());

        Optional<PaymentEntity> existingPayment = paymentRepository.findByOrderId(paymentToCreate.orderId().value());
        if (existingPayment.isPresent()) {
            throw new RuntimeException("Payment for order " + paymentToCreate.orderId().value() + " already exists");
        }

        PaymentEntity paymentEntity = new PaymentEntity();
        paymentEntity.setOrderId(paymentToCreate.orderId().value());
        paymentEntity.setUserId(paymentToCreate.clientId().value());
        paymentEntity.setAmount(paymentToCreate.amount());
        paymentEntity.setCurrency("PLN");
        paymentEntity.setStatus(PaymentState.PENDING);
        paymentEntity.setPaymentLink(generatePaymentLink());
        paymentEntity.setExpiresAt(LocalDateTime.now().plusMinutes(PAYMENT_EXPIRY_MINUTES));

        PaymentEntity savedPaymentEntity = paymentRepository.save(paymentEntity);

        createPaymentEvent(savedPaymentEntity.getId(), PaymentState.CREATED);
        
        log.info("Payment created with ID: {}", savedPaymentEntity.getId());
        return mapToPaymentResponse(savedPaymentEntity);
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
        
        Optional<PaymentEntity> paymentOpt = paymentRepository.findByPaymentLink(paymentLink);
        if (paymentOpt.isEmpty()) {
            throw new RuntimeException("Payment not found for link: " + paymentLink);
        }

        PaymentEntity paymentEntity = paymentOpt.get();
        
        if (paymentEntity.getStatus() != PaymentState.PENDING) {
            throw new RuntimeException("Payment is not in PENDING status");
        }

        if (LocalDateTime.now().isAfter(paymentEntity.getExpiresAt())) {
            paymentEntity.setStatus(PaymentState.EXPIRED);
            paymentRepository.save(paymentEntity);
            createPaymentEvent(paymentEntity.getId(), PaymentState.EXPIRED);
            throw new RuntimeException("Payment has expired");
        }

        paymentEntity.setStatus(PaymentState.PAID);
        paymentEntity.setPaidAt(LocalDateTime.now());
        PaymentEntity savedPaymentEntity = paymentRepository.save(paymentEntity);

        createPaymentEvent(savedPaymentEntity.getId(), PaymentState.PAID);
        
        log.info("Payment processed successfully for order: {}", savedPaymentEntity.getOrderId());
        return mapToPaymentResponse(savedPaymentEntity);
    }

    @Transactional
    public void expirePayments() {
        log.info("Starting payment expiration check");
        
        List<PaymentEntity> expiredPaymentEntities = paymentRepository.findExpiredPayments(
                PaymentState.PENDING, 
                LocalDateTime.now()
        );

        for (PaymentEntity paymentEntity : expiredPaymentEntities) {
            paymentEntity.setStatus(PaymentState.EXPIRED);
            paymentRepository.save(paymentEntity);
            createPaymentEvent(paymentEntity.getId(), PaymentState.EXPIRED);
            log.info("Payment expired for order: {}", paymentEntity.getOrderId());
        }
        
        log.info("Expired {} payments", expiredPaymentEntities.size());
    }

    private String generatePaymentLink() {
        return "payment-" + UUID.randomUUID().toString();
    }

    private void createPaymentEvent(Long paymentId, PaymentState eventType) {
        PaymentEventEntity event = new PaymentEventEntity();
        event.setPaymentId(paymentId);
        event.setEventType(eventType);
        event.setEventData("Payment event: " + eventType);
        paymentEventRepository.save(event);
        log.debug("Created payment event: {} for payment: {}", eventType, paymentId);
    }

    private PaymentResponse mapToPaymentResponse(PaymentEntity paymentEntity) {
        return new PaymentResponse(
                paymentEntity.getId(),
                paymentEntity.getOrderId(),
                paymentEntity.getUserId(),
                paymentEntity.getAmount(),
                paymentEntity.getCurrency(),
                paymentEntity.getStatus(),
                paymentEntity.getPaymentLink(),
                paymentEntity.getExpiresAt(),
                paymentEntity.getPaidAt(),
                paymentEntity.getCreatedAt()
        );
    }
} 