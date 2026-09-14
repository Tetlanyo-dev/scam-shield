# ScamShield: three-day GitHub issue plan

This is an issue-ready plan for a five-person team. The issues are grouped by day and include an owner role, dependencies, scope, and acceptance criteria. Replace Person A–E with the team members' names when assigning the GitHub issues.

## Team roles

- **Person A — Backend/API lead:** Spring Boot setup, API contracts, verification/report/incident endpoints, integration decisions.
- **Person B — Database/security:** PostgreSQL, schema and seed data, risk engine, validation, admin access controls.
- **Person C — USSD experience:** Web USSD simulator and the user-side verification/report/emergency journey.
- **Person D — Operations dashboard:** Admin dashboard, incident and number views, mock escalation controls.
- **Person E — Integration/QA/demo:** integration coordination, end-to-end test checklist, seeded demo story, final walkthrough.

Everyone joins the setup and final integration issues. One owner drives each implementation issue and asks for review from a teammate in a different role.

## Three-day sequence

### Day 1 — Foundation and runnable skeleton

1. #1 Project setup and team working agreement (all; Person E coordinates)
2. #2 Spring Boot application and API contract (Person A)
3. #3 PostgreSQL schema and demo seed data (Person B)
4. #4 USSD simulator shell (Person C)
5. #5 Security dashboard shell (Person D)

**Day 1 checkpoint:** a fresh clone can start the app and database; the UI shells load; the API contract and local setup instructions are agreed.

### Day 2 — User reporting and explainable risk

6. #6 Provider registry and caller verification API (Persons A + B)
7. #7 Report submission API and persistence (Persons A + B)
8. #8 Risk score implementation from `design.md` (Person B; Person A reviews)
9. #9 Connect USSD verification and reporting flows (Person C)
10. #10 Dashboard number and report views (Person D)
11. #11 Integration check and cross-feature fixes (Person E)

**Day 2 checkpoint:** a user can verify a number and submit a report; the report persists and changes an explainable risk result; the dashboard can show current numbers/reports.

### Day 3 — Incidents, escalation, security, and demo

12. #12 Incident lifecycle and mock escalation API (Person A)
13. #13 Incident dashboard and escalation interaction (Person D)
14. #14 Admin protection and abuse safeguards (Person B)
15. #15 End-to-end demo, QA, and final polish (Person E; all contribute)

**Day 3 checkpoint:** run the complete demo from a clean start: verify → report → risk update → incident → dashboard → mock escalation. Freeze feature work after the checkpoint and use remaining time for defects and demo practice.

## Risk scoring decision for issue #8

Use the weights and levels in `design.md`:

- Start at 0.
- Add 10 for each report.
- Add 20 when provider impersonation is reported.
- Add 25 for each distinct reported behavior: OTP request, PIN request, and money request.
- Add 20 once there are multiple independent reports (at least two distinct reporters).
- Cap the stored/displayed score at 100.
- Map 0–29 to `LOW`, 30–59 to `SUSPICIOUS`, 60–79 to `HIGH`, and 80–100 to `CRITICAL`.

Apply each behavior/impersonation bonus once per phone number, so duplicate reports do not repeatedly add the same categorical bonus. A new report adds its +10 report points. Document this interpretation in the code and tests. Do not store PINs, OTPs, or passwords. A high score is an investigative signal, not proof and not an automatic block.

---
# Set up the project and agree on the team workflow


### Goal

Create a shared, reproducible starting point so all five people can work in parallel without incompatible assumptions.

### Tasks

- Agree and record the backend, frontend, Java, and database versions and the local run commands.
- Create the initial application/repository structure and a `.gitignore`; keep secrets and local environment files out of Git.
- Add a README with prerequisites, setup, run, test, and demo commands.
- Agree on API naming, phone-number format, report fields, enum names, error response shape, and CORS/local ports; record these in the API contract from issue #2.
- Agree on short-lived `feature/...` branches, small commits, pull-request review, and one shared integration branch/approach.
- Each person confirms they can clone, build, and run their assigned shell or service.

### Acceptance criteria

- A teammate on a clean machine can follow the README to launch the skeleton.
- The five owners and their areas are recorded in the project board/README.
- The team has agreed on one API contract before frontend/backend integration begins.
- No credentials or personal data are committed.

