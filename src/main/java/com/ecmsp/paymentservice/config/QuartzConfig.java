package com.ecmsp.paymentservice.config;

import com.ecmsp.paymentservice.job.PaymentExpirationJob;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzConfig {

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