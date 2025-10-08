package com.ecmsp.paymentservice.payment.domain;

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

    public String getCode() {
        return code;
    }
}
