# ScamShield — Hackathon Development Plan

## 1. Development Objective

Build a functional MVP that demonstrates how ScamShield can reduce the cognitive burden of mobile-money scam detection and create a shared fraud-intelligence workflow.

The MVP must demonstrate one complete journey:

```text
Suspicious Call
      ↓
USSD Verification
      ↓
Caller Number Lookup
      ↓
Risk Assessment
      ↓
User Report
      ↓
Risk Score Update
      ↓
Incident Creation
      ↓
Security Dashboard
      ↓
Mock Provider Escalation
```

The priority is **a complete, convincing vertical slice**, not maximum feature count.

---

# 2. MVP Definition

## Must Have

### User / USSD

- USSD simulator
- Main menu
- Verify caller
- Report scam
- Emergency fraud flow
- Simple security response

### Backend

- Spring Boot REST API
- Phone-number lookup
- Provider registry
- Report creation
- Risk scoring
- Incident creation
- Alert/escalation

### Database

- PostgreSQL
- Seeded provider data
- Seeded official numbers
- Reports
- Risk scores
- Incidents

### Admin

- Login/authentication
- Dashboard
- Suspicious numbers
- Risk scores
- Recent reports
- Incidents
- Mock escalation

---

# 3. Nice to Have

Only implement these after the core MVP works:

- report history;
- charts;
- attack-pattern grouping;
- SMS scam reporting;
- provider-specific dashboards;
- more sophisticated risk scoring;
- notification simulation;
- audit log viewer.

---

# 4. Explicitly Out of Scope

Do not spend hackathon time on:

- real law-enforcement integration;
- real telecom internal APIs unless access is provided;
- machine learning;
- automatic number blocking;
- mobile applications;
- Kubernetes;
- complex distributed infrastructure;
- production-scale identity verification.

---

# 5. Suggested Team Roles

If the team has 3–5 people:

## Backend Developer

Responsible for:

- Spring Boot;
- REST APIs;
- business logic;
- risk engine;
- database integration.

## Frontend / Dashboard Developer

Responsible for:

- admin dashboard;
- USSD simulator;
- visualisation;
- UX.

## Database / Security Developer

Responsible for:

- PostgreSQL schema;
- authentication;
- authorization;
- audit logging;
- validation.

## Integration / Demo Developer

Responsible for:

- mock provider APIs;
- USSD flow;
- end-to-end integration;
- demo scenario.

## Pitch / Product Lead

Responsible for:

- problem validation;
- user journey;
- presentation;
- documentation;
- judging criteria.

For a smaller team, combine these roles.

---

# 6. Development Strategy

Use **vertical slices** rather than building every frontend component first and every backend component later.

Bad approach:

```text
Build entire frontend
        ↓
Build entire backend
        ↓
Build database
        ↓
Try to connect everything
```

Recommended approach:

```text
Feature 1:
USSD → API → DB → Response

Feature 2:
Report → API → DB → Risk

Feature 3:
Risk → Incident → Dashboard

Feature 4:
Incident → Escalation
```

This ensures there is always a working system.

---

# 7. Phase 0 — Project Setup

### Tasks

- Create GitHub repository.
- Define branch strategy.
- Create Spring Boot project.
- Configure PostgreSQL.
- Configure environment variables.
- Create basic frontend.
- Create README.
- Create architecture documentation.
- Set up `.gitignore`.
- Create initial CI workflow if time permits.

### Suggested stack

```text
Backend:
Java
Spring Boot
Spring Data JPA
Spring Security

Database:
PostgreSQL

Frontend:
HTML
CSS
JavaScript
(or the team's preferred simple frontend framework)

Testing:
JUnit
Spring Boot Test

Deployment:
Docker
```

### Definition of Done

```text
Application starts successfully
        +
Database connects
        +
One API endpoint returns successfully
```

---

# 8. Phase 1 — Provider Registry

Build the trusted-number foundation first.

### Entities

```text
Provider
OfficialNumber
```

### Example

```text
Orange
 ├── +267XXXXXXXX
 └── +267XXXXXXXX

Mascom
 └── +267XXXXXXXX

BTC
 └── +267XXXXXXXX
```

### API

```http
GET /api/providers
GET /api/providers/{id}/official-numbers
GET /api/verification/{phoneNumber}
```

### Definition of Done

Entering an authorised number produces:

```text
VERIFIED PROVIDER NUMBER
```

Entering an unknown number produces:

```text
UNKNOWN NUMBER
```

---

# 9. Phase 2 — Risk Engine

Implement the simplest explainable rules engine.

### Example

```text
Initial score = 0

Report exists:
+10

Provider impersonation:
+20

OTP requested:
+25

PIN requested:
+25

Money requested:
+25

Multiple independent reports:
+20
```

### Risk levels

