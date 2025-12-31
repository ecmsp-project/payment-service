package com.ecmsp.paymentservice.payment.domain;

import java.util.ArrayList;
import java.util.List;

class TestPaymentEventPublisher implements PaymentEventPublisher {

    private final List<PaymentEvent> publishedEvents = new ArrayList<>();

    @Override
    public void publish(PaymentEvent event) {
        publishedEvents.add(event);
    }

    public List<PaymentEvent> getPublishedEvents() {
        return new ArrayList<>(publishedEvents);
    }

    public void clear() {
        publishedEvents.clear();
    }

    public int getEventCount() {
        return publishedEvents.size();
    }

    public boolean hasEvent(Class<? extends PaymentEvent> eventType) {
        return publishedEvents.stream()
                .anyMatch(eventType::isInstance);
    }
}
