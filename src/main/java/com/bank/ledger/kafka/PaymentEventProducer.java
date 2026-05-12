package com.bank.ledger.kafka;

import com.bank.ledger.config.KafkaConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.kafka.enabled", havingValue = "true", matchIfMissing = false)
public class PaymentEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void publishPaymentAuthorized(String paymentId, String customerNo, Long amountCents) {
        publishEvent(KafkaConfig.PAYMENT_EVENTS_TOPIC, paymentId, Map.of(
                "eventType", "PAYMENT_AUTHORIZED",
                "paymentId", paymentId,
                "customerNo", customerNo,
                "amountCents", amountCents,
                "timestamp", Instant.now().toString()
        ));
    }

    public void publishPaymentSettled(String paymentId, String customerNo, Long amountCents) {
        publishEvent(KafkaConfig.LEDGER_EVENTS_TOPIC, paymentId, Map.of(
                "eventType", "PAYMENT_SETTLED",
                "paymentId", paymentId,
                "customerNo", customerNo,
                "amountCents", amountCents,
                "timestamp", Instant.now().toString()
        ));
    }

    public void publishPaymentFailed(String paymentId, String reason) {
        publishEvent(KafkaConfig.PAYMENT_FAILED_TOPIC, paymentId, Map.of(
                "eventType", "PAYMENT_FAILED",
                "paymentId", paymentId,
                "reason", reason,
                "timestamp", Instant.now().toString()
        ));
    }

    private void publishEvent(String topic, String key, Map<String, Object> payload) {
        try {
            String message = objectMapper.writeValueAsString(payload);
            kafkaTemplate.send(topic, key, message);
            log.info("Published event to topic={} key={}", topic, key);
        } catch (Exception e) {
            log.error("Failed to publish event to topic={} key={} error={}", topic, key, e.getMessage());
        }
    }
}