package com.ecmsp.paymentservice.payment.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class PaymentExpirationServiceTest {

    private static final PaymentId PAYMENT_1_ID = new PaymentId(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));
    private static final PaymentId PAYMENT_2_ID = new PaymentId(UUID.fromString("9e349a18-1203-4224-829c-dc15700c68a5"));
    private static final PaymentId PAYMENT_3_ID = new PaymentId(UUID.fromString("c5e75ab0-a110-4a2a-b6f4-c4573e6f548e"));
    private static final OrderId ORDER_1_ID = new OrderId(UUID.fromString("b5d1eec8-c3ea-4b55-8cec-900b5c018381"));
    private static final OrderId ORDER_2_ID = new OrderId(UUID.fromString("b259c7f1-483b-4700-accc-1554542eb8f5"));
    private static final OrderId ORDER_3_ID = new OrderId(UUID.fromString("66d155e8-2d57-44fa-9adc-580e1e4f9cc9"));
    private static final UserId USER_1_ID = new UserId(UUID.fromString("473c1579-12b1-49b0-b90e-253782c874a5"));

    private static final LocalDateTime DATE_2025_07_10_15_00_00 = LocalDateTime.of(2025, 7, 10, 15, 0, 0);
    private static final LocalDateTime DATE_2025_07_10_15_10_00 = LocalDateTime.of(2025, 7, 10, 15, 10, 0);
    private static final LocalDateTime DATE_2025_07_10_15_20_00 = LocalDateTime.of(2025, 7, 10, 15, 20, 0);

    @Test
    void should_expire_multiple_pending_payments_past_expiry_time() {
        // given:
        Payment expiredPayment1 = new Payment(
                PAYMENT_1_ID,
                ORDER_1_ID,
                USER_1_ID,
                BigDecimal.valueOf(100.00),
                Currency.USD,
                PaymentStatus.PENDING,
                "payment-" + PAYMENT_1_ID.value(),
                DATE_2025_07_10_15_00_00, // Expired
                null,
                DATE_2025_07_10_15_00_00,
                null
        );

        Payment expiredPayment2 = new Payment(
                PAYMENT_2_ID,
                ORDER_2_ID,
                USER_1_ID,
                BigDecimal.valueOf(200.00),
                Currency.USD,
                PaymentStatus.PENDING,
                "payment-" + PAYMENT_2_ID.value(),
                DATE_2025_07_10_15_10_00, // Expired
                null,
                DATE_2025_07_10_15_00_00,
                null
        );

        TestPaymentRepository paymentRepository = new TestPaymentRepository(List.of(expiredPayment1, expiredPayment2));
        TestPaymentEventPublisher eventPublisher = new TestPaymentEventPublisher();
        // Clock is at 15:20, both payments expired
        PaymentExpirationService expirationService = new PaymentExpirationService(
                paymentRepository,
                eventPublisher,
                Clock.fixed(DATE_2025_07_10_15_20_00.toInstant(ZoneOffset.UTC), ZoneOffset.UTC)
        );

        // when:
        expirationService.expirePayments();

        // then:
        Payment updatedPayment1 = paymentRepository.findById(PAYMENT_1_ID).get();
        Payment updatedPayment2 = paymentRepository.findById(PAYMENT_2_ID).get();

        assertThat(updatedPayment1.status()).isEqualTo(PaymentStatus.EXPIRED);
        assertThat(updatedPayment1.updatedAt()).isEqualTo(DATE_2025_07_10_15_20_00);

        assertThat(updatedPayment2.status()).isEqualTo(PaymentStatus.EXPIRED);
        assertThat(updatedPayment2.updatedAt()).isEqualTo(DATE_2025_07_10_15_20_00);

        // Verify 2 failed events were published
        assertThat(eventPublisher.getEventCount()).isEqualTo(2);
        assertThat(eventPublisher.hasEvent(PaymentEvent.PaymentProcessedFailed.class)).isTrue();
    }

    @Test
    void should_not_expire_payments_that_have_not_expired_yet() {
        // given:
        Payment futurePayment = new Payment(
                PAYMENT_1_ID,
                ORDER_1_ID,
                USER_1_ID,
                BigDecimal.valueOf(100.00),
                Currency.USD,
                PaymentStatus.PENDING,
                "payment-" + PAYMENT_1_ID.value(),
                DATE_2025_07_10_15_20_00, // Not expired yet
                null,
                DATE_2025_07_10_15_00_00,
                null
        );

        TestPaymentRepository paymentRepository = new TestPaymentRepository(List.of(futurePayment));
        TestPaymentEventPublisher eventPublisher = new TestPaymentEventPublisher();
        // Clock is at 15:10, payment expires at 15:20
        PaymentExpirationService expirationService = new PaymentExpirationService(
                paymentRepository,
                eventPublisher,
                Clock.fixed(DATE_2025_07_10_15_10_00.toInstant(ZoneOffset.UTC), ZoneOffset.UTC)
        );

        // when:
        expirationService.expirePayments();

        // then:
        Payment payment = paymentRepository.findById(PAYMENT_1_ID).get();
        assertThat(payment.status()).isEqualTo(PaymentStatus.PENDING); // Still pending
        assertThat(eventPublisher.getEventCount()).isEqualTo(0); // No events published
    }

    @Test
    void should_only_expire_pending_status_payments() {
        // given:
        Payment expiredPendingPayment = new Payment(
                PAYMENT_1_ID,
                ORDER_1_ID,
                USER_1_ID,
                BigDecimal.valueOf(100.00),
                Currency.USD,
                PaymentStatus.PENDING,
                "payment-" + PAYMENT_1_ID.value(),
                DATE_2025_07_10_15_00_00, // Expired
                null,
                DATE_2025_07_10_15_00_00,
                null
        );

        Payment expiredPaidPayment = new Payment(
                PAYMENT_2_ID,
                ORDER_2_ID,
                USER_1_ID,
                BigDecimal.valueOf(200.00),
                Currency.USD,
                PaymentStatus.PAID, // Already paid, should not be expired
                "payment-" + PAYMENT_2_ID.value(),
                DATE_2025_07_10_15_00_00, // Technically past expiry but already paid
                DATE_2025_07_10_15_00_00,
                DATE_2025_07_10_15_00_00,
                DATE_2025_07_10_15_00_00
        );

        TestPaymentRepository paymentRepository = new TestPaymentRepository(List.of(expiredPendingPayment, expiredPaidPayment));
        TestPaymentEventPublisher eventPublisher = new TestPaymentEventPublisher();
        PaymentExpirationService expirationService = new PaymentExpirationService(
                paymentRepository,
                eventPublisher,
                Clock.fixed(DATE_2025_07_10_15_20_00.toInstant(ZoneOffset.UTC), ZoneOffset.UTC)
        );

        // when:
        expirationService.expirePayments();

        // then:
        Payment pendingPayment = paymentRepository.findById(PAYMENT_1_ID).get();
        Payment paidPayment = paymentRepository.findById(PAYMENT_2_ID).get();

        assertThat(pendingPayment.status()).isEqualTo(PaymentStatus.EXPIRED); // Was expired
        assertThat(paidPayment.status()).isEqualTo(PaymentStatus.PAID); // Still paid

        // Only 1 event for the pending payment
        assertThat(eventPublisher.getEventCount()).isEqualTo(1);
    }

    @Test
    void should_handle_empty_list_of_expired_payments() {
        // given:
        TestPaymentRepository paymentRepository = new TestPaymentRepository(Collections.emptyList());
        TestPaymentEventPublisher eventPublisher = new TestPaymentEventPublisher();
        PaymentExpirationService expirationService = new PaymentExpirationService(
                paymentRepository,
                eventPublisher,
                Clock.fixed(DATE_2025_07_10_15_20_00.toInstant(ZoneOffset.UTC), ZoneOffset.UTC)
        );

        // when:
        expirationService.expirePayments();

        // then:
        assertThat(eventPublisher.getEventCount()).isEqualTo(0);
    }

    @Test
    void should_update_payment_status_to_expired() {
        // given:
        Payment pendingPayment = new Payment(
                PAYMENT_1_ID,
                ORDER_1_ID,
                USER_1_ID,
                BigDecimal.valueOf(100.00),
                Currency.USD,
                PaymentStatus.PENDING,
                "payment-" + PAYMENT_1_ID.value(),
                DATE_2025_07_10_15_00_00,
                null,
                DATE_2025_07_10_15_00_00,
                null
        );

        TestPaymentRepository paymentRepository = new TestPaymentRepository(List.of(pendingPayment));
        TestPaymentEventPublisher eventPublisher = new TestPaymentEventPublisher();
        PaymentExpirationService expirationService = new PaymentExpirationService(
                paymentRepository,
                eventPublisher,
                Clock.fixed(DATE_2025_07_10_15_20_00.toInstant(ZoneOffset.UTC), ZoneOffset.UTC)
        );

        // when:
        expirationService.expire(pendingPayment);

        // then:
        Payment expiredPayment = paymentRepository.findById(PAYMENT_1_ID).get();
        assertThat(expiredPayment.status()).isEqualTo(PaymentStatus.EXPIRED);
        assertThat(expiredPayment.id()).isEqualTo(PAYMENT_1_ID);
        assertThat(expiredPayment.orderId()).isEqualTo(ORDER_1_ID);
        assertThat(expiredPayment.userId()).isEqualTo(USER_1_ID);
        assertThat(expiredPayment.orderTotal()).isEqualByComparingTo(BigDecimal.valueOf(100.00));
        assertThat(expiredPayment.currency()).isEqualTo(Currency.USD);
        assertThat(expiredPayment.paymentLink()).isEqualTo("payment-" + PAYMENT_1_ID.value());
        assertThat(expiredPayment.expiresAt()).isEqualTo(DATE_2025_07_10_15_00_00);
        assertThat(expiredPayment.paidAt()).isNull();
        assertThat(expiredPayment.createdAt()).isEqualTo(DATE_2025_07_10_15_00_00);
        assertThat(expiredPayment.updatedAt()).isEqualTo(DATE_2025_07_10_15_20_00);
    }

    @Test
    void should_publish_payment_processed_failed_event_when_expired() {
        // given:
        Payment pendingPayment = new Payment(
                PAYMENT_1_ID,
                ORDER_1_ID,
                USER_1_ID,
                BigDecimal.valueOf(100.00),
                Currency.USD,
                PaymentStatus.PENDING,
                "payment-" + PAYMENT_1_ID.value(),
                DATE_2025_07_10_15_00_00,
                null,
                DATE_2025_07_10_15_00_00,
                null
        );

        TestPaymentRepository paymentRepository = new TestPaymentRepository(List.of(pendingPayment));
        TestPaymentEventPublisher eventPublisher = new TestPaymentEventPublisher();
        PaymentExpirationService expirationService = new PaymentExpirationService(
                paymentRepository,
                eventPublisher,
                Clock.fixed(DATE_2025_07_10_15_20_00.toInstant(ZoneOffset.UTC), ZoneOffset.UTC)
        );

        // when:
        expirationService.expire(pendingPayment);

        // then:
        assertThat(eventPublisher.getEventCount()).isEqualTo(1);
        assertThat(eventPublisher.hasEvent(PaymentEvent.PaymentProcessedFailed.class)).isTrue();

        PaymentEvent.PaymentProcessedFailed event = (PaymentEvent.PaymentProcessedFailed) eventPublisher.getPublishedEvents().get(0);
        assertThat(event.orderId()).isEqualTo(ORDER_1_ID);
        assertThat(event.paymentId()).isEqualTo(PAYMENT_1_ID);
        assertThat(event.processedAt()).isEqualTo(DATE_2025_07_10_15_20_00);
    }

    @Test
    void should_expire_only_expired_payments_in_mixed_list() {
        // given:
        Payment expiredPayment = new Payment(
                PAYMENT_1_ID,
                ORDER_1_ID,
                USER_1_ID,
                BigDecimal.valueOf(100.00),
                Currency.USD,
                PaymentStatus.PENDING,
                "payment-" + PAYMENT_1_ID.value(),
                DATE_2025_07_10_15_00_00, // Expired
                null,
                DATE_2025_07_10_15_00_00,
                null
        );

        Payment activePayment = new Payment(
                PAYMENT_2_ID,
                ORDER_2_ID,
                USER_1_ID,
                BigDecimal.valueOf(200.00),
                Currency.USD,
                PaymentStatus.PENDING,
                "payment-" + PAYMENT_2_ID.value(),
                DATE_2025_07_10_15_20_00, // Not expired
                null,
                DATE_2025_07_10_15_00_00,
                null
        );

        Payment paidPayment = new Payment(
                PAYMENT_3_ID,
                ORDER_3_ID,
                USER_1_ID,
                BigDecimal.valueOf(300.00),
                Currency.USD,
                PaymentStatus.PAID,
                "payment-" + PAYMENT_3_ID.value(),
                DATE_2025_07_10_15_00_00,
                DATE_2025_07_10_15_00_00,
                DATE_2025_07_10_15_00_00,
                DATE_2025_07_10_15_00_00
        );

        TestPaymentRepository paymentRepository = new TestPaymentRepository(List.of(expiredPayment, activePayment, paidPayment));
        TestPaymentEventPublisher eventPublisher = new TestPaymentEventPublisher();
        PaymentExpirationService expirationService = new PaymentExpirationService(
                paymentRepository,
                eventPublisher,
                Clock.fixed(DATE_2025_07_10_15_10_00.toInstant(ZoneOffset.UTC), ZoneOffset.UTC)
        );

        // when:
        expirationService.expirePayments();

        // then:
        Payment payment1 = paymentRepository.findById(PAYMENT_1_ID).get();
        Payment payment2 = paymentRepository.findById(PAYMENT_2_ID).get();
        Payment payment3 = paymentRepository.findById(PAYMENT_3_ID).get();

        assertThat(payment1.status()).isEqualTo(PaymentStatus.EXPIRED); // Was expired
        assertThat(payment2.status()).isEqualTo(PaymentStatus.PENDING); // Still pending
        assertThat(payment3.status()).isEqualTo(PaymentStatus.PAID); // Still paid

        // Only 1 event for the expired payment
        assertThat(eventPublisher.getEventCount()).isEqualTo(1);
    }
}
