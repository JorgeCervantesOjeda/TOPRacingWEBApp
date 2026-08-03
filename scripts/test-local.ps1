<# scripts/test-local.ps1
   Runs local unit, integration, deployment, and browser test modes. #>
param(
  [ValidateSet("unit", "verify-live", "deploy-and-verify-live", "browser-live", "deploy-and-browser-live", "full-live")]
  [string]$Mode = "unit",
  [string]$BaseUrl = "http://localhost:8080/topracingwebapp",
  [string]$TestDbName = "topracing26_test"
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
  $env:TOPRACING_ASADMIN,
  $(if ($env:TOPRACING_GLASSFISH_HOME) {
      Join-Path $env:TOPRACING_GLASSFISH_HOME "bin\asadmin.bat"
    }),
  "C:\Users\usuario\ownCloud2\tools\glassfish7\glassfish\bin\asadmin.bat",
  "C:\Users\usuario\ownCloud2\glassfish6\glassfish\bin\asadmin.bat"
) | Where-Object { $_ }

$javaHomeCandidates = @(
  $env:TOPRACING_JAVA_HOME,
  "C:\Program Files\Eclipse Adoptium\jdk-17.0.18.8-hotspot",
  "C:\Program Files\Eclipse Adoptium\jdk-21.0.10.7-hotspot",
  $env:JAVA_HOME
)

$testDbUrl = "jdbc:mysql://localhost:3306/${TestDbName}?zeroDateTimeBehavior=convertToNull&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Mexico_City"
$domainName = if ($env:TOPRACING_GF_DOMAIN) {
  $env:TOPRACING_GF_DOMAIN
} else {
  "topracing"
}

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

function Resolve-JavaHome {
  foreach ($candidate in $javaHomeCandidates) {
    if ($candidate -and (Test-Path -LiteralPath (Join-Path $candidate "bin\java.exe"))) {
      return $candidate
    }
  }

  throw "No local JDK was found. Set TOPRACING_JAVA_HOME to a JDK 11 or newer."
}

function Set-GlassFishJavaEnvironment {
  $jdkHome = Resolve-JavaHome
  $env:JAVA_HOME = $jdkHome
  $env:AS_JAVA = $jdkHome
  $env:Path = "$jdkHome\bin;$env:Path"
}

function Set-IsolatedDbEnvironment {
  $env:TOPRACING_DB_URL = $testDbUrl
  $env:TOPRACING_DB_CATALOG = $TestDbName
  $env:TOPRACING_DB_USERNAME = "admin"
  $env:TOPRACING_DB_PASSWORD = "admin"
}

function Prepare-IsolatedDb {
  Set-IsolatedDbEnvironment
  powershell -ExecutionPolicy Bypass -File "$PSScriptRoot\prepare-test-db.ps1" -TargetDb $TestDbName
  if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
}

function Restart-GlassFishForTests {
  param([string]$Asadmin)

  Set-IsolatedDbEnvironment
  Set-GlassFishJavaEnvironment
  & $Asadmin stop-domain $domainName *> $null
  & $Asadmin start-domain $domainName
  if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
}

function Package-And-Deploy {
  param([string]$Asadmin)

  & $maven package -DskipTests
  if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

  & $Asadmin deploy --force=true target\topracingwebapp
  if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
}

function Prepare-LiveEnvironment {
  Prepare-IsolatedDb
  $asadmin = Resolve-Asadmin
  Restart-GlassFishForTests -Asadmin $asadmin
  Package-And-Deploy -Asadmin $asadmin
}

switch ($Mode) {
  "unit" {
    & $maven test
  }
  "verify-live" {
    Prepare-LiveEnvironment
    & $maven verify "-Dtopracing.baseUrl=$BaseUrl"
  }
  "deploy-and-verify-live" {
    Prepare-LiveEnvironment
    & $maven failsafe:integration-test failsafe:verify "-Dtopracing.baseUrl=$BaseUrl"
  }
  "browser-live" {
    Prepare-LiveEnvironment
    Ensure-Playwright
    $env:TOPRACING_BASE_URL = $BaseUrl
    npx playwright test
  }
  "deploy-and-browser-live" {
    Prepare-LiveEnvironment
    Ensure-Playwright
    $env:TOPRACING_BASE_URL = $BaseUrl
    npx playwright test
  }
  "full-live" {
    Prepare-LiveEnvironment
    & $maven verify "-Dtopracing.baseUrl=$BaseUrl"
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

    Ensure-Playwright
    $env:TOPRACING_BASE_URL = $BaseUrl
    npx playwright test
  }
}

exit $LASTEXITCODE
