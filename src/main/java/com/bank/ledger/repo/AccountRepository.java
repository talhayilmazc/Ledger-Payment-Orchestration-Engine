package com.bank.ledger.repo;

import com.bank.ledger.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountRepository extends JpaRepository<Account, String> {
    List<Account> findTop200ByCustomerNoOrderByAccountNoAsc(String customerNo);
}
