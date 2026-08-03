# scripts/configure-cloudflare-named-tunnel.ps1
# Creates a named Cloudflare Tunnel and routes top-racing.org to the local app.

$ErrorActionPreference = "Stop"

$TunnelName = if( $env:TOPRACING_CLOUDFLARE_TUNNEL_NAME ) {
  $env:TOPRACING_CLOUDFLARE_TUNNEL_NAME
} else {
  "top-racing-web"
}

$Hostname = if( $env:TOPRACING_PUBLIC_HOSTNAME ) {
  $env:TOPRACING_PUBLIC_HOSTNAME
} else {
  "top-racing.org"
}

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

$cloudflaredHome = Join-Path $env:USERPROFILE ".cloudflared"
$certPath = Join-Path $cloudflaredHome "cert.pem"
if( -not ( Test-Path $certPath ) ) {
  throw "Cloudflare origin certificate was not found at $certPath. Run 'cloudflared tunnel login' first."
}

try {
  $response = Invoke-WebRequest -UseBasicParsing -Uri "$LocalUrl/topracingwebapp/faces/welcome.xhtml" -TimeoutSec 30
  if( $response.StatusCode -ne 200 ) {
    throw "Unexpected local status code $($response.StatusCode)."
  }
} catch {
  throw "TOP-Racing is not responding locally at $LocalUrl/topracingwebapp/faces/welcome.xhtml. Cause: $($_.Exception.Message)"
}

$existingTunnels = & $cloudflaredPath tunnel list 2>&1
if( $LASTEXITCODE -ne 0 ) {
  throw "Could not list Cloudflare tunnels. Output: $existingTunnels"
}

if( $existingTunnels -notmatch [regex]::Escape( $TunnelName ) ) {
  & $cloudflaredPath tunnel create $TunnelName
  if( $LASTEXITCODE -ne 0 ) {
    throw "Could not create Cloudflare tunnel '$TunnelName'."
  }
}

$tunnelInfo = & $cloudflaredPath tunnel list 2>&1
$tunnelLine = $tunnelInfo | Where-Object { $_ -match "\s$([regex]::Escape( $TunnelName ))\s" -or $_ -match "^[-a-f0-9]{36}\s+$([regex]::Escape( $TunnelName ))" } | Select-Object -First 1
if( -not $tunnelLine ) {
  throw "Tunnel '$TunnelName' was not found after creation. Output: $tunnelInfo"
}

$tunnelIdMatch = [regex]::Match( $tunnelLine, "[0-9a-fA-F-]{36}" )
if( -not $tunnelIdMatch.Success ) {
  throw "Could not parse tunnel id from: $tunnelLine"
}

$tunnelId = $tunnelIdMatch.Value
$credentialsPath = Join-Path $cloudflaredHome "$tunnelId.json"
if( -not ( Test-Path $credentialsPath ) ) {
  throw "Cloudflare tunnel credentials were not found at $credentialsPath."
}

$configPath = Join-Path $cloudflaredHome "top-racing-config.yml"
$config = @"
tunnel: $tunnelId
credentials-file: $credentialsPath

ingress:
  - hostname: $Hostname
    service: $LocalUrl
  - hostname: www.$Hostname
    service: $LocalUrl
  - service: http_status:404
"@
Set-Content -Path $configPath -Encoding ascii -Value $config

& $cloudflaredPath tunnel route dns $TunnelName $Hostname
if( $LASTEXITCODE -ne 0 ) {
  throw "Could not route $Hostname to tunnel '$TunnelName'. Confirm the domain is active in Cloudflare."
}

& $cloudflaredPath tunnel route dns $TunnelName "www.$Hostname"
if( $LASTEXITCODE -ne 0 ) {
  Write-Warning "Could not route www.$Hostname. The apex hostname may still work."
}

Write-Output "Configured Cloudflare tunnel '$TunnelName' ($tunnelId)."
Write-Output "Config: $configPath"
Write-Output "Run tunnel with:"
Write-Output "`"$cloudflaredPath`" tunnel --config `"$configPath`" run $TunnelName"
