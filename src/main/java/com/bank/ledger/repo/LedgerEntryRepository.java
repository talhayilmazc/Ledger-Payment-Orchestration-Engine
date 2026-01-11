package com.bank.ledger.repo;

import com.bank.ledger.domain.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, Long> {
    List<LedgerEntry> findTop500ByJournalIdOrderByCreatedAtAsc(String journalId);
    List<LedgerEntry> findTop500ByAccountNoOrderByCreatedAtDesc(String accountNo);
    List<LedgerEntry> findTop500ByReferenceTypeAndReferenceIdOrderByCreatedAtAsc(String referenceType, String referenceId);
}
