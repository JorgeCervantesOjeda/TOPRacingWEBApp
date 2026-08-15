-- docs/schema/2026-08-14-participant-terms-acceptance.sql
-- Adds versioned rules acceptance fields required by SRS v2.7.

SET @topracing_schema = DATABASE();
SET @terms_version_column_exists = (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @topracing_schema
    AND TABLE_NAME = 'participant'
    AND COLUMN_NAME = 'terms_version_accepted'
);
SET @terms_version_sql = IF(
  @terms_version_column_exists = 0,
  'ALTER TABLE participant ADD COLUMN terms_version_accepted VARCHAR(64) NULL',
  'SELECT ''participant.terms_version_accepted already exists'' AS migration_status'
);

PREPARE terms_version_statement FROM @terms_version_sql;
EXECUTE terms_version_statement;
DEALLOCATE PREPARE terms_version_statement;

SET @terms_accepted_at_column_exists = (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @topracing_schema
    AND TABLE_NAME = 'participant'
    AND COLUMN_NAME = 'terms_accepted_at'
);
SET @terms_accepted_at_sql = IF(
  @terms_accepted_at_column_exists = 0,
  'ALTER TABLE participant ADD COLUMN terms_accepted_at DATETIME NULL',
  'SELECT ''participant.terms_accepted_at already exists'' AS migration_status'
);

PREPARE terms_accepted_at_statement FROM @terms_accepted_at_sql;
EXECUTE terms_accepted_at_statement;
DEALLOCATE PREPARE terms_accepted_at_statement;
