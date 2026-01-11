package com.bank.ledger.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "ledger_entries", indexes = {
        @Index(name="ix_ledger_journal", columnList = "journalId"),
        @Index(name="ix_ledger_account", columnList = "accountNo"),
        @Index(name="ix_ledger_ref", columnList = "referenceType,referenceId")
})
public class LedgerEntry {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String journalId; // one logical posting group

    @Column(nullable = false, length = 96)
    private String accountNo;

    @Column(nullable = false, length = 8)
    private String currency;

    @Column(nullable = false)
    private long amountCents;

    @Column(nullable = false, length = 8)
    private String direction; // DEBIT or CREDIT

    @Column(nullable = false, length = 32)
    private String referenceType; // PAYMENT, SETTLEMENT, TOPUP

    @Column(nullable = false, length = 64)
    private String referenceId; // PaymentInstruction.id etc.

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    @Column(length = 256)
    private String narrative;

    public LedgerEntry() {}

    public LedgerEntry(String journalId, String accountNo, String currency, long amountCents,
                       String direction, String referenceType, String referenceId, String narrative) {
        this.journalId = journalId;
        this.accountNo = accountNo;
        this.currency = currency;
        this.amountCents = amountCents;
        this.direction = direction;
        this.referenceType = referenceType;
        this.referenceId = referenceId;
        this.narrative = narrative;
    }

    public Long getId() { return id; }
    public String getJournalId() { return journalId; }
    public String getAccountNo() { return accountNo; }
    public String getCurrency() { return currency; }
    public long getAmountCents() { return amountCents; }
    public String getDirection() { return direction; }
    public String getReferenceType() { return referenceType; }
    public String getReferenceId() { return referenceId; }
    public Instant getCreatedAt() { return createdAt; }
    public String getNarrative() { return narrative; }
}
