package com.bank.ledger.domain;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name="customer_daily_agg", indexes = {
        @Index(name="ix_daily_customer_day", columnList="customerNo,day", unique = true)
})
public class CustomerDailyAggregate {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, length=64)
    private String customerNo;

    @Column(nullable=false)
    private LocalDate day;

    @Column(nullable=false)
    private long totalAmountCents;

    public CustomerDailyAggregate() {}

    public CustomerDailyAggregate(String customerNo, LocalDate day, long totalAmountCents) {
        this.customerNo = customerNo;
        this.day = day;
        this.totalAmountCents = totalAmountCents;
    }

    public Long getId() { return id; }
    public String getCustomerNo() { return customerNo; }
    public LocalDate getDay() { return day; }
    public long getTotalAmountCents() { return totalAmountCents; }

    public void add(long amountCents) { this.totalAmountCents += amountCents; }
}
