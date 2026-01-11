package com.bank.ledger.api;

import com.bank.ledger.domain.PaymentInstruction;
import com.bank.ledger.repo.PaymentInstructionRepository;
import com.bank.ledger.service.PaymentService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/payments")
public class PaymentController {

    private final PaymentService svc;
    private final PaymentInstructionRepository repo;

    public PaymentController(PaymentService svc, PaymentInstructionRepository repo) {
        this.svc = svc;
        this.repo = repo;
    }

    public record AuthorizeRequest(
            @NotBlank String idempotencyKey,
            @NotBlank String customerNo,
            String counterpartyCustomerNo,
            @NotBlank String product,
            @NotBlank String channel,
            @Positive long amountCents,
            String currency
    ) {}

    @PostMapping("/authorize")
    public PaymentInstruction authorize(@RequestBody AuthorizeRequest req) {
        return svc.authorize(
                req.idempotencyKey(),
                req.customerNo(),
                req.counterpartyCustomerNo(),
                req.product(),
                req.channel(),
                req.amountCents(),
                req.currency()
        );
    }

    @PostMapping("/{paymentId}/settle")
    public PaymentService.SettlementResult settle(@PathVariable String paymentId) {
        return svc.settle(paymentId);
    }

    @GetMapping("/{paymentId}")
    public PaymentInstruction get(@PathVariable String paymentId) {
        return repo.findById(paymentId).orElseThrow();
    }

    @GetMapping
    public List<PaymentInstruction> listByCustomer(@RequestParam String customerNo) {
        return repo.findTop200ByCustomerNoOrderByCreatedAtDesc(customerNo);
    }
}
