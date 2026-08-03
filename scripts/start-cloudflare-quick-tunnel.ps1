# scripts/start-cloudflare-quick-tunnel.ps1
# Starts a temporary Cloudflare Quick Tunnel to the local TOP-Racing app.

$ErrorActionPreference = "Stop"

$LocalUrl = if( $env:TOPRACING_LOCAL_URL ) {
  $env:TOPRACING_LOCAL_URL
} else {
  "http://localhost:8080"
}

$cloudflaredCandidates = @(
  "C:\Program Files (x86)\cloudflared\cloudflared.exe",
  "C:\Program Files\cloudflared\cloudflared.exe",
  "C:\Program Files\Cloudflare\cloudflared.exe",
  "C:\Program Files (x86)\Cloudflare\cloudflared.exe"
)

$cloudflaredCommand = Get-Command cloudflared -ErrorAction SilentlyContinue
if( $cloudflaredCommand ) {
  $cloudflaredPath = $cloudflaredCommand.Source
} else {
  $cloudflaredPath = $cloudflaredCandidates | Where-Object { Test-Path $_ } | Select-Object -First 1
}

if( -not $cloudflaredPath ) {
  throw "cloudflared was not found. Install it with: winget install --id Cloudflare.cloudflared --exact"
}

try {
  $response = Invoke-WebRequest -UseBasicParsing -Uri "$LocalUrl/topracingwebapp/faces/welcome.xhtml" -TimeoutSec 30
  if( $response.StatusCode -ne 200 ) {
    throw "Unexpected local status code $($response.StatusCode)."
  }
} catch {
  throw "TOP-Racing is not responding locally at $LocalUrl/topracingwebapp/faces/welcome.xhtml. Cause: $($_.Exception.Message)"
}

Write-Output "Starting Cloudflare Quick Tunnel to $LocalUrl"
Write-Output "Keep this window open while the public URL is needed."
& $cloudflaredPath tunnel --url $LocalUrl
