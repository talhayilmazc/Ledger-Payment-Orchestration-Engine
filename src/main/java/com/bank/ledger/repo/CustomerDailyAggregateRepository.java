package com.bank.ledger.repo;

import com.bank.ledger.domain.CustomerDailyAggregate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface CustomerDailyAggregateRepository extends JpaRepository<CustomerDailyAggregate, Long> {
    Optional<CustomerDailyAggregate> findByCustomerNoAndDay(String customerNo, LocalDate day);
}
