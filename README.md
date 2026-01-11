# Ledger & Payment Orchestration Engine  
**A Bank-Grade Core Banking Component**

This project is a **production-style core banking engine** that demonstrates how modern banks build **payments, ledgers, and financial correctness** systems.

It is not a CRUD demo — it is a **financially correct, audit-ready, idempotent, double-entry accounting engine** designed to resemble what actually runs inside banks.

---

## 🏦 Why this project matters

In real banks, **money is not updated with UPDATE balance = balance - X**.

Instead, banks use:
- **Double-entry accounting**
- **Immutable ledgers**
- **Posting rules**
- **Idempotent payment processing**
- **Reconciliation and auditability**

This project implements all of them.

---

## 🧠 What this system does

It provides a **realistic core-banking style payment flow**:

1. **Authorize payment**  
   - Idempotent (safe retries)
   - Per-transaction and daily limits
   - Customer & channel aware

2. **Settle payment**
   - Uses **JSON-based posting rules**
   - Creates **double-entry ledger postings**
   - Updates balances atomically

3. **Ledger**
   - Every movement creates:
     - 1 DEBIT
     - 1 CREDIT
   - Same amount, same currency
   - Same journal ID

4. **Reconciliation**
   - Verifies that payments match ledger postings
   - Ensures financial correctness

---

## 🧱 Architecture

Client / Channel
|
v
Payment API ───► PaymentInstruction (idempotent)
|
v
Posting Rules Engine (JSON)
|
v
Ledger Service (Double-Entry)
|
v
PostgreSQL Ledger

yaml
Kodu kopyala

---

## 🛡️ Banking-grade features

| Feature | Why banks need it |
|------|----------------|
| Double-entry ledger | Prevents money creation/destruction |
| Idempotency keys | Prevents duplicate debits |
| Posting rules | Allows accounting to change without code |
| Suspense accounts | How real banks handle pending settlements |
| Limits | Regulatory & risk control |
| Reconciliation | Audit & financial integrity |
| Immutable ledger | Compliance & dispute handling |
| Prometheus metrics | Production observability |

---

## ⚙️ Tech Stack

- **Java 17**
- **Spring Boot 3**
- **PostgreSQL**
- **Docker & Docker Compose**
- **Spring Data JPA**
- **Prometheus / Actuator**

---

## 🚀 Running locally

```bash
docker compose up -d --build
Check:

Health → http://localhost:9300/actuator/health

Metrics → http://localhost:9300/actuator/prometheus

💳 Example Flow
1️⃣ Authorize payment
bash
Kodu kopyala
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
2️⃣ Settle
bash
Kodu kopyala
POST /v1/payments/{paymentId}/settle
This creates:

Debit: ACC:CUST0001:DEPOSIT

Credit: SUSPENSE:P2P_OUT

Both recorded in the ledger.

🔍 Reconciliation
bash
Kodu kopyala
GET /v1/recon/payment/{paymentId}
Checks:

Exactly 2 ledger entries exist

Debit = Credit

Amount matches the payment

🧩 Why recruiters care
This project proves I understand:

Core banking accounting

Transaction safety

Financial correctness

Idempotency

Real-world payment flows

Audit & compliance thinking

This is the same foundation used by:

Payment gateways

Core banking systems

Fintechs

Treasury platforms

AML & Fraud systems

📈 Production extensions (not implemented but designed for)
Maker-checker approvals

Reversals & chargebacks

FX legs

Fees & commissions

End-of-day batch posting

GL exports

External settlement file ingestion

🏁 Summary
This is a bank-grade payment and ledger engine, not a toy project.

It demonstrates how money should be handled correctly, safely, and auditable in a real financial institution.