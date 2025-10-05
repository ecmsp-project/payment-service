package com.ecmsp.paymentservice.payment.domain;

import lombok.Getter;

@Getter
public enum Currency {
    PLN("PLN"),
    USD("USD"),
    EUR("EUR"),
    GBP("GBP");

    private final String code;

    Currency(String code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return code;
    }
}
