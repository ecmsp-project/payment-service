package com.ecmsp.paymentservice.payment.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class DefaultPaymentFacadeTest {

    private static final PaymentId PAYMENT_1_ID = new PaymentId(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));
    private static final PaymentId PAYMENT_2_ID = new PaymentId(UUID.fromString("9e349a18-1203-4224-829c-dc15700c68a5"));
    private static final OrderId ORDER_1_ID = new OrderId(UUID.fromString("c5e75ab0-a110-4a2a-b6f4-c4573e6f548e"));
    private static final OrderId ORDER_2_ID = new OrderId(UUID.fromString("b5d1eec8-c3ea-4b55-8cec-900b5c018381"));
    private static final UserId USER_1_ID = new UserId(UUID.fromString("b259c7f1-483b-4700-accc-1554542eb8f5"));
    private static final UserId USER_2_ID = new UserId(UUID.fromString("66d155e8-2d57-44fa-9adc-580e1e4f9cc9"));
    private static final CorrelationId CORRELATION_ID = new CorrelationId(UUID.fromString("473c1579-12b1-49b0-b90e-253782c874a5"));

    private static final LocalDateTime DATE_2025_07_10_15_00_00 = LocalDateTime.of(2025, 7, 10, 15, 0, 0);
    private static final LocalDateTime DATE_2025_07_10_15_10_00 = LocalDateTime.of(2025, 7, 10, 15, 10, 0);
    private static final LocalDateTime DATE_2025_07_10_15_15_00 = LocalDateTime.of(2025, 7, 10, 15, 15, 0);

    @Test
    void should_create_payment_with_correct_expiry_time() {
        // given:
        TestPaymentRepository paymentRepository = new TestPaymentRepository(Collections.emptyList());
        TestPaymentEventPublisher eventPublisher = new TestPaymentEventPublisher();
        PaymentExpirationService expirationService = new PaymentExpirationService(
                paymentRepository,
                eventPublisher,
                Clock.fixed(DATE_2025_07_10_15_00_00.toInstant(ZoneOffset.UTC), ZoneOffset.UTC)
        );
        PaymentFacade facade = new DefaultPaymentFacade(
                paymentRepository,
                expirationService,
                eventPublisher,
                (correlationId) -> PAYMENT_1_ID,
                Clock.fixed(DATE_2025_07_10_15_00_00.toInstant(ZoneOffset.UTC), ZoneOffset.UTC)
        );

        PaymentToCreate paymentToCreate = new PaymentToCreate(
                ORDER_1_ID,
                USER_1_ID,
                BigDecimal.valueOf(100.00),
                Currency.USD,
                DATE_2025_07_10_15_00_00
        );

        // when:
        Payment createdPayment = facade.createPayment(paymentToCreate, new Context(CORRELATION_ID));

        // then:
        assertThat(createdPayment.id()).isEqualTo(PAYMENT_1_ID);
        assertThat(createdPayment.orderId()).isEqualTo(ORDER_1_ID);
        assertThat(createdPayment.userId()).isEqualTo(USER_1_ID);
        assertThat(createdPayment.orderTotal()).isEqualByComparingTo(BigDecimal.valueOf(100.00));
        assertThat(createdPayment.currency()).isEqualTo(Currency.USD);
        assertThat(createdPayment.status()).isEqualTo(PaymentStatus.PENDING);
        assertThat(createdPayment.paymentLink()).isEqualTo("payment-" + PAYMENT_1_ID.value());
        assertThat(createdPayment.expiresAt()).isEqualTo(DATE_2025_07_10_15_10_00); // 10 minutes later
        assertThat(createdPayment.paidAt()).isNull();
        assertThat(createdPayment.createdAt()).isEqualTo(DATE_2025_07_10_15_00_00);
        assertThat(createdPayment.updatedAt()).isNull();
    }

    @Test
    void should_process_payment_successfully() {
        // given:
        Payment pendingPayment = new Payment(
                PAYMENT_1_ID,
                ORDER_1_ID,
                USER_1_ID,
                BigDecimal.valueOf(100.00),
                Currency.USD,
                PaymentStatus.PENDING,
                "payment-" + PAYMENT_1_ID.value(),
                DATE_2025_07_10_15_10_00,
                null,
                DATE_2025_07_10_15_00_00,
                null
        );

        TestPaymentRepository paymentRepository = new TestPaymentRepository(List.of(pendingPayment));
        TestPaymentEventPublisher eventPublisher = new TestPaymentEventPublisher();
        PaymentExpirationService expirationService = new PaymentExpirationService(
                paymentRepository,
                eventPublisher,
                Clock.fixed(DATE_2025_07_10_15_00_00.toInstant(ZoneOffset.UTC), ZoneOffset.UTC)
        );
        PaymentFacade facade = new DefaultPaymentFacade(
                paymentRepository,
                expirationService,
                eventPublisher,
                (correlationId) -> PAYMENT_1_ID,
                Clock.fixed(DATE_2025_07_10_15_00_00.toInstant(ZoneOffset.UTC), ZoneOffset.UTC)
        );

        // when:
        Payment processedPayment = facade.processPayment("payment-" + PAYMENT_1_ID.value());

        // then:
        assertThat(processedPayment.status()).isEqualTo(PaymentStatus.PAID);
        assertThat(processedPayment.paidAt()).isEqualTo(DATE_2025_07_10_15_00_00);
        assertThat(processedPayment.updatedAt()).isEqualTo(DATE_2025_07_10_15_00_00);

        // Verify event was published
        assertThat(eventPublisher.getEventCount()).isEqualTo(1);
        assertThat(eventPublisher.hasEvent(PaymentEvent.PaymentProcessedSucceeded.class)).isTrue();
    }

    @Test
    void should_fail_when_payment_not_found() {
        // given:
        TestPaymentRepository paymentRepository = new TestPaymentRepository(Collections.emptyList());
        TestPaymentEventPublisher eventPublisher = new TestPaymentEventPublisher();
        PaymentExpirationService expirationService = new PaymentExpirationService(
                paymentRepository,
                eventPublisher,
                Clock.fixed(DATE_2025_07_10_15_00_00.toInstant(ZoneOffset.UTC), ZoneOffset.UTC)
        );
        PaymentFacade facade = new DefaultPaymentFacade(
                paymentRepository,
                expirationService,
                eventPublisher,
                (correlationId) -> PAYMENT_1_ID,
                Clock.fixed(DATE_2025_07_10_15_00_00.toInstant(ZoneOffset.UTC), ZoneOffset.UTC)
        );

        // when & then:
        assertThatThrownBy(() -> facade.processPayment("payment-nonexistent"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Payment not found for link: payment-nonexistent");
    }

    @Test
    void should_fail_when_payment_is_not_in_pending_status() {
        // given:
        Payment paidPayment = new Payment(
                PAYMENT_1_ID,
                ORDER_1_ID,
                USER_1_ID,
                BigDecimal.valueOf(100.00),
                Currency.USD,
                PaymentStatus.PAID,
                "payment-" + PAYMENT_1_ID.value(),
                DATE_2025_07_10_15_10_00,
                DATE_2025_07_10_15_00_00,
                DATE_2025_07_10_15_00_00,
                DATE_2025_07_10_15_00_00
        );

        TestPaymentRepository paymentRepository = new TestPaymentRepository(List.of(paidPayment));
        TestPaymentEventPublisher eventPublisher = new TestPaymentEventPublisher();
        PaymentExpirationService expirationService = new PaymentExpirationService(
                paymentRepository,
                eventPublisher,
                Clock.fixed(DATE_2025_07_10_15_00_00.toInstant(ZoneOffset.UTC), ZoneOffset.UTC)
        );
        PaymentFacade facade = new DefaultPaymentFacade(
                paymentRepository,
                expirationService,
                eventPublisher,
                (correlationId) -> PAYMENT_1_ID,
                Clock.fixed(DATE_2025_07_10_15_00_00.toInstant(ZoneOffset.UTC), ZoneOffset.UTC)
        );

        // when & then:
        assertThatThrownBy(() -> facade.processPayment("payment-" + PAYMENT_1_ID.value()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Payment is not in PENDING status");
    }

    @Test
    void should_fail_when_payment_has_expired() {
        // given:
        Payment expiredPayment = new Payment(
                PAYMENT_1_ID,
                ORDER_1_ID,
                USER_1_ID,
                BigDecimal.valueOf(100.00),
                Currency.USD,
                PaymentStatus.PENDING,
                "payment-" + PAYMENT_1_ID.value(),
                DATE_2025_07_10_15_00_00, // Already expired
                null,
                DATE_2025_07_10_15_00_00,
                null
        );

        TestPaymentRepository paymentRepository = new TestPaymentRepository(List.of(expiredPayment));
        TestPaymentEventPublisher eventPublisher = new TestPaymentEventPublisher();
        // Clock is 15 minutes after expiry
        PaymentExpirationService expirationService = new PaymentExpirationService(
                paymentRepository,
                eventPublisher,
                Clock.fixed(DATE_2025_07_10_15_15_00.toInstant(ZoneOffset.UTC), ZoneOffset.UTC)
        );
        PaymentFacade facade = new DefaultPaymentFacade(
                paymentRepository,
                expirationService,
                eventPublisher,
                (correlationId) -> PAYMENT_1_ID,
                Clock.fixed(DATE_2025_07_10_15_15_00.toInstant(ZoneOffset.UTC), ZoneOffset.UTC)
        );

        // when & then:
        assertThatThrownBy(() -> facade.processPayment("payment-" + PAYMENT_1_ID.value()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Payment has expired");

        // Verify the payment was marked as expired
        Payment updatedPayment = paymentRepository.findById(PAYMENT_1_ID).get();
        assertThat(updatedPayment.status()).isEqualTo(PaymentStatus.EXPIRED);

        // Verify failed event was published
        assertThat(eventPublisher.hasEvent(PaymentEvent.PaymentProcessedFailed.class)).isTrue();
    }

    @Test
    void should_find_payment_by_id() {
        // given:
        Payment payment = new Payment(
                PAYMENT_1_ID,
                ORDER_1_ID,
                USER_1_ID,
                BigDecimal.valueOf(100.00),
                Currency.USD,
                PaymentStatus.PENDING,
                "payment-" + PAYMENT_1_ID.value(),
                DATE_2025_07_10_15_10_00,
                null,
                DATE_2025_07_10_15_00_00,
                null
        );

        TestPaymentRepository paymentRepository = new TestPaymentRepository(List.of(payment));
        TestPaymentEventPublisher eventPublisher = new TestPaymentEventPublisher();
        PaymentExpirationService expirationService = new PaymentExpirationService(
                paymentRepository,
                eventPublisher,
                Clock.fixed(DATE_2025_07_10_15_00_00.toInstant(ZoneOffset.UTC), ZoneOffset.UTC)
        );
        PaymentFacade facade = new DefaultPaymentFacade(
                paymentRepository,
                expirationService,
                eventPublisher,
                (correlationId) -> PAYMENT_1_ID,
                Clock.fixed(DATE_2025_07_10_15_00_00.toInstant(ZoneOffset.UTC), ZoneOffset.UTC)
        );

        // when:
        Optional<Payment> foundPayment = facade.getPaymentById(PAYMENT_1_ID);

        // then:
        assertThat(foundPayment).isPresent();
        assertThat(foundPayment.get()).isEqualTo(payment);
    }

    @Test
    void should_not_find_payment_when_id_does_not_exist() {
        // given:
        TestPaymentRepository paymentRepository = new TestPaymentRepository(Collections.emptyList());
        TestPaymentEventPublisher eventPublisher = new TestPaymentEventPublisher();
        PaymentExpirationService expirationService = new PaymentExpirationService(
                paymentRepository,
                eventPublisher,
                Clock.fixed(DATE_2025_07_10_15_00_00.toInstant(ZoneOffset.UTC), ZoneOffset.UTC)
        );
        PaymentFacade facade = new DefaultPaymentFacade(
                paymentRepository,
                expirationService,
                eventPublisher,
                (correlationId) -> PAYMENT_1_ID,
                Clock.fixed(DATE_2025_07_10_15_00_00.toInstant(ZoneOffset.UTC), ZoneOffset.UTC)
        );

        // when:
        Optional<Payment> foundPayment = facade.getPaymentById(PAYMENT_1_ID);

        // then:
        assertThat(foundPayment).isNotPresent();
    }

    @Test
    void should_find_payment_by_order_id() {
        // given:
        Payment payment = new Payment(
                PAYMENT_1_ID,
                ORDER_1_ID,
                USER_1_ID,
                BigDecimal.valueOf(100.00),
                Currency.USD,
                PaymentStatus.PENDING,
                "payment-" + PAYMENT_1_ID.value(),
                DATE_2025_07_10_15_10_00,
                null,
                DATE_2025_07_10_15_00_00,
                null
        );

        TestPaymentRepository paymentRepository = new TestPaymentRepository(List.of(payment));
        TestPaymentEventPublisher eventPublisher = new TestPaymentEventPublisher();
        PaymentExpirationService expirationService = new PaymentExpirationService(
                paymentRepository,
                eventPublisher,
                Clock.fixed(DATE_2025_07_10_15_00_00.toInstant(ZoneOffset.UTC), ZoneOffset.UTC)
        );
        PaymentFacade facade = new DefaultPaymentFacade(
                paymentRepository,
                expirationService,
                eventPublisher,
                (correlationId) -> PAYMENT_1_ID,
                Clock.fixed(DATE_2025_07_10_15_00_00.toInstant(ZoneOffset.UTC), ZoneOffset.UTC)
        );

        // when:
        Optional<Payment> foundPayment = facade.getPaymentByOrderId(ORDER_1_ID);

        // then:
        assertThat(foundPayment).isPresent();
        assertThat(foundPayment.get()).isEqualTo(payment);
    }

    @Test
    void should_not_find_payment_when_order_id_does_not_exist() {
        // given:
        TestPaymentRepository paymentRepository = new TestPaymentRepository(Collections.emptyList());
        TestPaymentEventPublisher eventPublisher = new TestPaymentEventPublisher();
        PaymentExpirationService expirationService = new PaymentExpirationService(
                paymentRepository,
                eventPublisher,
                Clock.fixed(DATE_2025_07_10_15_00_00.toInstant(ZoneOffset.UTC), ZoneOffset.UTC)
        );
        PaymentFacade facade = new DefaultPaymentFacade(
                paymentRepository,
                expirationService,
                eventPublisher,
                (correlationId) -> PAYMENT_1_ID,
                Clock.fixed(DATE_2025_07_10_15_00_00.toInstant(ZoneOffset.UTC), ZoneOffset.UTC)
        );

        // when:
        Optional<Payment> foundPayment = facade.getPaymentByOrderId(ORDER_1_ID);

        // then:
        assertThat(foundPayment).isNotPresent();
    }

    @Test
    void should_find_multiple_payments_by_user_id() {
        // given:
        Payment payment1 = new Payment(
                PAYMENT_1_ID,
                ORDER_1_ID,
                USER_1_ID,
                BigDecimal.valueOf(100.00),
                Currency.USD,
                PaymentStatus.PENDING,
                "payment-" + PAYMENT_1_ID.value(),
                DATE_2025_07_10_15_10_00,
                null,
                DATE_2025_07_10_15_00_00,
                null
        );

        Payment payment2 = new Payment(
                PAYMENT_2_ID,
                ORDER_2_ID,
                USER_1_ID,
                BigDecimal.valueOf(200.00),
                Currency.USD,
                PaymentStatus.PAID,
                "payment-" + PAYMENT_2_ID.value(),
                DATE_2025_07_10_15_10_00,
                DATE_2025_07_10_15_00_00,
                DATE_2025_07_10_15_00_00,
                DATE_2025_07_10_15_00_00
        );

        TestPaymentRepository paymentRepository = new TestPaymentRepository(List.of(payment1, payment2));
        TestPaymentEventPublisher eventPublisher = new TestPaymentEventPublisher();
        PaymentExpirationService expirationService = new PaymentExpirationService(
                paymentRepository,
                eventPublisher,
                Clock.fixed(DATE_2025_07_10_15_00_00.toInstant(ZoneOffset.UTC), ZoneOffset.UTC)
        );
        PaymentFacade facade = new DefaultPaymentFacade(
                paymentRepository,
                expirationService,
                eventPublisher,
                (correlationId) -> PAYMENT_1_ID,
                Clock.fixed(DATE_2025_07_10_15_00_00.toInstant(ZoneOffset.UTC), ZoneOffset.UTC)
        );

        // when:
        List<Payment> userPayments = facade.getPaymentsByUserId(USER_1_ID);

        // then:
        assertThat(userPayments).hasSize(2);
        assertThat(userPayments).containsExactlyInAnyOrder(payment1, payment2);
    }

    @Test
    void should_return_empty_list_when_user_has_no_payments() {
        // given:
        TestPaymentRepository paymentRepository = new TestPaymentRepository(Collections.emptyList());
        TestPaymentEventPublisher eventPublisher = new TestPaymentEventPublisher();
        PaymentExpirationService expirationService = new PaymentExpirationService(
                paymentRepository,
                eventPublisher,
                Clock.fixed(DATE_2025_07_10_15_00_00.toInstant(ZoneOffset.UTC), ZoneOffset.UTC)
        );
        PaymentFacade facade = new DefaultPaymentFacade(
                paymentRepository,
                expirationService,
                eventPublisher,
                (correlationId) -> PAYMENT_1_ID,
                Clock.fixed(DATE_2025_07_10_15_00_00.toInstant(ZoneOffset.UTC), ZoneOffset.UTC)
        );

        // when:
        List<Payment> userPayments = facade.getPaymentsByUserId(USER_1_ID);

        // then:
        assertThat(userPayments).isEmpty();
    }
}
