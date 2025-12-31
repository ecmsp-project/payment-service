package com.ecmsp.paymentservice.aplication.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
class ApplicationConfiguration {

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }



}
