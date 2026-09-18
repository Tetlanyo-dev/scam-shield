ALTER TABLE reports ADD COLUMN IF NOT EXISTS description VARCHAR(500);
ALTER TABLE reports ADD COLUMN IF NOT EXISTS reporter_key VARCHAR(120);
CREATE INDEX IF NOT EXISTS idx_reports_phone_created ON reports(phone_number, created_at);
CREATE INDEX IF NOT EXISTS idx_phone_risks_level_score ON phone_risks(level, score);
CREATE TABLE IF NOT EXISTS escalations (id BIGSERIAL PRIMARY KEY, incident_id BIGINT NOT NULL REFERENCES incidents(id), destination VARCHAR(120) NOT NULL, external_reference VARCHAR(50) NOT NULL, status VARCHAR(30) NOT NULL, created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP);
