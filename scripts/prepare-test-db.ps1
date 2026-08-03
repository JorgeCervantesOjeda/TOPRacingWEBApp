<# scripts/prepare-test-db.ps1
   Recreates an isolated MySQL catalog for local integration and browser tests. #>
param(
  [string]$MysqlBin = "C:\Program Files\MySQL\MySQL Server 8.0\bin",
  [string]$SourceDb = "topracing26",
  [string]$TargetDb = "topracing26_test",
  [string]$Username = "admin",
  [string]$Password = "admin"
)

$ErrorActionPreference = "Stop"

function Assert-SafeDbName {
  param(
    [Parameter(Mandatory = $true)]
    [string]$Name,
    [Parameter(Mandatory = $true)]
    [string]$Label
  )

  if ($Name -notmatch '^[A-Za-z0-9_]+$') {
    throw "$Label contiene caracteres no permitidos: $Name"
  }
}

function Quote-DbName {
  param(
    [Parameter(Mandatory = $true)]
    [string]$Name
  )

  return "``$Name``"
}

Assert-SafeDbName -Name $SourceDb -Label "SourceDb"
Assert-SafeDbName -Name $TargetDb -Label "TargetDb"

if ($TargetDb -eq $SourceDb) {
  throw "TargetDb no puede ser igual a SourceDb."
}

if ($TargetDb -notmatch '_test$') {
  throw "TargetDb debe terminar en _test para permitir DROP DATABASE: $TargetDb"
}

$mysql = Join-Path $MysqlBin "mysql.exe"
if (-not (Test-Path -LiteralPath $mysql)) {
  throw "No se encontró mysql.exe en $MysqlBin"
}

$schemaTables = @(
  "appstats",
  "bid",
  "car",
  "country",
  "countryregion",
  "currency",
  "participant",
  "penaltiespl",
  "planetregion",
  "pointscount",
  "province",
  "provinceregion",
  "regatta",
  "registration",
  "variant",
  "venue"
)

$seedTables = @(
  "appstats",
  "currency",
  "planetregion",
  "country",
  "countryregion",
  "province",
  "provinceregion",
  "participant",
  "venue",
  "variant"
)

$quotedSourceDb = Quote-DbName -Name $SourceDb
$quotedTargetDb = Quote-DbName -Name $TargetDb

$sql = New-Object System.Text.StringBuilder
[void]$sql.AppendLine("SET FOREIGN_KEY_CHECKS=0;")
[void]$sql.AppendLine("DROP DATABASE IF EXISTS $quotedTargetDb;")
[void]$sql.AppendLine("CREATE DATABASE $quotedTargetDb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;")

foreach ($table in $schemaTables) {
  [void]$sql.AppendLine("CREATE TABLE $quotedTargetDb.$table LIKE $quotedSourceDb.$table;")
}

foreach ($table in $seedTables) {
  [void]$sql.AppendLine("INSERT INTO $quotedTargetDb.$table SELECT * FROM $quotedSourceDb.$table;")
}

[void]$sql.AppendLine("SET FOREIGN_KEY_CHECKS=1;")

& $mysql --protocol=tcp "-u$Username" "-p$Password" -e $sql.ToString()
if ($LASTEXITCODE -ne 0) {
  throw "No fue posible preparar la base $TargetDb."
}

Write-Output "Prepared isolated test database $TargetDb from $SourceDb."
