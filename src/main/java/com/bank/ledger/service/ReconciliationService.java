package com.bank.ledger.service;

import com.bank.ledger.repo.LedgerEntryRepository;
import com.bank.ledger.repo.PaymentInstructionRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ReconciliationService {

    private final PaymentInstructionRepository payRepo;
    private final LedgerEntryRepository ledgerRepo;

    public ReconciliationService(PaymentInstructionRepository payRepo, LedgerEntryRepository ledgerRepo) {
        this.payRepo = payRepo;
        this.ledgerRepo = ledgerRepo;
    }

    /**
     * Demo reconciliation:
     * For a given paymentId, checks:
     * - payment exists
     * - ledger has 2 entries for reference (PAYMENT, paymentId)
     * - debit/credit amounts match
     */
    public Map<String, Object> reconcilePayment(String paymentId) {
        var pi = payRepo.findById(paymentId).orElseThrow();
        var entries = ledgerRepo.findTop500ByReferenceTypeAndReferenceIdOrderByCreatedAtAsc("PAYMENT", paymentId);

        Map<String, Object> out = new HashMap<>();
        out.put("paymentId", paymentId);
        out.put("paymentStatus", pi.getStatus());
        out.put("entryCount", entries.size());

        if (entries.size() != 2) {
            out.put("ok", false);
            out.put("reason", "Expected 2 ledger entries for double-entry, got " + entries.size());
            return out;
        }

        long debit = entries.stream().filter(e -> "DEBIT".equalsIgnoreCase(e.getDirection())).mapToLong(e -> e.getAmountCents()).sum();
        long credit = entries.stream().filter(e -> "CREDIT".equalsIgnoreCase(e.getDirection())).mapToLong(e -> e.getAmountCents()).sum();

        out.put("debitTotalCents", debit);
        out.put("creditTotalCents", credit);
        out.put("ok", debit == credit && debit == pi.getAmountCents());

        if (!(debit == credit && debit == pi.getAmountCents())) {
            out.put("reason", "Mismatch between payment amount and ledger totals");
        }
        return out;
    }
}
