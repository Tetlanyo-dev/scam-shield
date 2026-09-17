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
4. Open `http://localhost:8081` for the security dashboard or `http://localhost:8081/ussd.html` for the USSD simulator. Each page links to the other in its top navigation.

The default local PostgreSQL host port is `5433` (container port `5432`) to avoid colliding with a PostgreSQL service already installed on the computer. Keep `DB_PORT` the same for Compose and the Spring Boot app if you change it.

The dashboard demo key is `demo-admin-key`; set `SCAMSHIELD_ADMIN_KEY` for another local value. API details are in [API_CONTRACT.md](API_CONTRACT.md). Reset demo data with `docker compose down -v` followed by `docker compose up -d postgres`.

Run tests with `mvn test`. Stop the database with `docker compose down`; add `-v` only when you also want to remove the local database volume.

### PostgreSQL password authentication failed

PostgreSQL keeps its initialized user password in the named data volume. Changing `.env` or `docker-compose.yml` later does not update that stored password. Keep `POSTGRES_PASSWORD` the same in `.env` for both the database and application. To preserve the existing database, update its role password inside the running container:

```powershell
docker exec -u postgres scamshield-postgres psql -d postgres -c "ALTER USER scamshield WITH PASSWORD 'your-current-POSTGRES_PASSWORD';"
```

Replace the example value with the exact `POSTGRES_PASSWORD` in `.env` (or `change-me-for-local-development` when using the defaults), then restart `mvn spring-boot:run`. If the database contains no data you need, `docker compose down -v` followed by `docker compose up -d postgres` recreates it with the current configured credentials.

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

The team workflow follows short-lived `feature/...` branches, small commits, and pull-request review. Owners are recorded in `GITHUB_ISSUES_PLAN.md`.

## Risk scoring

Implement the weights and thresholds in `design.md`. Keep scores explainable by returning the reasons that contributed to the result; never request or store a user's PIN or OTP.
