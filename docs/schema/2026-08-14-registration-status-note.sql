-- docs/schema/2026-08-14-registration-status-note.sql
-- Adds the written note required for canceled or disqualified registrations.

SET @topracing_schema = DATABASE();
SET @status_note_column_exists = (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @topracing_schema
    AND TABLE_NAME = 'registration'
    AND COLUMN_NAME = 'status_note'
);
SET @status_note_sql = IF(
  @status_note_column_exists = 0,
  'ALTER TABLE registration ADD COLUMN status_note VARCHAR(512) NULL',
  'SELECT ''registration.status_note already exists'' AS migration_status'
);

PREPARE status_note_statement FROM @status_note_sql;
EXECUTE status_note_statement;
DEALLOCATE PREPARE status_note_statement;
