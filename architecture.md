# ScamShield — Hackathon Architecture

## 1. Overview

ScamShield is a USSD-based mobile-money fraud verification and reporting platform designed to reduce the cognitive burden on users when dealing with suspected impersonation and social-engineering scams.

The hackathon architecture uses an **evolvable modular monolith**:

- One Spring Boot application for the MVP
- Clear domain-oriented modules
- PostgreSQL as the primary database
- REST APIs between the UI/gateway and backend
- Internal application events for loose coupling
- Mock provider integrations for the hackathon
- A web-based USSD simulator instead of depending on a live telecom USSD gateway

The architecture is intentionally simple enough to finish during the hackathon while preserving boundaries that can later be extracted into independent services.

---

## 2. Architectural Goals

### Primary goals

1. Build a working end-to-end MVP during the hackathon.
2. Demonstrate the security concept rather than infrastructure complexity.
3. Keep the system easy for a beginner/intermediate development team to understand.
4. Maintain clear boundaries between fraud reporting, verification, risk scoring and incident management.
5. Make future telecom-provider integrations possible without rewriting the core system.
6. Treat privacy, authorization, auditability and abuse prevention as first-class concerns.

### Non-goals for the hackathon

The MVP will **not** attempt to:

- integrate directly with real Orange, Mascom or BTC internal systems;
- automatically block telephone numbers;
- prove that a caller is the real owner of a phone number;
- replace law-enforcement investigation;
- deploy Kubernetes or a large microservice infrastructure;
- build a production-grade machine-learning fraud detector.

---

## 3. High-Level Architecture

```text
                         ┌──────────────────────┐
                         │        USERS         │
                         │                      │
                         │ Feature Phone        │
                         │ Smartphone           │
                         └──────────┬───────────┘
                                    │
                            USSD / Web Simulator
                                    │
                                    ▼
                         ┌──────────────────────┐
                         │    API / CONTROLLER  │
                         │                      │
                         │ Validation           │
                         │ Rate limiting        │
                         │ Request handling     │
                         └──────────┬───────────┘
                                    │
                                    ▼
              ┌───────────────────────────────────────────┐
              │              SCAMSHIELD CORE              │
              │             Spring Boot MVP               │
              │                                           │
              │  ┌──────────────┐  ┌───────────────────┐ │
              │  │ Verification │  │ Reporting         │ │
              │  └──────────────┘  └───────────────────┘ │
              │                                           │
              │  ┌──────────────┐  ┌───────────────────┐ │
              │  │ Risk Engine  │  │ Incident          │ │
              │  └──────────────┘  └───────────────────┘ │
              │                                           │
              │  ┌──────────────┐  ┌───────────────────┐ │
              │  │ Provider     │  │ Admin /           │ │
              │  │ Registry     │  │ Escalation        │ │
              │  └──────────────┘  └───────────────────┘ │
              └───────────────────────┬───────────────────┘
                                      │
                            ┌─────────┴─────────┐
                            │                   │
                            ▼                   ▼
                     ┌─────────────┐    ┌──────────────┐
                     │ PostgreSQL  │    │ App Events   │
                     │             │    │              │
                     │ Reports     │    │ ReportCreated│
                     │ Numbers     │    │ RiskUpdated  │
                     │ Incidents   │    │ IncidentOpen │
                     │ Providers   │    └──────────────┘
                     └─────────────┘
```

---

## 4. Why a Modular Monolith?

The project should **not** begin as a microservice system.

Microservices would introduce operational complexity that does not directly contribute to the hackathon's core value.

The modular monolith gives us:

- one codebase;
- one deployment;
- one database;
- easier debugging;
- faster development;
- clear domain boundaries;
- a straightforward path to future service extraction.

The important architectural rule is:

> Modules communicate through well-defined application interfaces/events rather than directly depending on each other's internal implementation.

---

## 5. Domain Modules

### 5.1 Verification Module

**Responsibility:** Determine whether a phone number is known, authorised or associated with suspicious activity.

Example:

```text
User enters:
+267 XXXXXXXX

        ↓

Verification Module

        ↓

Risk + Provider Registry

        ↓

HIGH RISK
```

Responsibilities:

- caller lookup;
- provider lookup;
- official-number verification;
- risk-status retrieval;
- user-friendly verification response.

---

### 5.2 Reporting Module

**Responsibility:** Accept and store user fraud reports.

Example report:

```json
{
  "phoneNumber": "+267XXXXXXXX",
  "claimedProvider": "ORANGE",
  "attackType": "OTP_REQUEST"
}
```

Responsibilities:

- validate reports;
- store reports;
- prevent obvious abuse;
- publish `ReportCreated` events.

---

### 5.3 Risk Engine

**Responsibility:** Convert reports and other signals into an explainable risk score.

For the MVP, use a deterministic rules engine.

Example:

```text
Report received                  +10
Multiple independent reports     +20
Provider impersonation           +20
PIN requested                    +25
OTP requested                    +25
Money requested                  +25
Previously confirmed incident    +40
```

Example result:

```text
Score: 85

Risk Level: CRITICAL
```

The system should explain why a number received its score.

---

### 5.4 Incident Module

A report is not automatically an incident.

Multiple related reports can be grouped into an incident.

```text
Report
  ↓
Related reports
  ↓
Incident
  ↓
Investigation
  ↓
Escalation
```

Responsibilities:

- incident creation;
- incident status;
- severity;
- investigation metadata;
- escalation status.

---

### 5.5 Provider Registry Module

Stores trusted provider information.

Example:

```text
Provider: Orange
Official numbers:
- +267 XXXXXXXX
- +267 XXXXXXXX

Provider: Mascom
Official numbers:
- +267 XXXXXXXX

Provider: BTC
Official numbers:
- +267 XXXXXXXX
```

