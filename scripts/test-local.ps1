param(
  [ValidateSet("unit", "verify-live", "deploy-and-verify-live", "browser-live", "deploy-and-browser-live", "full-live")]
  [string]$Mode = "unit",
  [string]$BaseUrl = "http://localhost:8080/topracingwebapp"
)

$candidates = @(
  "C:\Users\usuario\ownCloud2\tools\apache-maven-3.9.11\bin\mvn.cmd",
  "C:\Users\usuario\.vscode\extensions\oracle.oracle-java-25.0.1\nbcode\java\maven\bin\mvn.cmd"
)

$maven = $null
foreach ($candidate in $candidates) {
  if (Test-Path -LiteralPath $candidate) {
    $maven = $candidate
    break
  }
}

if (-not $maven) {
  $mavenCommand = Get-Command mvn -ErrorAction SilentlyContinue
  if ($mavenCommand) {
    $maven = $mavenCommand.Source
  }
}

if (-not $maven) {
  throw "No local Maven installation was found. Update scripts/test-local.ps1 with a valid mvn.cmd path."
}

$asadminCandidates = @(
  "C:\Users\usuario\ownCloud2\glassfish6\glassfish\bin\asadmin.bat",
  "C:\Users\usuario\ownCloud2\tools\glassfish7\glassfish\bin\asadmin.bat"
)

function Ensure-Playwright {
  if (-not (Test-Path -LiteralPath "node_modules\@playwright\test")) {
    npm install
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
  }

  $playwrightHome = Join-Path $env:LOCALAPPDATA "ms-playwright"
  $playwrightHomeExists = Test-Path -LiteralPath $playwrightHome -PathType Container
  $chromiumDir = $null
  if ($playwrightHomeExists) {
    $chromiumDir = Get-ChildItem $playwrightHome -Directory -ErrorAction SilentlyContinue |
      Where-Object { $_.Name -like 'chromium-*' } |
      Select-Object -First 1
  }
  $hasChromium = $playwrightHomeExists -and $null -ne $chromiumDir

  if (-not $hasChromium) {
    npx playwright install chromium
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
  }
}

function Resolve-Asadmin {
  foreach ($candidate in $asadminCandidates) {
    if (Test-Path -LiteralPath $candidate) {
      return $candidate
    }
  }

  throw "No local asadmin.bat was found. Update scripts/test-local.ps1 with a valid GlassFish path."
}

switch ($Mode) {
  "unit" {
    & $maven test
  }
  "verify-live" {
    & $maven verify "-Dtopracing.baseUrl=$BaseUrl"
  }
  "deploy-and-verify-live" {
    & $maven package -DskipTests
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

    $asadmin = Resolve-Asadmin
    & $asadmin deploy --force=true target\topracingwebapp
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

    & $maven failsafe:integration-test failsafe:verify "-Dtopracing.baseUrl=$BaseUrl"
  }
  "browser-live" {
    Ensure-Playwright
    $env:TOPRACING_BASE_URL = $BaseUrl
    npx playwright test
  }
  "deploy-and-browser-live" {
    & $maven package -DskipTests
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

    $asadmin = Resolve-Asadmin
    & $asadmin deploy --force=true target\topracingwebapp
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

    Ensure-Playwright
    $env:TOPRACING_BASE_URL = $BaseUrl
    npx playwright test
  }
  "full-live" {
    & $maven verify "-Dtopracing.baseUrl=$BaseUrl"
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

    Ensure-Playwright
    $env:TOPRACING_BASE_URL = $BaseUrl
    npx playwright test
  }
}

exit $LASTEXITCODE
