# Ledger — Payment Orchestration Engine

> Bank-grade double-entry accounting, idempotent payment processing, and financial reconciliation engine.
> Java 17 + Spring Boot 3 + PostgreSQL + Kafka + Redis + Docker + CI/CD

![Java](https://img.shields.io/badge/Java-17-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3-green?logo=springboot)
![Kafka](https://img.shields.io/badge/Apache%20Kafka-3.7-black?logo=apachekafka)
![Redis](https://img.shields.io/badge/Redis-7-red?logo=redis)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?logo=postgresql)
![Docker](https://img.shields.io/badge/Docker-Compose-blue?logo=docker)
![CI](https://github.com/talhayilmazc/Ledger-Payment-Orchestration-Engine/actions/workflows/ci.yml/badge.svg)

---

## 🏦 Why This Project Matters

In real banks, money is **never** updated with `UPDATE balance = balance - X`.

Instead, banks use:
- Double-entry accounting
- Immutable ledger entries
- JSON-based posting rules
- Idempotent payment processing
- Reconciliation and auditability

**This project implements all of them** — and adds Kafka event streaming, Redis caching, and production-grade observability on top.

---

## 🧠 What This System Does

A realistic core-banking style payment flow:

**1. Authorize Payment**
- Idempotency check (safe retries — no duplicate debits)
- Per-transaction and daily limits
- Customer & channel aware
- Publishes `PAYMENT_AUTHORIZED` event to Kafka

**2. Settle Payment**
- Resolves JSON-based posting rules by product + channel
- Creates double-entry ledger postings atomically
- Updates balances transactionally
- Publishes `PAYMENT_SETTLED` event to Kafka

**3. Ledger**
- Every movement creates exactly: 1 DEBIT + 1 CREDIT
- Same amount, same currency, same journal ID
- Immutable — entries are never modified

**4. Reconciliation**
- Verifies payments match ledger postings
- Checks: Debit = Credit = Payment amount
- Ensures financial correctness

---

## 🏗️ Architecture
Client / Channel
│
▼
Payment API (REST)
│
├──► Idempotency Check (Redis cache)
│
├──► Limit Validation (per-tx + daily)
│
▼
PaymentInstruction (PostgreSQL)
│
▼
PostingRuleEngine (JSON rules)
│
▼
LedgerService (Double-Entry)
│
├──► LedgerEntry x2 (DEBIT + CREDIT) → PostgreSQL
│
└──► Kafka Events → payment-events / ledger-events / payment-failed

---

## 🛡️ Banking-Grade Features

| Feature | Why Banks Need It |
|---|---|
| Double-entry ledger | Prevents money creation/destruction |
| Idempotency keys | Prevents duplicate debits |
| JSON posting rules | Accounting changes without code deploy |
| Suspense accounts | How real banks handle pending settlements |
| Daily limits | Regulatory & risk control |
| Reconciliation | Audit & financial integrity |
| Immutable ledger | Compliance & dispute handling |
| Kafka event streaming | Async downstream integration |
| Redis caching | High-performance idempotency lookups |
| Prometheus metrics | Production observability |

---

## ⚙️ Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.3 |
| Database | PostgreSQL 16 + Spring Data JPA |
| Event Streaming | Apache Kafka — 3 topics (payment-events, ledger-events, payment-failed) |
| Caching | Redis 7 |
| API Docs | SpringDoc OpenAPI / Swagger UI |
| Observability | Micrometer + Prometheus + Actuator |
| Build | Gradle 8.10 |
| DevOps | Docker, Docker Compose, GitHub Actions CI/CD |
| Testing | JUnit 5, Mockito — 9 unit tests |

---

## 🚀 Running Locally

### Prerequisites
- Docker Desktop
- Java 17

### Start all services

```bash
docker compose up -d --build
```

This starts:
- **App** → http://localhost:9300
- **PostgreSQL** → localhost:5433
- **Redis** → localhost:6382
- **Kafka** → localhost:9094
- **Kafka UI** → http://localhost:8092

### Endpoints
Swagger UI  → http://localhost:9300/swagger-ui/index.html
Health      → http://localhost:9300/actuator/health
Metrics     → http://localhost:9300/actuator/prometheus

---

## 💳 Example Payment Flow

**1️⃣ Authorize payment**
```bash
POST /v1/payments/authorize
{
  "idempotencyKey": "IDEM-001",
  "customerNo": "CUST0001",
  "counterpartyCustomerNo": "CUST0002",
  "product": "P2P_TRANSFER",
  "channel": "MOBILE",
  "amountCents": 1500000,
  "currency": "TRY"
}
```

**2️⃣ Settle**
```bash
POST /v1/payments/{paymentId}/settle
```

This creates in the ledger:
- `DEBIT: ACC:CUST0001:DEPOSIT — 15,000.00 TRY`
- `CREDIT: SUSPENSE:P2P_OUT — 15,000.00 TRY`

Both entries share the same `journalId` and are immutable.

**3️⃣ Reconcile**
```bash
GET /v1/recon/payment/{paymentId}
```

Checks: exactly 2 ledger entries exist, Debit = Credit = Payment amount.

---

## 📊 Kafka Topics

| Topic | Event Types |
|---|---|
| `payment-events` | PAYMENT_AUTHORIZED |
| `ledger-events` | PAYMENT_SETTLED |
| `payment-failed` | PAYMENT_FAILED |

---

## 📁 Project Structure
src/main/java/com/bank/ledger/
├── api/          # PaymentController, AccountController, ReconciliationController
├── config/       # KafkaConfig, RedisConfig, OpenApiConfig
├── domain/       # PaymentInstruction, LedgerEntry, Account, IdempotencyKey
├── kafka/        # PaymentEventProducer
├── repo/         # JPA Repositories
└── service/      # PaymentService, LedgerService, PostingRuleEngine, LimitService, ReconciliationService

---

## 🧪 Testing

```bash
./gradlew test
```

- ✅ LedgerServiceTest — 5 unit tests
- ✅ PaymentServiceTest — 3 unit tests
- ✅ LedgerApplicationTests — context loads

---

## 🔄 CI/CD Pipeline

GitHub Actions on every push:

1. **Build & Test** — `./gradlew clean build` + `./gradlew test`
2. **Docker Build** — builds image on `main` and `develop`

---

## 🧩 Why Recruiters Care

This project proves understanding of:
- Core banking accounting principles
- Transaction safety and atomicity
- Financial correctness and idempotency
- Real-world payment flows
- Audit & compliance thinking
- Event-driven architecture

The same foundation used by payment gateways, core banking systems, fintechs, and treasury platforms.

---

## 👨‍💻 Author

**Talha Yılmaz**
[github.com/talhayilmazc](https://github.com/talhayilmazc) · [linkedin.com/in/talha-yilmaz-38a13a225](https://linkedin.com/in/talha-yilmaz-38a13a225)