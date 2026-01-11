package com.bank.ledger.api;

import com.bank.ledger.domain.Account;
import com.bank.ledger.repo.AccountRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/accounts")
public class AccountController {

    private final AccountRepository repo;

    public AccountController(AccountRepository repo) { this.repo = repo; }

    @GetMapping("/{accountNo}")
    public Account get(@PathVariable String accountNo) {
        return repo.findById(accountNo).orElseThrow();
    }

    @GetMapping
    public List<Account> listByCustomer(@RequestParam String customerNo) {
        return repo.findTop200ByCustomerNoOrderByAccountNoAsc(customerNo);
    }
}
