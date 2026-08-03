<# scripts/browser-fixture.ps1
   Emits browser-test fixture data through the Maven integration fixture CLI. #>
param(
  [Parameter(Mandatory = $true)]
  [string]$Command,
  [string]$Argument,
  [string]$TestDbName = "topracing26_test"
)

if (-not $env:TOPRACING_DB_URL) {
  $env:TOPRACING_DB_URL = "jdbc:mysql://localhost:3306/${TestDbName}?zeroDateTimeBehavior=convertToNull&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Mexico_City"
}
if (-not $env:TOPRACING_DB_CATALOG) {
  $env:TOPRACING_DB_CATALOG = $TestDbName
}
if (-not $env:TOPRACING_DB_USERNAME) {
  $env:TOPRACING_DB_USERNAME = "admin"
}
if (-not $env:TOPRACING_DB_PASSWORD) {
  $env:TOPRACING_DB_PASSWORD = "admin"
}

if (-not $env:TOPRACING_JAVA_HOME) {
  $env:TOPRACING_JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.10.7-hotspot"
}
if (Test-Path -LiteralPath (Join-Path $env:TOPRACING_JAVA_HOME "bin\java.exe")) {
  $env:JAVA_HOME = $env:TOPRACING_JAVA_HOME
  $env:Path = "$($env:TOPRACING_JAVA_HOME)\bin;$env:Path"
}

$candidates = @(
  $env:TOPRACING_MAVEN,
  "C:\Users\usuario\ownCloud2\tools\apache-maven-3.9.11\bin\mvn.cmd",
  "C:\Users\usuario\.vscode\extensions\oracle.oracle-java-25.0.1\nbcode\java\maven\bin\mvn.cmd"
) | Where-Object { $_ }

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
  throw "No local Maven installation was found."
}

$execArgs = $Command
if ($Argument) {
  $execArgs += " " + $Argument
}

& $maven '-q' 'test-compile' 'exec:java' '-Dexec.classpathScope=test' '-Dexec.mainClass=integration.BrowserFixtureCli' "-Dexec.args=$execArgs"
exit $LASTEXITCODE