# Bootstrap Spring Boot and define the REST API contract


### Goal

Create the backend skeleton and the contract the simulator and dashboard will use.

### Tasks

- Create a Spring Boot app with the agreed Java version and dependencies for web, validation, persistence, PostgreSQL, and tests.
- Establish package/module boundaries for provider registry, verification, reports, risk, incidents, and admin.
- Add a health endpoint and a consistent JSON error response.
- Document request/response examples for provider list, verification, report creation, risk lookup, incidents, and escalation.
- Document API field names/enums, phone-number normalization, and the local base URL.

### Acceptance criteria

- The application starts locally and the health endpoint succeeds.
- API examples are committed and frontend owners can build against them without guessing field names.
- Invalid requests return a useful 4xx response in the agreed error shape.

# Create the PostgreSQL schema and seeded demo data



### Goal

Persist the minimum data needed for trusted-number lookup, reports, risk results, and incidents.

### Tasks

- Add PostgreSQL local configuration with environment-based credentials and a documented example env file containing placeholders only.
- Define migrations/entities for providers, official numbers, phone-number records, reports, risk scores, incidents, and escalation records as needed by the agreed contract.
- Seed mock provider names and clearly identified mock official numbers; do not present unverified values as real provider data.
- Seed a demo suspicious number and reports sufficient to show low through critical risk states, or provide a resettable demo-data command.
- Add indexes/unique constraints for phone numbers and the lookup paths the MVP uses.

### Acceptance criteria

- A fresh local database can be created and seeded using documented commands.
- The app connects to the database without hard-coded secrets.
- Demo records are clearly marked as mock data and can be reset reproducibly.

# Build the web-based USSD simulator shell


### Goal

Build the user-facing simulator frame and navigation for the short-code-style interaction.

### Tasks

- Create a mobile-friendly USSD conversation view with input, submit, back/restart, and clear system/user messages.
- Implement the main menu: verify caller, report scam, emergency fraud, and security advice.
- Add loading, success, validation-error, and network-error states.
- Keep text short, readable, and usable without cybersecurity knowledge.
- Do not ask users to enter PINs, OTPs, passwords, or account credentials.

### Acceptance criteria

- All menu items can be navigated in the simulator with keyboard and touch.
- The simulator can be demonstrated with mocked responses before API integration.
- It does not request or store authentication secrets.

##  Build the security dashboard shell


### Goal

Create the operations view structure so the team can integrate real backend data on Day 2.

### Tasks

- Build a responsive dashboard layout with summary cards, suspicious-number table, recent reports, and incidents area.
- Add empty, loading, and error states; keep demo/mock mode visibly identified if used.
- Add number-detail and incident-detail panel/page shells.
- Reserve clear locations for score, level, reasons, report count, claimed provider, incident status, and escalation state.

### Acceptance criteria

- The dashboard shell runs locally and works at a laptop and mobile viewport.
- Main sections remain legible with empty or long data values.
- Mock data is visibly distinguishable from live API data.

## Issue #6 — Day 2: Implement provider registry and caller verification



### Goal

Let the USSD client check whether a number is in the seeded official-number registry and retrieve its current risk status.

### Tasks

- Implement provider list and official-number lookup endpoints from the API contract.
- Normalize and validate Botswana phone-number input consistently.
- Return `VERIFIED_PROVIDER_NUMBER`, `UNKNOWN_NUMBER`, or an appropriate risk status without claiming to authenticate the caller's identity.
- Include provider (if known), current score/level, report count, and concise reasons where available.
- Add service-level tests for official and unknown numbers and invalid formats.

### Acceptance criteria

- Seeded official numbers return the expected provider verification result.
- Unknown valid numbers return a safe unknown/risk result rather than being called verified.
- Invalid numbers receive a 4xx response.
- The response clearly distinguishes number registry status from proof of caller identity.

## Issue #7 — Day 2: Implement scam report submission and persistence



### Goal

Accept a minimal, validated scam report and persist it for risk and incident processing.

### Tasks

