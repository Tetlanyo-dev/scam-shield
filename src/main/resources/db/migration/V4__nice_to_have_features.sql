ALTER TABLE reports ADD COLUMN IF NOT EXISTS channel VARCHAR(40) NOT NULL DEFAULT 'USSD_SIMULATOR';
CREATE INDEX IF NOT EXISTS idx_reports_created_at ON reports(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_reports_provider_attack ON reports(claimed_provider, attack_type);

CREATE TABLE IF NOT EXISTS audit_logs (
  id BIGSERIAL PRIMARY KEY,
  action VARCHAR(80) NOT NULL,
  actor VARCHAR(120) NOT NULL,
  target_type VARCHAR(50) NOT NULL,
  target_id VARCHAR(120),
  details VARCHAR(1000),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_audit_created_at ON audit_logs(created_at DESC);

CREATE TABLE IF NOT EXISTS notification_simulations (
  id BIGSERIAL PRIMARY KEY,
  incident_id BIGINT NOT NULL REFERENCES incidents(id),
  channel VARCHAR(30) NOT NULL,
  recipient VARCHAR(120) NOT NULL,
  message VARCHAR(500) NOT NULL,
  status VARCHAR(30) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
