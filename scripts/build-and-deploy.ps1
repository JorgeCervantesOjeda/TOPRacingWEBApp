$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $PSScriptRoot
$localEnv = Join-Path $PSScriptRoot "local-env.ps1"
if( Test-Path -LiteralPath $localEnv ) {
  . $localEnv
}

$jdkHome = if( $env:TOPRACING_JAVA_HOME ) {
  $env:TOPRACING_JAVA_HOME
} else {
  "C:\Program Files\Eclipse Adoptium\jdk-17.0.18.8-hotspot"
}

$mavenHome = if( $env:TOPRACING_MAVEN_HOME ) {
  $env:TOPRACING_MAVEN_HOME
} else {
  "C:\Users\usuario\ownCloud2\tools\apache-maven-3.9.11"
}

$glassfishHome = if( $env:TOPRACING_GLASSFISH_HOME ) {
  $env:TOPRACING_GLASSFISH_HOME
} else {
  "C:\Users\usuario\ownCloud2\tools\glassfish7\glassfish"
}

$domainName = if( $env:TOPRACING_GF_DOMAIN ) {
  $env:TOPRACING_GF_DOMAIN
} else {
  "topracing"
}

$env:JAVA_HOME = $jdkHome
$env:AS_JAVA = $jdkHome
$env:Path = "$jdkHome\bin;C:\Windows\System32;C:\Windows"

& "$PSScriptRoot\sync-glassfish-truststore.ps1" `
  -JdkHome $jdkHome `
  -GlassfishHome $glassfishHome `
  -DomainName $domainName

Push-Location $projectRoot
try {
  & "$mavenHome\bin\mvn.cmd" clean package -DskipTests
  & "$glassfishHome\bin\asadmin.bat" start-domain $domainName
  & "$glassfishHome\bin\asadmin.bat" deploy --force=true "$projectRoot\target\topracingwebapp.war"
} finally {
  Pop-Location
}
