package com.bank.ledger.service;

import com.bank.ledger.domain.PaymentInstruction;
import com.bank.ledger.repo.AccountRepository;
import com.bank.ledger.repo.IdempotencyKeyRepository;
import com.bank.ledger.repo.PaymentInstructionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentService Unit Tests")
class PaymentServiceTest {

    @Mock
    private PaymentInstructionRepository payRepo;

    @Mock
    private IdempotencyKeyRepository idemRepo;

    @Mock
    private AccountRepository accountRepo;

    @Mock
    private PostingRuleEngine postingRules;

    @Mock
    private LimitService limits;

    @Mock
    private LedgerService ledger;

    @Mock
    private MetricsService metrics;

    @InjectMocks
    private PaymentService paymentService = new PaymentService(
            null, null, null, null, null, null, null, "TRY"
    );

    @Test
    @DisplayName("should return existing payment for duplicate idempotency key")
    void shouldReturnExistingPaymentForDuplicateKey() {
        PaymentInstruction existing = new PaymentInstruction(
                "IDEM-001", "CUST0001", "CUST0002",
                "P2P_TRANSFER", "MOBILE", 100_000L, "TRY"
        );

        when(payRepo.findByIdempotencyKey("IDEM-001")).thenReturn(Optional.of(existing));

        PaymentInstruction result = paymentService.authorize(
                "IDEM-001", "CUST0001", "CUST0002",
                "P2P_TRANSFER", "MOBILE", 100_000L, "TRY"
        );

        assertThat(result.getIdempotencyKey()).isEqualTo("IDEM-001");
        assertThat(result.getAmountCents()).isEqualTo(100_000L);
        verify(payRepo, never()).save(any());
    }

    @Test
    @DisplayName("should create new payment for new idempotency key")
    void shouldCreateNewPaymentForNewKey() {
        PaymentInstruction saved = new PaymentInstruction(
                "IDEM-002", "CUST0001", "CUST0002",
                "P2P_TRANSFER", "MOBILE", 50_000L, "TRY"
        );

        when(payRepo.findByIdempotencyKey("IDEM-002")).thenReturn(Optional.empty());
        when(idemRepo.findById("IDEM-002")).thenReturn(Optional.empty());
        when(idemRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(accountRepo.existsById(anyString())).thenReturn(true);
        when(payRepo.save(any())).thenReturn(saved);
        doNothing().when(limits).validatePerTx(anyLong());
        doNothing().when(limits).validateAndAccumulateDaily(anyString(), anyLong());

        PaymentInstruction result = paymentService.authorize(
                "IDEM-002", "CUST0001", "CUST0002",
                "P2P_TRANSFER", "MOBILE", 50_000L, "TRY"
        );

        assertThat(result).isNotNull();
        assertThat(result.getCustomerNo()).isEqualTo("CUST0001");
        verify(payRepo, times(1)).save(any());
    }

    @Test
    @DisplayName("should have correct amount in payment instruction")
    void shouldHaveCorrectAmountInPaymentInstruction() {
        PaymentInstruction pi = new PaymentInstruction(
                "IDEM-003", "CUST0001", "CUST0002",
                "P2P_TRANSFER", "MOBILE", 250_000L, "TRY"
        );

        assertThat(pi.getAmountCents()).isEqualTo(250_000L);
        assertThat(pi.getCurrency()).isEqualTo("TRY");
        assertThat(pi.getStatus()).isEqualTo("AUTHORIZED");
    }
}