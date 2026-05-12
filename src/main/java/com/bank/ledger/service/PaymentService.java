package com.bank.ledger.service;

import com.bank.ledger.domain.Account;
import com.bank.ledger.domain.IdempotencyKey;
import com.bank.ledger.domain.PaymentInstruction;
import com.bank.ledger.repo.AccountRepository;
import com.bank.ledger.repo.IdempotencyKeyRepository;
import com.bank.ledger.repo.PaymentInstructionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;

@Service
public class PaymentService {

    private final PaymentInstructionRepository payRepo;
    private final IdempotencyKeyRepository idemRepo;
    private final AccountRepository accountRepo;

    private final PostingRuleEngine postingRules;
    private final LimitService limits;
    private final LedgerService ledger;
    private final MetricsService metrics;

    private final String defaultCurrency;

    public PaymentService(PaymentInstructionRepository payRepo,
                          IdempotencyKeyRepository idemRepo,
                          AccountRepository accountRepo,
                          PostingRuleEngine postingRules,
                          LimitService limits,
                          LedgerService ledger,
                          MetricsService metrics,
                          @org.springframework.beans.factory.annotation.Value("${app.defaultCurrency}") String defaultCurrency) {
        this.payRepo = payRepo;
        this.idemRepo = idemRepo;
        this.accountRepo = accountRepo;
        this.postingRules = postingRules;
        this.limits = limits;
        this.ledger = ledger;
        this.metrics = metrics;
        this.defaultCurrency = defaultCurrency;
    }

    /**
     * AUTHORIZATION step:
     * - checks idempotency
     * - checks limits
     * - creates PaymentInstruction in AUTHORIZED state
     * - creates accounts if missing (demo-grade convenience)
     */
    @Transactional
    public PaymentInstruction authorize(String idempotencyKey,
                                        String customerNo,
                                        String counterpartyCustomerNo,
                                        String product,
                                        String channel,
                                        long amountCents,
                                        String currency) {

        if (currency == null || currency.isBlank()) currency = defaultCurrency;

        // idempotency fast return
        var existing = payRepo.findByIdempotencyKey(idempotencyKey).orElse(null);
        if (existing != null) {
            metrics.incIdempotencyHit();
            return existing;
        }

        // idempotency lock by key (best effort)
        String reqHash = sha256(customerNo + "|" + counterpartyCustomerNo + "|" + product + "|" + channel + "|" + amountCents + "|" + currency);
        IdempotencyKey existingKey = idemRepo.findById(idempotencyKey).orElse(null);
        if (existingKey != null && !existingKey.isExpired()) {
            // if same request hash, we can allow returning the existing PaymentInstruction (which might be created already)
            metrics.incIdempotencyHit();
            return payRepo.findByIdempotencyKey(idempotencyKey).orElseThrow(() -> new IllegalStateException("idempotency key reserved but instruction missing"));
        }

        // reserve idempotency key
        idemRepo.save(new IdempotencyKey(idempotencyKey, reqHash, Instant.now().plus(24, ChronoUnit.HOURS)));

        // limits
        limits.validatePerTx(amountCents);
        limits.validateAndAccumulateDaily(customerNo, amountCents);

        // ensure demo accounts exist
        ensureDepositAccount(customerNo, currency);
        ensureSuspenseAccounts(currency);

        PaymentInstruction pi = new PaymentInstruction(idempotencyKey, customerNo, counterpartyCustomerNo, product, channel, amountCents, currency);
        payRepo.save(pi);

        metrics.incPaymentAuthorized(product, channel);
        return pi;
    }

    /**
     * SETTLEMENT step:
     * Applies posting rule and posts double-entry journal.
     * For demo:
     * - P2P_TRANSFER -> debit customer deposit, credit suspense
     * - P2P_SETTLEMENT -> debit suspense, credit counterparty deposit
     * - BILL_PAYMENT -> debit customer deposit, credit billpay suspense
     */
    @Transactional
    public SettlementResult settle(String paymentId) {
        PaymentInstruction pi = payRepo.findById(paymentId).orElseThrow();

        if ("SETTLED".equalsIgnoreCase(pi.getStatus())) {
            return new SettlementResult(pi.getId(), "ALREADY_SETTLED", null);
        }
        if (!"AUTHORIZED".equalsIgnoreCase(pi.getStatus())) {
            return new SettlementResult(pi.getId(), "NOT_SETTLED_STATUS=" + pi.getStatus(), null);
        }

        PostingRuleEngine.PostingRule rule = postingRules.resolve(pi);

        // ensure counterparty account exists if rule needs it
        if (rule.creditAccount().contains("ACC:") && pi.getCounterpartyCustomerNo() != null) {
            ensureDepositAccount(pi.getCounterpartyCustomerNo(), pi.getCurrency());
        }

        String journalId = ledger.postBalanced(
                rule.debitAccount(),
                rule.creditAccount(),
                pi.getCurrency(),
                pi.getAmountCents(),
                "PAYMENT",
                pi.getId(),
                rule.description()
        );

        pi.setStatus("SETTLED");
        payRepo.save(pi);

        metrics.incPaymentSettled(pi.getProduct());

        return new SettlementResult(pi.getId(), "SETTLED", journalId);
    }

    private void ensureDepositAccount(String customerNo, String currency) {
        String accNo = "ACC:" + customerNo + ":DEPOSIT";
        if (accountRepo.existsById(accNo)) return;

        Account a = new Account(accNo, customerNo, "DEPOSIT", currency);

        // Demo convenience: initial balance gives ability to test
        // In real bank: balance comes from core/accounting.
        a.credit(500_000_00L); // 500,000.00 in cents (demo)
        accountRepo.save(a);
    }

    private void ensureSuspenseAccounts(String currency) {
        createIfMissing("SUSPENSE:P2P_OUT", null, "SUSPENSE", currency);
        createIfMissing("SUSPENSE:BILLPAY", null, "SUSPENSE", currency);
    }

    private void createIfMissing(String accountNo, String customerNo, String type, String currency) {
        if (accountRepo.existsById(accountNo)) return;
        Account a = new Account(accountNo, customerNo, type, currency);
        accountRepo.save(a);
    }

    private String sha256(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] dig = md.digest(s.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(dig);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public record SettlementResult(String paymentId, String status, String journalId) {}
}
