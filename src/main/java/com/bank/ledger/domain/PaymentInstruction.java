package com.bank.ledger.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payment_instructions", indexes = {
        @Index(name="ix_pay_customer", columnList = "customerNo"),
        @Index(name="ix_pay_status", columnList = "status"),
        @Index(name="ix_pay_idem", columnList = "idempotencyKey", unique = true)
})
public class PaymentInstruction {

    @Id
    @Column(length = 64)
    private String id = UUID.randomUUID().toString();

    @Column(nullable = false, length = 64)
    private String idempotencyKey;

    @Column(nullable = false, length = 64)
    private String customerNo;

    @Column(length = 64)
    private String counterpartyCustomerNo; // for settlement target (P2P)

    @Column(nullable = false, length = 32)
    private String product; // P2P_TRANSFER, BILL_PAYMENT, ...

    @Column(nullable = false, length = 16)
    private String channel; // MOBILE, BRANCH, API, BATCH

    @Column(nullable = false)
    private long amountCents;

    @Column(nullable = false, length = 8)
    private String currency;

    @Column(nullable = false, length = 24)
    private String status; // AUTHORIZED, SETTLED, REJECTED

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    public PaymentInstruction() {}

    public PaymentInstruction(String idempotencyKey, String customerNo, String counterpartyCustomerNo,
                              String product, String channel, long amountCents, String currency) {
        this.idempotencyKey = idempotencyKey;
        this.customerNo = customerNo;
        this.counterpartyCustomerNo = counterpartyCustomerNo;
        this.product = product;
        this.channel = channel;
        this.amountCents = amountCents;
        this.currency = currency;
        this.status = "AUTHORIZED";
    }

    public String getId() { return id; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public String getCustomerNo() { return customerNo; }
    public String getCounterpartyCustomerNo() { return counterpartyCustomerNo; }
    public String getProduct() { return product; }
    public String getChannel() { return channel; }
    public long getAmountCents() { return amountCents; }
    public String getCurrency() { return currency; }
    public String getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }

    public void setStatus(String status) { this.status = status; }
}
