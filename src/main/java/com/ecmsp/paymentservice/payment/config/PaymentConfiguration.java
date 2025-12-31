package com.ecmsp.paymentservice.payment.config;

import com.ecmsp.paymentservice.payment.domain.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
class PaymentConfiguration {


    @Bean
    PaymentExpirationService paymentExpirationService(PaymentRepository paymentRepository, PaymentEventPublisher paymentEventPublisher, Clock clock) {
        return new PaymentExpirationService(paymentRepository, paymentEventPublisher, clock);
    }

    @Bean
    PaymentFacade paymentFacade(
            PaymentRepository paymentRepository,
            PaymentExpirationService paymentExpirationService,
            PaymentEventPublisher paymentEventPublisher,
            PaymentIdGenerator paymentIdGenerator,
            Clock clock
    ) {
        return new DefaultPaymentFacade(
                paymentRepository,
                paymentExpirationService,
                paymentEventPublisher,
                paymentIdGenerator,
                clock
        );

    }





}
