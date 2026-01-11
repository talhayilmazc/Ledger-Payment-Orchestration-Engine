package com.bank.ledger.service;

import com.bank.ledger.domain.Account;
import com.bank.ledger.domain.LedgerEntry;
import com.bank.ledger.repo.AccountRepository;
import com.bank.ledger.repo.LedgerEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class LedgerService {

    private final AccountRepository accountRepo;
    private final LedgerEntryRepository ledgerRepo;
    private final MetricsService metrics;

    public LedgerService(AccountRepository accountRepo, LedgerEntryRepository ledgerRepo, MetricsService metrics) {
        this.accountRepo = accountRepo;
        this.ledgerRepo = ledgerRepo;
        this.metrics = metrics;
    }

    /**
     * Posts a balanced double-entry journal.
     * Debit one account, credit another account with same amount/currency.
     */
    @Transactional
    public String postBalanced(String debitAccountNo,
                               String creditAccountNo,
                               String currency,
                               long amountCents,
                               String referenceType,
                               String referenceId,
                               String narrative) {

        if (amountCents <= 0) throw new IllegalArgumentException("amount must be > 0");
        if (debitAccountNo.equalsIgnoreCase(creditAccountNo)) throw new IllegalArgumentException("debit and credit cannot be same");

        Account debit = accountRepo.findById(debitAccountNo)
                .orElseThrow(() -> new IllegalArgumentException("Debit account not found: " + debitAccountNo));
        Account credit = accountRepo.findById(creditAccountNo)
                .orElseThrow(() -> new IllegalArgumentException("Credit account not found: " + creditAccountNo));

        if (!currency.equalsIgnoreCase(debit.getCurrency()) || !currency.equalsIgnoreCase(credit.getCurrency())) {
            throw new IllegalArgumentException("currency mismatch");
        }

        // banking: available balance check for debit side
        if (!debit.hasSufficientAvailable(amountCents)) {
            throw new IllegalArgumentException("insufficient funds");
        }

        String journalId = UUID.randomUUID().toString();

        LedgerEntry dr = new LedgerEntry(journalId, debitAccountNo, currency, amountCents,
                "DEBIT", referenceType, referenceId, narrative);

        LedgerEntry cr = new LedgerEntry(journalId, creditAccountNo, currency, amountCents,
                "CREDIT", referenceType, referenceId, narrative);

        // persist journal
        ledgerRepo.save(dr);
        ledgerRepo.save(cr);

        // update balances
        debit.debit(amountCents);
        credit.credit(amountCents);
        accountRepo.save(debit);
        accountRepo.save(credit);

        metrics.incPosting("DEBIT");
        metrics.incPosting("CREDIT");

        // sanity check (balanced)
        // (We enforce it by symmetric creation; this is still a guard.)
        if (dr.getAmountCents() != cr.getAmountCents()) {
            throw new IllegalStateException("unbalanced journal");
        }

        return journalId;
    }
}
