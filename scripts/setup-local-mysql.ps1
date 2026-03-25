param(
  [string]$ServiceName = "MySQL80",
  [string]$MysqlBin = "C:\Program Files\MySQL\MySQL Server 8.0\bin",
  [string]$DefaultsFile = "C:\ProgramData\MySQL\MySQL Server 8.0\my.ini",
  [string]$DumpFile = "C:\Users\usuario\Dropbox\NetBeansProjects\TOPRacingWEBApp\topracing26.sql"
)

$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $PSScriptRoot
$tmpDir = Join-Path $projectRoot "tmp"
$backupDefaultsFile = Join-Path $tmpDir "my.ini.backup"
$serviceConfigDir = Split-Path -Parent $DefaultsFile
$initFile = Join-Path $serviceConfigDir "topracing-init.sql"
$mysql = Join-Path $MysqlBin "mysql.exe"

New-Item -ItemType Directory -Force -Path $tmpDir | Out-Null
if( Test-Path $initFile ) {
  Remove-Item -Path $initFile -Force
}

@"
CREATE USER IF NOT EXISTS 'root'@'localhost' IDENTIFIED BY 'root';
ALTER USER 'root'@'localhost' IDENTIFIED BY 'root';
CREATE USER IF NOT EXISTS 'admin'@'localhost' IDENTIFIED BY 'admin';
ALTER USER 'admin'@'localhost' IDENTIFIED BY 'admin';
CREATE USER IF NOT EXISTS 'admin'@'127.0.0.1' IDENTIFIED BY 'admin';
ALTER USER 'admin'@'127.0.0.1' IDENTIFIED BY 'admin';
GRANT ALL PRIVILEGES ON *.* TO 'admin'@'localhost' WITH GRANT OPTION;
GRANT ALL PRIVILEGES ON *.* TO 'admin'@'127.0.0.1' WITH GRANT OPTION;
FLUSH PRIVILEGES;
"@ | Set-Content -Path $initFile -Encoding ascii

function Stop-MySqlProcesses {
  Get-Process -Name "mysqld" -ErrorAction SilentlyContinue | Stop-Process -Force
}

function Stop-MySqlService {
  $service = Get-Service -Name $ServiceName -ErrorAction Stop
  if( $service.Status -ne "Stopped" ) {
    Stop-Service -Name $ServiceName -Force
    $service.WaitForStatus( "Stopped",
                            [TimeSpan]::FromSeconds( 60 ) )
  }
}

function Start-MySqlServiceAndWait {
  Start-Service -Name $ServiceName
  ( Get-Service -Name $ServiceName ).WaitForStatus( "Running",
                                                   [TimeSpan]::FromSeconds( 60 ) )
}

function Wait-MySqlLogin( [string[]]$Arguments,
                          [string]$FailureMessage ) {
  for( $i = 0; $i -lt 30; $i++ ) {
    Start-Sleep -Seconds 2
    & $mysql @Arguments *> $null
    if( $LASTEXITCODE -eq 0 ) {
      return
    }
  }
  throw $FailureMessage
}

function Add-InitFileToDefaults( [string]$OriginalConfig ) {
  $normalizedLines = $OriginalConfig -split "`r?`n" | Where-Object {
    $_ -notmatch '^\s*init_file\s*='
  }
  $resultLines = New-Object System.Collections.Generic.List[string]
  $inserted = $false
  foreach( $line in $normalizedLines ) {
    $resultLines.Add( $line )
    if( $line.Trim() -eq "[mysqld]" ) {
      $resultLines.Add( "init_file=`"$($initFile.Replace( '\',
                                                          '/' ))`"" )
      $inserted = $true
    }
  }
  if( -not $inserted ) {
    throw "No se encontró la sección [mysqld] en $DefaultsFile."
  }
  return ( $resultLines -join "`r`n" )
}

Stop-MySqlService
Stop-MySqlProcesses

$originalDefaults = Get-Content -Path $DefaultsFile -Raw
$originalDefaults | Set-Content -Path $backupDefaultsFile -Encoding ascii
$modifiedDefaults = Add-InitFileToDefaults -OriginalConfig $originalDefaults
$serviceRestored = $false

try {
  $modifiedDefaults | Set-Content -Path $DefaultsFile -Encoding ascii
  Start-MySqlServiceAndWait
  Wait-MySqlLogin -Arguments @( "--protocol=tcp",
                                "-uroot",
                                "-proot",
                                "-e",
                                "SELECT 1" ) `
                  -FailureMessage "No fue posible validar la cuenta root tras el arranque con init_file."
  Wait-MySqlLogin -Arguments @( "--protocol=tcp",
                                "-uadmin",
                                "-padmin",
                                "-e",
                                "SELECT 1" ) `
                  -FailureMessage "No fue posible validar la cuenta admin/admin tras el arranque con init_file."
} finally {
  Stop-MySqlService
  Stop-MySqlProcesses
  $originalDefaults | Set-Content -Path $DefaultsFile -Encoding ascii
  if( Test-Path $initFile ) {
    Remove-Item -Path $initFile -Force
  }
  Start-MySqlServiceAndWait
  $serviceRestored = $true
}

if( -not $serviceRestored ) {
  throw "No fue posible restaurar el servicio MySQL."
}

Wait-MySqlLogin -Arguments @( "--protocol=tcp",
                              "-uadmin",
                              "-padmin",
                              "-e",
                              "SELECT 1" ) `
                -FailureMessage "No fue posible autenticar con admin/admin después de restaurar el servicio."

& $mysql --protocol=tcp -uadmin -padmin -e "CREATE DATABASE IF NOT EXISTS topracing26 CHARACTER SET utf8;"
if( $LASTEXITCODE -ne 0 ) {
  throw "No fue posible crear la base topracing26."
}

cmd /c "`"$mysql`" --protocol=tcp -uadmin -padmin < `"$DumpFile`""
if( $LASTEXITCODE -ne 0 ) {
  throw "La importación de $DumpFile falló."
}

Write-Output "MySQL local listo con usuario admin/admin y dump importado en topracing26."
