package com.ecmsp.paymentservice.payment.adapter.job;

import com.ecmsp.paymentservice.payment.adapter.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentExpirationJob implements Job {

    private final PaymentService paymentService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("Starting scheduled payment expiration job");
        try {
            paymentService.expirePayments();
            log.info("Payment expiration job completed successfully");
        } catch (Exception e) {
            log.error("Error during payment expiration job", e);
            throw new JobExecutionException(e);
        }
    }
} 