```text
0–29       LOW
30–59      SUSPICIOUS
60–79      HIGH
80–100     CRITICAL
```

### Important

Store **why** points were added.

Example:

```json
{
  "score": 85,
  "level": "CRITICAL",
  "reasons": [
    "Multiple independent reports",
    "Provider impersonation",
    "OTP request"
  ]
}
```

This makes the system explainable.

---

# 10. Phase 3 — Reporting

Build the report flow.

### USSD flow

```text
SCAMSHIELD

1. Verify caller
2. Report scam
3. Emergency fraud
4. Security advice
```

Report flow:

```text
2. Report scam

Enter number:
+267XXXXXXXX

Which provider did they claim to represent?

1. Orange
2. Mascom
3. BTC
4. Other

What happened?

1. Asked for PIN
2. Asked for OTP
3. Asked for money
4. Other
```

The report is stored.

---

# 11. Phase 4 — Event-Driven Processing

When a report is created:

```text
ReportCreated
      │
      ├────────► Risk Engine
      │
      └────────► Incident Module
```

For the hackathon, implement this with Spring application events.

Do not introduce Kafka unless there is a strong reason.

### Definition of Done

A new report automatically:

1. saves to the database;
2. recalculates the risk score;
3. updates the phone-number status;
4. creates or updates an incident when appropriate.

---

# 12. Phase 5 — Incident Management

Create the distinction between:

**Report**

and

**Incident**

Example:

```text
Report #1
+267XXXXXXX
OTP request

Report #2
+267XXXXXXX
OTP request

Report #3
+267XXXXXXX
Provider impersonation
```

System:

```text
INCIDENT #SC-001

Potential coordinated scam
Risk: HIGH
Reports: 3
```

### Incident statuses

```text
OPEN
UNDER_INVESTIGATION
ESCALATED
RESOLVED
FALSE_POSITIVE
```

---

# 13. Phase 6 — Security Dashboard

The dashboard should answer:

> **"What is happening right now?"**

### Dashboard metrics

```text
Active Incidents
Reports Today
High-Risk Numbers
Critical Numbers
Provider Impersonation Reports
```

### Table

```text
Phone       Risk       Reports    Claimed Provider
---------------------------------------------------
+267XXX     CRITICAL      14      Orange
+267XXX     HIGH           8      Mascom
+267XXX     HIGH           6      BTC
```

Clicking a number should show:

```text
Risk score
Risk reasons
Report count
Attack types
Claimed providers
Incident history
```

---

# 14. Phase 7 — Escalation

For the hackathon, simulate the provider connection.

Example:

```text
INCIDENT #SC-001

Risk: CRITICAL

[Escalate to Orange]
```

After clicking:

```text
ESCALATED

Destination:
Orange Security Operations

Reference:
EXT-SC-001

Status:
QUEUED
```

The backend can call a mock provider endpoint.

This demonstrates the architecture without requiring real operator access.

---

# 15. Phase 8 — Security Hardening

Before the demo, implement the highest-value controls.

### Input validation

Validate:

- phone-number format;
- report fields;
- provider IDs;
- attack types.

### Rate limiting

Prevent one user/IP from generating hundreds of reports.

### Authentication

Protect admin endpoints.

### Authorization

Example:

```text
ADMIN
ANALYST
PROVIDER
```

### Audit logging

Log:

```text
Actor
Action
Timestamp
Target
Result
```

### Sensitive data

Never store:

```text
PIN
OTP
Passwords
```

---

# 16. Testing Plan

## Unit tests

Test:

- risk scoring;
- provider verification;
- report validation;
- incident creation.

Example:

```text
Given:
5 reports
+ provider impersonation
+ OTP request

Expect:
HIGH/CRITICAL risk
```

## Integration tests

Test:

```text
POST report
      ↓
Database
      ↓
Risk engine
      ↓
Incident
```

## End-to-end demo test

Test exactly what the judges will see:

```text
USSD
 ↓
Verify
 ↓
Report
 ↓
Risk increases
 ↓
Incident appears
 ↓
Dashboard updates
 ↓
Escalation
```

---

# 17. Git Development Workflow

Use short-lived branches.

```text
main
 │
 ├── feature/provider-registry
 ├── feature/risk-engine
 ├── feature/reporting
 ├── feature/incident-management
 ├── feature/ussd-flow
 └── feature/dashboard
```

Recommended flow:

```text
Create branch
     ↓
Implement one feature
     ↓
Test
     ↓
Commit
     ↓
Push
     ↓
Pull Request
     ↓
Review
     ↓
Merge
```

Keep commits small and descriptive.

Examples:

```text
feat: add provider registry
feat: implement risk scoring
feat: add scam report endpoint
feat: add incident creation
fix: validate Botswana phone numbers
test: add risk engine tests
```

---

# 18. API Contract

Keep the API small.

### Verification

