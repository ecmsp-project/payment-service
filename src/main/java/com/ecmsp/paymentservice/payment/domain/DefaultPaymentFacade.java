package com.ecmsp.paymentservice.payment.domain;

import com.ecmsp.paymentservice.api.rest.payment.dto.CreatePaymentDto;
import com.ecmsp.paymentservice.payment.adapter.publisher.kafka.PaymentEventPublisher;
import com.ecmsp.paymentservice.payment.adapter.publisher.kafka.KafkaPaymentProcessedFailedEvent;
import com.ecmsp.paymentservice.payment.adapter.publisher.kafka.KafkaPaymentProcessedSucceededEvent;
import com.ecmsp.paymentservice.payment.adapter.repository.db.PaymentEntity;
import com.ecmsp.paymentservice.payment.adapter.repository.db.PaymentEventEntity;
import com.ecmsp.paymentservice.payment.adapter.repository.db.DbPaymentEventRepository;
import com.ecmsp.paymentservice.payment.adapter.repository.db.DbPaymentEntityRepository;
import com.ecmsp.paymentservice.payment.adapter.repository.db.PaymentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DefaultPaymentFacade implements PaymentFacade {

    private final DbPaymentEntityRepository paymentRepository;
    private final DbPaymentEventRepository paymentEventRepository;
    private final PaymentEventPublisher paymentEventPublisher;
    private final PaymentMapper paymentMapper;

    private static final int PAYMENT_EXPIRY_MINUTES = 10;

    @Override
    @Transactional
    public Payment createPayment(CreatePaymentDto request) {
        PaymentToCreate paymentToCreate = new PaymentToCreate(
                new OrderId(request.orderId()),
                new UserId(request.userId()),
                request.orderTotal(),
                Currency.PLN,
                LocalDateTime.now()
        );
        return createPaymentInternal(paymentToCreate);
    }

    @Override
    @Transactional
    public Payment processPaymentRequest(PaymentToCreate paymentToCreate) {
        log.info("Processing payment request for order: {}", paymentToCreate.orderId().value());

        try {
            Payment payment = createPaymentInternal(paymentToCreate);

            paymentEventPublisher.publishPaymentProcessedSuccess(
                    new KafkaPaymentProcessedSucceededEvent(
                            paymentToCreate.orderId().value().toString(),
                            payment.id().toString(),
                            DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now())
                    )
            );

            log.info("Payment request processed successfully for order: {}", paymentToCreate.orderId().value());
            return payment;

        } catch (Exception e) {
            log.error("Failed to process payment request for order: {}", paymentToCreate.orderId().value(), e);

            paymentEventPublisher.publishPaymentProcessedFailure(
                    new KafkaPaymentProcessedFailedEvent(
                            paymentToCreate.orderId().value().toString(),
                            null,
                            DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now())
                    )
            );

            throw e;
        }
    }

    private Payment createPaymentInternal(PaymentToCreate paymentToCreate) {
        log.info("Creating payment for order: {}", paymentToCreate.orderId().value());

        Optional<PaymentEntity> existingPayment = paymentRepository.findByOrderId(paymentToCreate.orderId().value());
        if (existingPayment.isPresent()) {
            throw new RuntimeException("Payment for order " + paymentToCreate.orderId().value() + " already exists");
        }

        PaymentEntity paymentEntity = new PaymentEntity();
        paymentEntity.setOrderId(paymentToCreate.orderId().value());
        paymentEntity.setUserId(paymentToCreate.userId().value());
        paymentEntity.setOrderTotal(paymentToCreate.orderTotal());
        paymentEntity.setCurrency(paymentToCreate.currency());
        paymentEntity.setStatus(PaymentStatus.PENDING);
        paymentEntity.setPaymentLink(generatePaymentLink());
        paymentEntity.setExpiresAt(LocalDateTime.now().plusMinutes(PAYMENT_EXPIRY_MINUTES));

        PaymentEntity savedPaymentEntity = paymentRepository.save(paymentEntity);

        createPaymentEvent(savedPaymentEntity.getId(), PaymentStatus.CREATED);

        log.info("Payment created with ID: {}", savedPaymentEntity.getId());
        return paymentMapper.mapToPaymentResponse(savedPaymentEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Payment> getPaymentById(UUID id) {
        return paymentRepository.findById(id)
                .map(paymentMapper::mapToPaymentResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Payment> getPaymentByOrderId(UUID orderId) {
        return paymentRepository.findByOrderId(orderId)
                .map(paymentMapper::mapToPaymentResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Payment> getPaymentsByUserId(UUID userId) {
        return paymentRepository.findByUserId(userId)
                .stream()
                .map(paymentMapper::mapToPaymentResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Payment processPayment(String paymentLink) {
        log.info("Processing payment with link: {}", paymentLink);
        
        Optional<PaymentEntity> paymentOpt = paymentRepository.findByPaymentLink(paymentLink);
        if (paymentOpt.isEmpty()) {
            throw new RuntimeException("Payment not found for link: " + paymentLink);
        }

        PaymentEntity paymentEntity = paymentOpt.get();
        
        if (paymentEntity.getStatus() != PaymentStatus.PENDING) {
            throw new RuntimeException("Payment is not in PENDING status");
        }

        if (LocalDateTime.now().isAfter(paymentEntity.getExpiresAt())) {
            paymentEntity.setStatus(PaymentStatus.EXPIRED);
            paymentRepository.save(paymentEntity);
            createPaymentEvent(paymentEntity.getId(), PaymentStatus.EXPIRED);
            throw new RuntimeException("Payment has expired");
        }

        paymentEntity.setStatus(PaymentStatus.PAID);
        paymentEntity.setPaidAt(LocalDateTime.now());
        PaymentEntity savedPaymentEntity = paymentRepository.save(paymentEntity);

        createPaymentEvent(savedPaymentEntity.getId(), PaymentStatus.PAID);

        log.info("Payment processed successfully for order: {}", savedPaymentEntity.getOrderId());
        return paymentMapper.mapToPaymentResponse(savedPaymentEntity);
    }

    @Override
    @Transactional
    public void expirePayments() {
        log.info("Starting payment expiration check");
        
        List<PaymentEntity> expiredPaymentEntities = paymentRepository.findExpiredPayments(
                PaymentStatus.PENDING,
                LocalDateTime.now()
        );

        for (PaymentEntity paymentEntity : expiredPaymentEntities) {
            paymentEntity.setStatus(PaymentStatus.EXPIRED);
            paymentRepository.save(paymentEntity);
            createPaymentEvent(paymentEntity.getId(), PaymentStatus.EXPIRED);
            log.info("Payment expired for order: {}", paymentEntity.getOrderId());
        }
        
        log.info("Expired {} payments", expiredPaymentEntities.size());
    }

    private String generatePaymentLink() {
        return "payment-" + UUID.randomUUID();
    }

    private void createPaymentEvent(UUID paymentId, PaymentStatus eventType) {
        PaymentEventEntity event = new PaymentEventEntity();
        event.setPaymentId(paymentId);
        event.setEventType(eventType);
        event.setEventData("Payment event: " + eventType);
        paymentEventRepository.save(event);
        log.debug("Created payment event: {} for payment: {}", eventType, paymentId);
    }
} 