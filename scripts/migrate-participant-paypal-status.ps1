# scripts/migrate-participant-paypal-status.ps1
# Adds participant e-mail confirmation and PayPal readiness columns.

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
    [string] $Sql,
    [switch] $Scalar
  )

  if( $DryRun ) {
    Write-Host $Sql
    return ""
  }

  $arguments = @(
    "--protocol=tcp",
    "-h$HostName",
    "-P$Port",
    "-u$Username",
    "-p$Password",
    "--database=$Database"
  )

  if( $Scalar ) {
    $arguments += @(
      "--batch",
      "--skip-column-names"
    )
  }

  $arguments += @(
    "-e",
    $Sql
  )

  & $mysql @arguments
  if( $LASTEXITCODE -ne 0 ) {
    throw "mysql.exe terminó con código $LASTEXITCODE"
  }
}

function Add-TopRacingColumnIfMissing {
  param(
    [string] $ColumnName,
    [string] $ColumnDefinition
  )

  $existsSql = @"
SELECT COUNT(*)
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'participant'
  AND COLUMN_NAME = '$ColumnName';
"@

  $exists = Invoke-TopRacingSql -Sql $existsSql -Scalar
  if( $DryRun -or $exists.Trim() -eq "0" ) {
    Invoke-TopRacingSql -Sql "ALTER TABLE participant ADD COLUMN $ColumnName $ColumnDefinition;"
  }
}

Add-TopRacingColumnIfMissing "email_confirmed" "BIT(1) NOT NULL DEFAULT b'0'"
Add-TopRacingColumnIfMissing "paypal_usable" "BIT(1) NOT NULL DEFAULT b'0'"
Add-TopRacingColumnIfMissing "paypal_payer_id" "VARCHAR(255) NULL"
Add-TopRacingColumnIfMissing "paypal_merchant_id" "VARCHAR(255) NULL"
Add-TopRacingColumnIfMissing "paypal_status" "VARCHAR(32) NOT NULL DEFAULT 'UNVERIFIED'"
Add-TopRacingColumnIfMissing "confirmed_at" "DATETIME NULL"
Add-TopRacingColumnIfMissing "paypal_verified_at" "DATETIME NULL"

$backfillSql = @"
UPDATE participant
SET email_confirmed = CASE
      WHEN confirmed = b'1' THEN b'1'
      ELSE email_confirmed
    END,
    paypal_status = CASE
      WHEN paypal_usable = b'1' THEN 'USABLE'
      WHEN paypal_status IS NULL OR paypal_status = '' THEN 'UNVERIFIED'
      ELSE paypal_status
    END;

UPDATE participant
SET confirmed = CASE
      WHEN email_confirmed = b'1' AND paypal_usable = b'1' THEN b'1'
      ELSE b'0'
    END,
    confirmed_at = CASE
      WHEN email_confirmed = b'1' AND paypal_usable = b'1' AND confirmed_at IS NULL THEN NOW()
      WHEN email_confirmed = b'1' AND paypal_usable = b'1' THEN confirmed_at
      ELSE NULL
    END,
    paypal_verified_at = CASE
      WHEN paypal_usable = b'1' AND paypal_verified_at IS NULL THEN NOW()
      WHEN paypal_usable = b'1' THEN paypal_verified_at
      ELSE NULL
    END;
"@

Invoke-TopRacingSql -Sql $backfillSql
