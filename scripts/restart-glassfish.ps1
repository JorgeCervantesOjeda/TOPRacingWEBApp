$ErrorActionPreference = "Stop"

$jdkHome = if( $env:TOPRACING_JAVA_HOME ) {
  $env:TOPRACING_JAVA_HOME
} else {
  "C:\Program Files\Eclipse Adoptium\jdk-17.0.18.8-hotspot"
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

& "$glassfishHome\bin\asadmin.bat" stop-domain $domainName
& "$glassfishHome\bin\asadmin.bat" start-domain $domainName
