package com.bank.ledger.service;

import com.bank.ledger.domain.CustomerDailyAggregate;
import com.bank.ledger.repo.CustomerDailyAggregateRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class LimitService {

    private final CustomerDailyAggregateRepository aggRepo;
    private final MetricsService metrics;

    private final long dailyLimitCents;
    private final long perTxLimitCents;

    public LimitService(CustomerDailyAggregateRepository aggRepo,
                        MetricsService metrics,
                        @Value("${app.limits.dailyAmountCents}") long dailyLimitCents,
                        @Value("${app.limits.perTxAmountCents}") long perTxLimitCents) {
        this.aggRepo = aggRepo;
        this.metrics = metrics;
        this.dailyLimitCents = dailyLimitCents;
        this.perTxLimitCents = perTxLimitCents;
    }

    public void validatePerTx(long amountCents) {
        if (amountCents > perTxLimitCents) {
            metrics.incLimitReject("PER_TX");
            throw new IllegalArgumentException("Per-transaction limit exceeded");
        }
    }

    @Transactional
    public void validateAndAccumulateDaily(String customerNo, long amountCents) {
        LocalDate today = LocalDate.now();
        CustomerDailyAggregate agg = aggRepo.findByCustomerNoAndDay(customerNo, today)
                .orElseGet(() -> new CustomerDailyAggregate(customerNo, today, 0));

        long newTotal = agg.getTotalAmountCents() + amountCents;
        if (newTotal > dailyLimitCents) {
            metrics.incLimitReject("DAILY");
            throw new IllegalArgumentException("Daily limit exceeded");
        }

        agg.add(amountCents);
        aggRepo.save(agg);
    }
}
