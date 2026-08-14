# scripts/migrate-participant-access-status.ps1
# Adds local promoter restriction and global exclusion tables for participant access.

param(
  [string] $MysqlBin = "C:\Program Files\MySQL\MySQL Server 8.0\bin",
  [string] $Database = "topracing26",
  [string] $Username = "admin",
  [string] $Password = "admin",
  [string] $HostName = "localhost",
  [int] $Port = 3306,
  [switch] $DryRun
)

$ErrorActionPreference = "Stop"

$mysql = Join-Path $MysqlBin "mysql.exe"
if( -not ( Test-Path -LiteralPath $mysql ) ) {
  throw "No se encontró mysql.exe en $MysqlBin"
}

function Invoke-TopRacingSql {
  param(
    [string] $Sql
  )

  if( $DryRun ) {
    Write-Host $Sql
    return
  }

  $arguments = @(
    "--protocol=tcp",
    "-h$HostName",
    "-P$Port",
    "-u$Username",
    "-p$Password",
    "--database=$Database",
    "-e",
    $Sql
  )

  & $mysql @arguments
  if( $LASTEXITCODE -ne 0 ) {
    throw "mysql.exe terminó con código $LASTEXITCODE"
  }
}

$sql = @"
CREATE TABLE IF NOT EXISTS participant_global_exclusion (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  id_participant BIGINT UNSIGNED NOT NULL,
  active BIT(1) NOT NULL DEFAULT b'1',
  reason VARCHAR(512) NULL,
  created_at DATETIME NOT NULL,
  resolved_at DATETIME NULL,
  id_created_by BIGINT UNSIGNED NULL,
  id_resolved_by BIGINT UNSIGNED NULL,
  PRIMARY KEY (id),
  INDEX idx_pge_participant_active (id_participant, active),
  INDEX idx_pge_created_by (id_created_by),
  INDEX idx_pge_resolved_by (id_resolved_by),
  CONSTRAINT fk_pge_participant
    FOREIGN KEY (id_participant) REFERENCES participant(id),
  CONSTRAINT fk_pge_created_by
    FOREIGN KEY (id_created_by) REFERENCES participant(id),
  CONSTRAINT fk_pge_resolved_by
    FOREIGN KEY (id_resolved_by) REFERENCES participant(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS participant_local_restriction (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  id_participant BIGINT UNSIGNED NOT NULL,
  id_promoter BIGINT UNSIGNED NOT NULL,
  kind VARCHAR(32) NOT NULL,
  active BIT(1) NOT NULL DEFAULT b'1',
  reason VARCHAR(512) NULL,
  created_at DATETIME NOT NULL,
  resolved_at DATETIME NULL,
  id_created_by BIGINT UNSIGNED NULL,
  id_resolved_by BIGINT UNSIGNED NULL,
  PRIMARY KEY (id),
  INDEX idx_plr_participant_promoter_kind_active
    (id_participant, id_promoter, kind, active),
  INDEX idx_plr_promoter_active (id_promoter, active),
  INDEX idx_plr_created_by (id_created_by),
  INDEX idx_plr_resolved_by (id_resolved_by),
  CONSTRAINT fk_plr_participant
    FOREIGN KEY (id_participant) REFERENCES participant(id),
  CONSTRAINT fk_plr_promoter
    FOREIGN KEY (id_promoter) REFERENCES participant(id),
  CONSTRAINT fk_plr_created_by
    FOREIGN KEY (id_created_by) REFERENCES participant(id),
  CONSTRAINT fk_plr_resolved_by
    FOREIGN KEY (id_resolved_by) REFERENCES participant(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS participant_access_decision_record (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  event_type VARCHAR(64) NOT NULL,
  id_actor_participant BIGINT UNSIGNED NULL,
  id_target_participant BIGINT UNSIGNED NOT NULL,
  id_promoter BIGINT UNSIGNED NULL,
  id_regatta BIGINT UNSIGNED NULL,
  id_registration BIGINT UNSIGNED NULL,
  id_global_exclusion BIGINT UNSIGNED NULL,
  id_local_restriction BIGINT UNSIGNED NULL,
  reason VARCHAR(512) NULL,
  effect VARCHAR(64) NOT NULL,
  created_at DATETIME NOT NULL,
  PRIMARY KEY (id),
  INDEX idx_padr_target_created (id_target_participant, created_at),
  INDEX idx_padr_actor_created (id_actor_participant, created_at),
  INDEX idx_padr_promoter_created (id_promoter, created_at),
  INDEX idx_padr_event_type (event_type),
  CONSTRAINT fk_padr_actor
    FOREIGN KEY (id_actor_participant) REFERENCES participant(id),
  CONSTRAINT fk_padr_target
    FOREIGN KEY (id_target_participant) REFERENCES participant(id),
  CONSTRAINT fk_padr_promoter
    FOREIGN KEY (id_promoter) REFERENCES participant(id),
  CONSTRAINT fk_padr_regatta
    FOREIGN KEY (id_regatta) REFERENCES regatta(id),
  CONSTRAINT fk_padr_registration
    FOREIGN KEY (id_registration) REFERENCES registration(id),
  CONSTRAINT fk_padr_global_exclusion
    FOREIGN KEY (id_global_exclusion) REFERENCES participant_global_exclusion(id),
  CONSTRAINT fk_padr_local_restriction
    FOREIGN KEY (id_local_restriction) REFERENCES participant_local_restriction(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
"@

Invoke-TopRacingSql -Sql $sql
