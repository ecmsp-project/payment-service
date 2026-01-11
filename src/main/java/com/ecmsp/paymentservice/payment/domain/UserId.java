package com.ecmsp.paymentservice.payment.domain;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.UUID;

public record UserId(@JsonValue UUID value) {
    @Override
    public String toString(){
        return value.toString();
    }
}