- Implement `POST /api/reports` using agreed fields: phone number, claimed provider, attack type, timestamp, and optional short description.
- Validate number, provider, attack type, and description length.
- Record enough information to distinguish independent reports for the MVP without collecting unnecessary identity information.
- Never accept or store PINs, OTPs, or passwords; reject fields that attempt to submit them if practical.
- Return the saved report ID and updated risk result, or the API contract's equivalent.

### Acceptance criteria

- A valid report persists and can be retrieved/seen in later queries.
- Invalid fields return clear 4xx errors.
- No PIN/OTP/password data is stored.
- Duplicate/retry behavior is documented sufficiently for the demo client.

## Issue #8 — Day 2: Implement explainable risk scoring from `design.md`

**Owner:** Person B. **Review:** Person A.  
**Depends on:** #3 and #7.

### Goal

Apply the design document's deterministic weights and return score reasons users and analysts can understand.

### Rules

- Initial score is 0.
- Each report contributes +10.
- Provider impersonation contributes +20 once per phone number.
- OTP request, PIN request, and money request each contribute +25 once per distinct behavior per phone number.
- At least two independent reports (distinct reporters) contribute +20 once.
- Cap the score at 100.
- Levels: 0–29 `LOW`; 30–59 `SUSPICIOUS`; 60–79 `HIGH`; 80–100 `CRITICAL`.

### Tasks

- Implement a pure, testable scoring service and persist/update the aggregate score and reason list.
- Recalculate when a report is saved; make repeat processing idempotent so bonuses are not counted again.
- Return the level and human-readable reasons with verification/report/dashboard data.
- Test boundary scores 29/30, 59/60, 79/80, the 100 cap, duplicates, multiple reporters, and every scoring signal.

### Acceptance criteria

- The implemented weights and boundaries match this issue and `design.md`.
- Replaying a report event does not double-count a bonus.
- Score, level, and reasons are consistent across lookup and dashboard responses.
- Scoring never automatically blocks a number or treats an allegation as proof.

## Issue #9 — Day 2: Connect USSD verification and report journeys to the API

**Owner:** Person C. **Review:** Person A.  
**Depends on:** #4, #6, and #7.

### Goal

Make the core user journey work from the simulator through the backend.

### Tasks

- Connect verify flow to the verification endpoint and render provider status, risk level, reasons, and safe advice.
- Connect report flow to report submission and show a confirmation only after success.
- Validate phone input in the UI while treating backend validation as authoritative.
- Implement emergency fraud and security advice as concise, safe guidance that never asks for a secret.
- Handle timeouts, server errors, and retries without silently duplicating a report.

### Acceptance criteria

- A user can verify a number and report it without leaving the simulator.
- The response is plain-language and includes “do not share your OTP/PIN” guidance where appropriate.
- Failure states are visible and let the user recover.

## Issue #10 — Day 2: Populate dashboard number and report views from the API

**Owner:** Person D. **Review:** Person A.  
**Depends on:** #5, #6, and #7.

### Goal

Show current suspicious-number and report data instead of static mock data.

### Tasks

- Connect dashboard summary, number list, recent reports, and number-detail views to available endpoints.
- Show score, level, report count, reasons, attack types, and claimed providers.
- Sort/filter high-risk records in a simple way useful for a live demo.
- Clearly show loading, no-data, and API-error states.
- Keep mock/demo fixtures separate from API mode.

### Acceptance criteria

- Submitting a report is reflected in the dashboard after refresh or a reasonable update interval.
- An analyst can open a number and understand why the score has its current value.
- Dashboard data is not presented as verified provider intelligence unless it came from the seeded official-number registry.

## Issue #11 — Day 2: Run integration checks and resolve API/UI mismatches

**Owner:** Person E. **Contributors:** A–D.  
**Depends on:** #6–#10 as available.

### Goal

Find integration breaks while there is still time to fix them before incident/dashboard work.

### Tasks

- Run the happy path from simulator → API → database → score → dashboard.
- Check phone formats, enum casing, timestamps, empty data, server validation, and CORS/local URLs across clients.
- Record blockers with reproducible steps and assign each fix to its owning module.
- Verify a fresh reset/reseed still supports the demo scenario.

### Acceptance criteria

