package com.ecmsp.paymentservice.payment.domain;

import java.util.UUID;

public record UserId(UUID value) {
    @Override
    public String toString(){
        return value.toString();
    }
}
