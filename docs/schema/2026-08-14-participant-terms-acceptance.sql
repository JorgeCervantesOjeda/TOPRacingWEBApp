-- docs/schema/2026-08-14-participant-terms-acceptance.sql
-- Adds versioned rules acceptance fields required by SRS v2.7.

ALTER TABLE participant
  ADD COLUMN terms_version_accepted VARCHAR(64) NULL,
  ADD COLUMN terms_accepted_at DATETIME NULL;