- The happy path completes on the integrated branch.
- Any known remaining defects are written down with severity and owner.
- No integration issue is left as an undocumented assumption.

## Issue #12 — Day 3: Implement incident lifecycle and mock escalation API

**Owner:** Person A. **Pair/review:** Person B.  
**Depends on:** #7 and #8.

### Goal

Turn a cluster of related reports into a trackable incident and demonstrate escalation without contacting a real provider.

### Tasks

- Agree and document a simple MVP incident rule (for example, create/update an incident when a number reaches `HIGH` or `CRITICAL`); keep it configurable if time allows.
- Create or update an incident idempotently as additional relevant reports arrive.
- Support listing/viewing incidents and statuses from the design: `OPEN`, `UNDER_INVESTIGATION`, `ESCALATED`, `RESOLVED`, `FALSE_POSITIVE`.
- Add a mock escalation endpoint that records destination, reference, timestamp, and `QUEUED` status; it must not call a real provider.
- Keep audit-relevant timestamps and status changes.

### Acceptance criteria

- Reports meeting the documented rule create/update an incident without duplicates.
- An analyst can request incident list/detail data.
- Mock escalation returns a clear queued reference and is visibly simulated.
- Risk reports remain signals for human review, not automatic punitive actions.

## Issue #13 — Day 3: Complete incident views and mock escalation interaction

**Owner:** Person D. **Review:** Person A.  
**Depends on:** #5 and #12.

### Goal

Let an analyst inspect an incident and demonstrate the mock escalation outcome from the dashboard.

### Tasks

- Populate incident list and detail panels with status, score, linked number, reports, attack types, and reasons.
- Add status/action controls supported by the API.
- Add an escalation action that shows destination, reference, and queued status after success.
- Make simulated/provider-review status clear to the demo audience.
- Add recoverable loading and error feedback.

### Acceptance criteria

- The demo incident is visible with its supporting reports and reasons.
- An analyst can trigger mock escalation and see the resulting status/reference.
- No UI wording implies a real telecom provider received the escalation.

## Issue #14 — Day 3: Add minimum admin protection and abuse safeguards

**Owner:** Person B. **Review:** Person A.  
**Depends on:** #2, #7, and #12.

### Goal

Cover the highest-value MVP safeguards without expanding into production infrastructure.

### Tasks

- Protect dashboard/admin write endpoints with the simplest agreed local/demo authentication approach; document demo credentials outside source control.
- Validate all API input server-side and enforce reasonable payload/description limits.
- Add a basic report submission rate limit or a clearly documented fallback safeguard if framework/time constraints prevent a robust limiter.
- Ensure logs avoid PINs, OTPs, credentials, and unnecessary personal data.
- Document the limits of demo authentication and mock provider data.

### Acceptance criteria

- Unauthenticated access to protected admin actions is rejected.
- Report endpoints reject malformed/oversized inputs and are not trivially spammed in the demo.
- Secrets are not committed or logged.
- The team can explain the limits of the hackathon safeguards honestly.

## Issue #15 — Day 3: Verify the complete demo, fix blockers, and prepare the walkthrough

**Owner:** Person E. **Contributors:** all five.  
**Depends on:** #1–#14 as applicable.

### Goal

Prove the MVP works from a clean start and prepare a concise, repeatable demo.

### Demo sequence

1. Start the database and application using README instructions.
2. In the USSD simulator, verify a suspicious mock number and show its risk reasons and safe advice.
3. Submit an OTP-request scam report; show confirmation and the increased risk score.
4. Show the corresponding incident and supporting reports in the dashboard.
5. Trigger mock escalation and show the queued destination/reference.

### Tasks

- Run the full sequence from a clean database reset/reseed.
- Check the score calculation against the exact issue #8 test cases and demo data.
- Fix only demo-blocking defects after the feature freeze; record lower-priority issues for follow-up.
- Prepare a short ownership-aware presentation: problem, user flow, explainable scoring, incident response, prototype limitations.
- Rehearse once with each person covering their contribution and likely judge questions.

### Acceptance criteria

- The sequence completes reliably from documented commands.
- No real provider integration or automatic number blocking is implied.
- Known limitations and any non-blocking bugs are recorded.
- The demo can be delivered within the team's allotted time.

