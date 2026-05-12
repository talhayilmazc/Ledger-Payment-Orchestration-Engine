package com.bank.ledger.service;

import com.bank.ledger.domain.PaymentInstruction;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class PostingRuleEngine {

    private final ObjectMapper om = new ObjectMapper();
    private final JsonNode root;

    public PostingRuleEngine(
            ResourceLoader loader,
            @org.springframework.beans.factory.annotation.Value("${app.postingRulesPath}") String rulesPath
    ) throws Exception {
        Resource r = loader.getResource(rulesPath);
        String json = new String(r.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        this.root = om.readTree(json);
    }

    public PostingRule resolve(PaymentInstruction pi) {
        JsonNode rules = root.get("rules");
        if (rules == null || !rules.isArray()) throw new IllegalStateException("posting rules missing");

        for (JsonNode rule : rules) {
            String product = rule.path("product").asText();
            String channel = rule.path("channel").asText();

            if (product.equalsIgnoreCase(pi.getProduct()) && channel.equalsIgnoreCase(pi.getChannel())) {
                String debit = template(rule.path("debitAccount").asText(), pi);
                String credit = template(rule.path("creditAccount").asText(), pi);
                String desc = template(rule.path("description").asText(""), pi);
                return new PostingRule(product, channel, debit, credit, desc);
            }
        }

        throw new IllegalArgumentException("No posting rule for product=" + pi.getProduct() + " channel=" + pi.getChannel());
    }

    private String template(String s, PaymentInstruction pi) {
        if (s == null) return "";
        return s.replace("{{customerNo}}", nz(pi.getCustomerNo()))
                .replace("{{counterpartyCustomerNo}}", nz(pi.getCounterpartyCustomerNo()));
    }

    private String nz(String v) { return v == null ? "" : v; }

    public record PostingRule(String product, String channel, String debitAccount, String creditAccount, String description) {}
}
