package com.bank.ledger.repo;

import com.bank.ledger.domain.PaymentInstruction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentInstructionRepository extends JpaRepository<PaymentInstruction, String> {

    // Idempotency: same key must always return the same PaymentInstruction
    Optional<PaymentInstruction> findByIdempotencyKey(String idempotencyKey);

    // Used by API to list customer payment history (bank UI / ops)
    List<PaymentInstruction> findTop200ByCustomerNoOrderByCreatedAtDesc(String customerNo);
}
