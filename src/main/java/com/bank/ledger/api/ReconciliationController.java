package com.bank.ledger.api;

import com.bank.ledger.service.ReconciliationService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/v1/recon")
public class ReconciliationController {

    private final ReconciliationService svc;

    public ReconciliationController(ReconciliationService svc) { this.svc = svc; }

    @GetMapping("/payment/{paymentId}")
    public Map<String, Object> reconcilePayment(@PathVariable String paymentId) {
        return svc.reconcilePayment(paymentId);
    }
}
