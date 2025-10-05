package com.ecmsp.paymentservice.payment.config;

import com.ecmsp.paymentservice.payment.adapter.job.PaymentExpirationJob;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzConfiguration {

    @Bean
    public JobDetail paymentExpirationJobDetail() {
        return JobBuilder.newJob(PaymentExpirationJob.class)
                .withIdentity("paymentExpirationJob")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger paymentExpirationJobTrigger() {
        SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder
                .simpleSchedule()
                .withIntervalInMinutes(10)
                .repeatForever();

        return TriggerBuilder.newTrigger()
                .forJob(paymentExpirationJobDetail())
                .withIdentity("paymentExpirationTrigger")
                .withSchedule(scheduleBuilder)
                .startAt(DateBuilder.evenMinuteDateAfterNow())
                .build();
    }
} 