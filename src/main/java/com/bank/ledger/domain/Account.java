package com.bank.ledger.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "accounts", indexes = {
        @Index(name="ix_accounts_customer", columnList = "customerNo"),
        @Index(name="ix_accounts_type", columnList = "accountType")
})
public class Account {

    @Id
    @Column(length = 96)
    private String accountNo; // e.g. ACC:CUST0001:DEPOSIT or SUSPENSE:P2P_OUT

    @Column(length = 64)
    private String customerNo; // null for system/suspense accounts

    @Column(nullable = false, length = 24)
    private String accountType; // DEPOSIT, SUSPENSE, GL

    @Column(nullable = false, length = 8)
    private String currency; // TRY, USD...

    @Column(nullable = false)
    private long availableBalanceCents = 0;

    @Column(nullable = false)
    private long ledgerBalanceCents = 0;

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    public Account() {}

    public Account(String accountNo, String customerNo, String accountType, String currency) {
        this.accountNo = accountNo;
        this.customerNo = customerNo;
        this.accountType = accountType;
        this.currency = currency;
    }

    public String getAccountNo() { return accountNo; }
    public String getCustomerNo() { return customerNo; }
    public String getAccountType() { return accountType; }
    public String getCurrency() { return currency; }
    public long getAvailableBalanceCents() { return availableBalanceCents; }
    public long getLedgerBalanceCents() { return ledgerBalanceCents; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void credit(long amountCents) {
        ledgerBalanceCents += amountCents;
        availableBalanceCents += amountCents;
        updatedAt = Instant.now();
    }

    public void debit(long amountCents) {
        ledgerBalanceCents -= amountCents;
        availableBalanceCents -= amountCents;
        updatedAt = Instant.now();
    }

    public boolean hasSufficientAvailable(long amountCents) {
        return availableBalanceCents >= amountCents;
    }
}
