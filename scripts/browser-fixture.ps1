param(
  [Parameter(Mandatory = $true)]
  [string]$Command,
  [string]$Argument
)

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
