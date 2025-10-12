package com.ecmsp.paymentservice.payment.adapter.repository.db;

import com.ecmsp.paymentservice.payment.domain.PaymentRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.time.Clock;

@Configuration
@ConditionalOnProperty(
        prefix = "payment.repository",
        name = "type",
        havingValue = "db")
@Import({DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
@EnableJpaRepositories(basePackages = "com.ecmsp.paymentservice.payment.adapter.repository.db")
class DbPaymentRepositoryConfiguration {

    @Bean
    PaymentRepository dbPaymentRepository(PaymentEntityRepository paymentEntityRepository) {
        return new DbPaymentRepository(
                /* paymentEntityRepository = */ paymentEntityRepository
        );
    }
}
