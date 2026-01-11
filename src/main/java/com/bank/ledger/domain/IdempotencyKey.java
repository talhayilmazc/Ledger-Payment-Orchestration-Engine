package com.bank.ledger.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name="idempotency_keys")
public class IdempotencyKey {

    @Id
    @Column(length = 96)
    private String keyValue;

    @Column(nullable = false, length = 64)
    private String requestHash;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private Instant expiresAt;

    public IdempotencyKey() {}

    public IdempotencyKey(String keyValue, String requestHash, Instant expiresAt) {
        this.keyValue = keyValue;
        this.requestHash = requestHash;
        this.expiresAt = expiresAt;
    }

    public String getKeyValue() { return keyValue; }
    public String getRequestHash() { return requestHash; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getExpiresAt() { return expiresAt; }

    public boolean isExpired() {
        return expiresAt.isBefore(Instant.now());
    }
}
