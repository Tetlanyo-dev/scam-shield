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

Keep module internals inside their domain package. Add database changes as ordered Flyway migrations. Provider numbers in local seed/demo data must be clearly marked as mock until independently verified. Number reputation is a risk signal and does not prove who placed a call.

## Risk scoring

Implement the weights and thresholds in `design.md`. Keep scores explainable by returning the reasons that contributed to the result; never request or store a user's PIN or OTP.