```http
GET /api/verification/{phoneNumber}
```

Response:

```json
{
  "phoneNumber": "+267XXXXXXXX",
  "status": "HIGH_RISK",
  "riskScore": 78,
  "provider": "ORANGE",
  "reasons": [
    "Multiple reports",
    "Provider impersonation"
  ]
}
```

### Report

```http
POST /api/reports
```

### Incident

```http
GET /api/incidents
GET /api/incidents/{id}
```

### Escalation

```http
POST /api/incidents/{id}/escalate
```

---

# 19. Demo Data

Do not wait for real users to generate reports.

Seed the database.

Create:

```text
3 providers
10 official numbers
5 suspicious numbers
20 reports
3 incidents
```

Create one particularly convincing scam campaign:

```text
Number:
+267 XXXXXXXX

Claims:
Orange Money

Reports:
12

Attack:
OTP request

Risk:
89 CRITICAL
```

---

# 20. The Demo Story

The entire presentation should revolve around one scenario.

### Scene 1 — The victim

An elderly user receives:

> "I'm calling from your mobile-money provider. We detected suspicious activity."

The caller asks for an OTP.

### Scene 2 — Verification

The user dials ScamShield.

```text
1. Verify caller
```

Enters the number.

ScamShield:

```text
⚠️ HIGH RISK

This number is not an authorised
provider number.

Reports: 8

DO NOT provide your OTP.
```

### Scene 3 — Reporting

The user reports the number.

### Scene 4 — Intelligence

The dashboard updates:

```text
Reports: 9
Risk: 84 → CRITICAL
```

### Scene 5 — Pattern detection

The dashboard shows:

```text
12 users
Same number
Same provider impersonation
Same OTP request
```

### Scene 6 — Escalation

Security analyst:

```text
[ESCALATE]
```

System:

```text
Incident queued for provider review.
```

Then explain:

> In production, this interface would connect to authorised telecom-provider and law-enforcement workflows.

---

# 21. Suggested Hackathon Schedule

Adjust this to the actual hackathon duration.

## Day 1 — Foundation

### Morning

- Problem validation
- Requirements
- Architecture
- Repository
- Spring Boot setup
- PostgreSQL setup

### Afternoon

- Provider registry
- Database entities
- Verification API

### Evening

- Basic USSD simulator
- First end-to-end verification flow

---

## Day 2 — Core Intelligence

### Morning

- Reporting API
- Report database
- Risk engine

### Afternoon

- Event-driven report processing
- Incident creation
- Risk updates

### Evening

- USSD reporting flow
- End-to-end testing

---

## Day 3 — Security Operations

### Morning

- Admin dashboard
- Incident view
- Risk-number view

### Afternoon

- Mock provider escalation
- Authentication
- Authorization
- Audit logging

### Evening

- Integration testing
- Bug fixing

---

## Final Day / Final Hours

### Do not add major features.

Instead:

```text
Test
 ↓
Fix
 ↓
Polish
 ↓
Demo
 ↓
Pitch
```

---

# 22. Definition of MVP Complete

The MVP is considered complete when this works:

```text
                USER
                  │
                  ▼
            USSD Simulator
                  │
                  ▼
             Verification
                  │
                  ▼
             Risk Engine
                  │
          ┌───────┴───────┐
          ▼               ▼
       Report          Risk Score
          │               │
          └───────┬───────┘
                  ▼
              Incident
                  │
                  ▼
          Security Dashboard
                  │
                  ▼
             Escalation
```

If this entire flow works reliably, **stop building and start polishing**.

---

# 23. Stretch Goals

Only attempt these after MVP completion.

### Priority 1

- SMS scam reporting
- campaign grouping
- richer dashboard analytics

### Priority 2

- provider-specific portal
- notification simulation
- better incident workflow

### Priority 3

- ML-based risk scoring
- anomaly detection
- graph-based scam-network analysis

### Priority 4

- real USSD integration
- real provider APIs
- law-enforcement integration

---

# 24. Success Metrics for the Hackathon

Measure the prototype using simple metrics.

### User experience

- Number of USSD steps required to report a scam
- Time required to verify a caller
- Number of decisions required from the user

### Detection

- Number of suspicious reports grouped
- Risk-score changes after additional reports
- Number of incidents generated

### Operations

- Time from report → incident
- Time from incident → escalation

A useful design target is:

> **A user should be able to report a suspicious call in under one minute without needing to understand cybersecurity terminology.**

---

# 25. Final Development Principle

The team should repeatedly ask:

> **Does this feature improve the user's ability to safely respond to a scam, or the security team's ability to detect and respond to one?**

If the answer is no, it probably does not belong in the hackathon MVP.

The goal is not to build a complete telecommunications fraud platform in a few days.

The goal is to prove that:

> **A simple USSD interaction can become the entry point to a larger fraud-intelligence network.**
