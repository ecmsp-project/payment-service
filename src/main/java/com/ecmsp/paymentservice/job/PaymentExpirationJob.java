package com.ecmsp.paymentservice.job;

import com.ecmsp.paymentservice.payment.domain.PaymentExpirationService;
import com.ecmsp.paymentservice.payment.domain.PaymentFacade;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

@Component
@Slf4j
class PaymentExpirationJob implements Job {

    private PaymentExpirationService paymentExpirationService;

    public PaymentExpirationJob(PaymentExpirationService paymentExpirationService) {
        this.paymentExpirationService = paymentExpirationService;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("Starting scheduled payment expiration job");
        try {
            paymentExpirationService.expirePayments();
            log.info("Payment expiration job completed successfully");
        } catch (Exception e) {
            log.error("Error during payment expiration job", e);
            throw new JobExecutionException(e);
        }
    }
} 