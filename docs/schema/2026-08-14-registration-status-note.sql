-- docs/schema/2026-08-14-registration-status-note.sql
-- Adds the written note required for canceled or disqualified registrations.

ALTER TABLE registration
  ADD COLUMN status_note VARCHAR(512) NULL;
