# ScamShield

ScamShield is a hackathon MVP for helping users verify suspicious mobile-money calls, report scams, and give security analysts explainable risk signals and incidents. The current scaffold uses a Spring Boot modular monolith, PostgreSQL, and static HTML/CSS/JavaScript served by Spring Boot.

## Requirements

- Java 21 or later
- Maven 3.6.3 or later
- Docker with the Compose plugin (for the local PostgreSQL database)

## Run locally

1. Copy `.env.example` to `.env` and change the local database password if needed. The supplied values are for local development only.
2. Start PostgreSQL with `docker compose up -d postgres`.
3. Start the application with `mvn spring-boot:run`.
4. Open `http://localhost:8080`.

Run tests with `mvn test`. Stop the database with `docker compose down`; add `-v` only when you also want to remove the local database volume.

## Team ownership

- Tetlanyo-dev: database, dashboard integration, and integration checks.
- Thabang-Tsimakwane: backend contract, risk scoring, incidents, and safeguards.
- loratopoliten: USSD simulator and final demo.
- kaelowadingalo1-sys: dashboard shell and USSD API integration.
- Vitore-dev: provider verification, reports, and incident UI.

## API agreement

- Local base URL: `http://localhost:8080`
- JSON API prefix: `/api`
- Normalize phone numbers to the agreed Botswana format before lookup or persistence.
- Use uppercase `SCREAMING_SNAKE_CASE` enum values.
- Return HTTP `400` validation errors as JSON with `timestamp`, `status`, `error`, `message`, and `path`.
- Protect admin actions with authentication and return HTTP `401` or `403` when access is denied.

Core endpoints:

```text
GET  /api/providers
GET  /api/providers/{id}/official-numbers
GET  /api/verification/{phoneNumber}
POST /api/reports
GET  /api/reports
GET  /api/risk/{phoneNumber}
GET  /api/incidents
GET  /api/incidents/{id}
POST /api/incidents/{id}/escalate
```

Reports use `phoneNumber`, `claimedProvider`, `attackType`, `occurredAt`, and an optional `description`. Responses include verification status, risk score, risk level, report count, and explainable reasons where applicable.

## Branch and review workflow

Develop each issue on its own `feature/issue-<number>-<short-name>` branch. Do not commit directly to `main`. Open a pull request after the issue acceptance checks pass, get at least one teammate review, and merge only after approval and a clean test run.

## Project layout

```text
src/
  main/
    java/com/scamshield/
      admin/          # dashboard and administrative workflows
      common/         # shared API, validation, and error handling
      incident/       # incident lifecycle and escalation
      provider/       # provider registry and official-number data
      reporting/      # report intake and persistence
      risk/           # explainable risk scoring
      verification/   # caller-number verification
    resources/
      db/migration/   # versioned Flyway SQL migrations
      static/         # USSD simulator and analyst dashboard assets
  test/
    java/com/scamshield/  # unit and integration tests by module
    resources/            # test-only configuration and fixtures
scripts/                  # developer and demo helper scripts
```

Keep module internals inside their domain package. Add database changes as ordered Flyway migrations. Provider numbers in local seed/demo data must be clearly marked as mock until independently verified. Number reputation is a risk signal and does not prove who placed a call. Never request or store PINs, OTPs, passwords, or account credentials.

## Risk scoring

Implement the weights and thresholds in `design.md`, returning the reasons behind every score. Risk scores support review; they do not prove identity or automatically block a number.
