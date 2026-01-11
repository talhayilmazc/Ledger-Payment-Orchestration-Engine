package com.bank.ledger.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

@Service
public class MetricsService {

    private final MeterRegistry registry;

    public MetricsService(MeterRegistry registry) {
        this.registry = registry;
    }

    public void incPaymentAuthorized(String product, String channel) {
        Counter.builder("payments_authorized_total")
                .description("Payments authorized total")
                .tag("product", safe(product))
                .tag("channel", safe(channel))
                .register(registry)
                .increment();
    }

    public void incPaymentSettled(String product) {
        Counter.builder("payments_settled_total")
                .description("Payments settled total")
                .tag("product", safe(product))
                .register(registry)
                .increment();
    }

    public void incPosting(String direction) {
        Counter.builder("ledger_postings_total")
                .description("Ledger postings total")
                .tag("direction", safe(direction))
                .register(registry)
                .increment();
    }

    public void incIdempotencyHit() {
        Counter.builder("idempotency_hits_total")
                .description("Idempotency hits total")
                .register(registry)
                .increment();
    }

    public void incLimitReject(String type) {
        Counter.builder("limit_rejects_total")
                .description("Rejected due to limits total")
                .tag("type", safe(type))
                .register(registry)
                .increment();
    }

    private String safe(String s) {
        if (s == null || s.isBlank()) return "unknown";
        return s.replace(" ", "_").toLowerCase();
    }
}
