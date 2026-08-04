<# scripts/sync-glassfish-truststore.ps1
   Keeps the local GlassFish domain truststore aligned with the configured JDK. #>
param(
  [Parameter(Mandatory = $true)]
  [string]$JdkHome,
  [Parameter(Mandatory = $true)]
  [string]$GlassfishHome,
  [Parameter(Mandatory = $true)]
  [string]$DomainName
)

$sourceTrustStore = Join-Path $JdkHome "lib\security\cacerts"
$targetTrustStore = Join-Path $GlassfishHome "domains\$DomainName\config\cacerts.jks"

if( -not ( Test-Path -LiteralPath $sourceTrustStore ) ) {
  throw "JDK truststore not found: $sourceTrustStore"
}

$targetExists = Test-Path -LiteralPath $targetTrustStore
$shouldCopy = -not $targetExists

if( $targetExists ) {
  $sourceInfo = Get-Item -LiteralPath $sourceTrustStore
  $targetInfo = Get-Item -LiteralPath $targetTrustStore
  $shouldCopy = $targetInfo.Length -lt 50000 -and
                $targetInfo.Length -lt $sourceInfo.Length
}

if( -not $shouldCopy ) {
  return
}

$targetDirectory = Split-Path -Parent $targetTrustStore
if( -not ( Test-Path -LiteralPath $targetDirectory ) ) {
  throw "GlassFish domain config directory not found: $targetDirectory"
}

if( $targetExists ) {
  $timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
  $backupTrustStore = "$targetTrustStore.before-jdk-sync-$timestamp"
  Copy-Item -LiteralPath $targetTrustStore -Destination $backupTrustStore -Force
  Write-Host "Backed up GlassFish truststore to $backupTrustStore"
}

Copy-Item -LiteralPath $sourceTrustStore -Destination $targetTrustStore -Force
Write-Host "Synchronized GlassFish truststore from JDK cacerts."
