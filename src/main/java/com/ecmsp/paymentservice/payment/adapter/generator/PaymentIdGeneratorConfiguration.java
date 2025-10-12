package com.ecmsp.paymentservice.payment.adapter.generator;

import com.ecmsp.paymentservice.payment.domain.PaymentIdGenerator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class PaymentIdGeneratorConfiguration {

    @Bean
    @ConditionalOnProperty(
            name = "payment.id-generator.type",
            havingValue = "random",
            matchIfMissing = true  // Default to random
    )
    PaymentIdGenerator randomPaymentIdGenerator() {
        return new RandomPaymentIdGenerator();
    }

    @Bean
    @ConditionalOnProperty(
            name = "payment.id-generator.type",
            havingValue = "fixed",
            matchIfMissing = false
    )
    PaymentIdGenerator fixedPaymentIdGenerator() {
        return new FixedPaymentIdGenerator();
    }
}