For the hackathon, these values are **mock/seeded data**.

Future production versions can replace this with provider integrations.

---

### 5.6 Admin / Escalation Module

Provides the security operations dashboard.

Security staff can:

- view incidents;
- inspect reports;
- view risk scores;
- identify attack patterns;
- mark incidents as investigated;
- simulate provider escalation.

For the hackathon, escalation should be demonstrated using a mock provider endpoint or status update.

---

## 6. Internal Event Flow

The MVP can use Spring application events.

Example:

```text
User submits report
        │
        ▼
Reporting Module
        │
        ▼
ReportCreatedEvent
        │
        ├──────────────► Risk Engine
        │
        └──────────────► Incident Module
```

Later, the same architecture can use Kafka or RabbitMQ:

```text
Reporting Service
        │
        ▼
Message Broker
   │       │       │
   ▼       ▼       ▼
 Risk   Incident  Analytics
```

The business modules therefore remain conceptually stable while the infrastructure evolves.

---

## 7. Provider Integration Architecture

Do not couple the core system directly to individual telecom APIs.

Use an adapter/interface approach.

```text
                 ScamShield
                     │
             Provider Interface
                     │
        ┌────────────┼────────────┐
        ▼            ▼            ▼
 Orange Adapter  Mascom Adapter  BTC Adapter
        │            │            │
        ▼            ▼            ▼
  Mock API       Mock API       Mock API
```

For the hackathon:

```text
OrangeAdapter → MockOrangeProvider
MascomAdapter → MockMascomProvider
BTCAdapter    → MockBTCProvider
```

For production:

```text
OrangeAdapter → Actual Provider Integration
```

The rest of ScamShield does not need to change.

---

## 8. Data Architecture

PostgreSQL is sufficient for the MVP.

Core entities:

```text
Provider
   │
   └── OfficialNumber

PhoneNumber
   │
   ├── Reports
   └── RiskScore

Report
   │
   └── Incident

Incident
   │
   └── Alert
```

Suggested tables:

```text
providers
official_numbers
phone_numbers
reports
incidents
risk_scores
alerts
audit_logs
```

---

## 9. Security Architecture

Security must exist at every layer.

```text
External Request
       │
       ▼
Rate Limiting
       │
       ▼
Input Validation
       │
       ▼
Authentication
       │
       ▼
Authorization
       │
       ▼
Business Logic
       │
       ▼
Database
       │
       └──────► Audit Log
```

### User-side security principles

ScamShield must **never request**:

- mobile-money PINs;
- OTPs;
- passwords;
- unnecessary financial information.

### Admin-side security

The dashboard should use:

- authentication;
- role-based authorization;
- protected administrative endpoints;
- audit logging.

---

## 10. Risk Scoring Must Not Automatically Block Numbers

A user report is a signal, not proof.

Therefore:

```text
User Report
     ↓
Risk Score
     ↓
Pattern Detection
     ↓
Human / Provider Verification
     ↓
Potential Action
```

Do not implement:

```text
100 reports → automatically block number
```

This could allow malicious users to weaponise the reporting system.

---

## 11. Privacy Principles

Collect only information required for fraud detection and incident investigation.

Avoid collecting:

- PINs;
- OTPs;
- unnecessary identity information;
- unnecessary transaction details.

Recommended report fields:

```text
Caller Number
Claimed Provider
Attack Type
Timestamp
Optional Description
```

Administrative access should be restricted and auditable.

---

## 12. Hackathon Deployment

### Simplest deployment

```text
                    Internet
                       │
                       ▼
                ┌─────────────┐
                │ Spring Boot │
                │ Application │
                └──────┬──────┘
                       │
                       ▼
                 ┌───────────┐
                 │ PostgreSQL│
                 └───────────┘
```

The frontend can be served separately or from the same application.

### Docker option

```text
Docker
│
├── scamshield-api
│
└── postgres
```

This is sufficient for a hackathon.

---

## 13. Production Evolution

The architecture can evolve incrementally.

### Stage 1 — Hackathon

```text
Modular Spring Boot
+
PostgreSQL
+
USSD Simulator
+
Admin Dashboard
```

### Stage 2 — Pilot

```text
Containerised Spring Boot
+
PostgreSQL
+
Real USSD Gateway
+
Authentication
+
Monitoring
+
Provider Integration
```

### Stage 3 — Scale

Extract only components that actually need independent scaling.

Possible services:

```text
Verification Service
Reporting Service
Risk Intelligence Service
Incident Service
Notification Service
Provider Integration Service
Analytics Service
```

Use a message broker for high-volume events.

---

## 14. Future Production Architecture

```text
                       Users
                         │
                         ▼
                  API Gateway
                         │
          ┌──────────────┼──────────────┐
          ▼              ▼              ▼
    Verification     Reporting     Authentication
       Service         Service         Service
          │              │
          │              ▼
          │        Message Broker
          │        ┌──────┼──────┐
          │        ▼      ▼      ▼
          │       Risk  Incident Analytics
          │      Engine Service
          │
          └──────────────────────────┐
                                     ▼
                              Provider Adapters
                               │      │      │
                               ▼      ▼      ▼
                             Orange Mascom  BTC
```

Only move to this architecture when actual scale, organisational boundaries or operational requirements justify it.

---

## 15. Hackathon Architecture Principle

> **Build the smallest architecture that demonstrates the complete security workflow, but establish boundaries that allow the system to evolve without rewriting the domain.**

The hackathon judges should see:

**User → Verification → Report → Risk Score → Incident → Security Dashboard → Escalation**

That complete vertical slice is more valuable than having ten technically impressive services that do not form a working system.
