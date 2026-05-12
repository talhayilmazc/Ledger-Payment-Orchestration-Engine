package com.bank.ledger.service;

import com.bank.ledger.domain.Account;
import com.bank.ledger.domain.LedgerEntry;
import com.bank.ledger.repo.AccountRepository;
import com.bank.ledger.repo.LedgerEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LedgerService Unit Tests")
class LedgerServiceTest {

    @Mock
    private AccountRepository accountRepo;

    @Mock
    private LedgerEntryRepository ledgerRepo;

    @Mock
    private MetricsService metrics;

    @InjectMocks
    private LedgerService ledgerService;

    private Account debitAccount;
    private Account creditAccount;

    @BeforeEach
    void setUp() {
        debitAccount = new Account("ACC:CUST0001:DEPOSIT", "CUST0001", "DEPOSIT", "TRY");
        debitAccount.credit(1_000_000L); // give it balance

        creditAccount = new Account("SUSPENSE:P2P_OUT", null, "SUSPENSE", "TRY");
    }

    @Test
    @DisplayName("should post balanced double-entry journal successfully")
    void shouldPostBalancedJournal() {
        when(accountRepo.findById("ACC:CUST0001:DEPOSIT")).thenReturn(Optional.of(debitAccount));
        when(accountRepo.findById("SUSPENSE:P2P_OUT")).thenReturn(Optional.of(creditAccount));
        when(ledgerRepo.save(any(LedgerEntry.class))).thenAnswer(inv -> inv.getArgument(0));
        when(accountRepo.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        String journalId = ledgerService.postBalanced(
                "ACC:CUST0001:DEPOSIT",
                "SUSPENSE:P2P_OUT",
                "TRY",
                100_000L,
                "PAYMENT",
                "PAY-001",
                "P2P Transfer"
        );

        assertThat(journalId).isNotNull();
        verify(ledgerRepo, times(2)).save(any(LedgerEntry.class));
        verify(accountRepo, times(2)).save(any(Account.class));
    }

    @Test
    @DisplayName("should throw exception when amount is zero or negative")
    void shouldThrowWhenAmountIsZero() {
        assertThatThrownBy(() ->
                ledgerService.postBalanced(
                        "ACC:CUST0001:DEPOSIT",
                        "SUSPENSE:P2P_OUT",
                        "TRY", 0L,
                        "PAYMENT", "PAY-001", "test"
                )
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("amount must be > 0");
    }

    @Test
    @DisplayName("should throw exception when debit and credit accounts are same")
    void shouldThrowWhenSameAccount() {
        assertThatThrownBy(() ->
                ledgerService.postBalanced(
                        "ACC:CUST0001:DEPOSIT",
                        "ACC:CUST0001:DEPOSIT",
                        "TRY", 100_000L,
                        "PAYMENT", "PAY-001", "test"
                )
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("debit and credit cannot be same");
    }

    @Test
    @DisplayName("should throw exception when insufficient funds")
    void shouldThrowWhenInsufficientFunds() {
        Account poorAccount = new Account("ACC:POOR:DEPOSIT", "POOR", "DEPOSIT", "TRY");
        // no balance

        when(accountRepo.findById("ACC:POOR:DEPOSIT")).thenReturn(Optional.of(poorAccount));
        when(accountRepo.findById("SUSPENSE:P2P_OUT")).thenReturn(Optional.of(creditAccount));

        assertThatThrownBy(() ->
                ledgerService.postBalanced(
                        "ACC:POOR:DEPOSIT",
                        "SUSPENSE:P2P_OUT",
                        "TRY", 100_000L,
                        "PAYMENT", "PAY-001", "test"
                )
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("insufficient funds");
    }

    @Test
    @DisplayName("should throw exception when currency mismatch")
    void shouldThrowWhenCurrencyMismatch() {
        Account usdAccount = new Account("ACC:CUST0001:DEPOSIT", "CUST0001", "DEPOSIT", "USD");
        usdAccount.credit(1_000_000L);

        when(accountRepo.findById("ACC:CUST0001:DEPOSIT")).thenReturn(Optional.of(usdAccount));
        when(accountRepo.findById("SUSPENSE:P2P_OUT")).thenReturn(Optional.of(creditAccount));

        assertThatThrownBy(() ->
                ledgerService.postBalanced(
                        "ACC:CUST0001:DEPOSIT",
                        "SUSPENSE:P2P_OUT",
                        "TRY", 100_000L,
                        "PAYMENT", "PAY-001", "test"
                )
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("currency mismatch");
    }
}