# ScamShield API contract

Base URL: `http://localhost:8081`. Botswana numbers are accepted as `+267XXXXXXXX` or local `7XXXXXXX` and are normalized to the international form. API errors use `{ "error": { "code": "...", "message": "..." } }`.

- `GET /api/health` → `{ "status": "UP", "service": "scamshield" }`
- `GET /api/providers` and `GET /api/providers/{id}/official-numbers`
- `GET /api/verification/{phone}` → registry `status` (`VERIFIED_PROVIDER_NUMBER`, `UNKNOWN_NUMBER`, or risk level), provider, score, reasons, and report count. Registry status never authenticates a caller's identity.
- `POST /api/reports` body `{phoneNumber, claimedProvider, attackType, description?, reporterKey?}`. `claimedProvider` is `Orange|Mascom|BTC|Other`; `attackType` is `PIN|OTP|MONEY|OTHER`. Reports are submitted through the USSD simulator flow. Never submit secrets. Returns report ID and updated score.
- `GET /api/reports` → latest 20 reports.
- Admin endpoints require `X-Admin-Key` (local demo default `demo-admin-key`, configure `SCAMSHIELD_ADMIN_KEY`): `GET /api/admin/dashboard`, `GET /api/admin/numbers/{phone}`, `GET /api/admin/reports?phone=&provider=&attackType=&channel=&limit=`, `GET /api/admin/analytics`, `GET /api/admin/providers/{provider}`, and `GET /api/admin/audit`.
- `GET /api/incidents`, `GET /api/incidents/{id}`, `PATCH /api/incidents/{id}` with `{status}`, `POST /api/incidents/{id}/escalate`, and `POST /api/incidents/{id}/notifications` with `{channel,recipient?,message?}`. Escalation creates a persisted item in ScamShield's internal provider-review queue. Notifications create persisted in-app entries for the analyst team. The project has no external provider, SMS, email, or push delivery integration.

Scores are explainable signals: each report +10; provider impersonation once +20; distinct OTP/PIN/money behaviours +25 each; two distinct reporter keys +20; capped at 100.
