# scripts/retry-oci-free-vm.ps1
# Retries creation of an Oracle Cloud Always Free VM in the configured tenancy.

$ErrorActionPreference = "Stop"

$OciPath = if( $env:TOPRACING_OCI_CLI ) {
  $env:TOPRACING_OCI_CLI
} else {
  "C:\Program Files (x86)\Oracle\oci_cli\oci.exe"
}

if( -not ( Test-Path $OciPath ) ) {
  throw "OCI CLI was not found at $OciPath."
}

$ConfigPath = Join-Path $env:USERPROFILE ".oci\config"
if( -not ( Test-Path $ConfigPath ) ) {
  throw "OCI CLI config was not found at $ConfigPath."
}

$configText = Get-Content -Raw $ConfigPath
$TenancyId = if( $env:TOPRACING_OCI_COMPARTMENT_ID ) {
  $env:TOPRACING_OCI_COMPARTMENT_ID
} else {
  ( [regex]::Match( $configText, "(?m)^tenancy=(.+)$" ) ).Groups[1].Value.Trim()
}

if( -not $TenancyId ) {
  throw "No compartment id was provided and tenancy was not found in OCI config."
}

$AvailabilityDomain = if( $env:TOPRACING_OCI_AD ) {
  $env:TOPRACING_OCI_AD
} else {
  "fbWc:MX-QUERETARO-1-AD-1"
}

$SubnetDisplayName = if( $env:TOPRACING_OCI_SUBNET_NAME ) {
  $env:TOPRACING_OCI_SUBNET_NAME
} else {
  "top-racing-public-subnet"
}

$SshPublicKeyPath = if( $env:TOPRACING_OCI_SSH_PUBLIC_KEY ) {
  $env:TOPRACING_OCI_SSH_PUBLIC_KEY
} else {
  Join-Path $env:USERPROFILE ".ssh\top_racing_oci_ed25519.pub"
}

if( -not ( Test-Path $SshPublicKeyPath ) ) {
  throw "SSH public key was not found at $SshPublicKeyPath."
}

$ArmImageId = if( $env:TOPRACING_OCI_ARM_IMAGE_ID ) {
  $env:TOPRACING_OCI_ARM_IMAGE_ID
} else {
  "ocid1.image.oc1.mx-queretaro-1.aaaaaaaaemveyi7xmiwngbxqci35v6cfqcbyq2tbicn55cll7dlbu7a6gola"
}

$X86ImageId = if( $env:TOPRACING_OCI_X86_IMAGE_ID ) {
  $env:TOPRACING_OCI_X86_IMAGE_ID
} else {
  "ocid1.image.oc1.mx-queretaro-1.aaaaaaaacgoi3kyngyzzkbmxh5nhk2wuxcecav7dutbwdnjyqhivshv6xbqa"
}

$env:SUPPRESS_LABEL_WARNING = "True"

function Invoke-OciJson {
  param( [string[]] $Arguments )

  $raw = & $OciPath @Arguments --output json
  if( $LASTEXITCODE -ne 0 ) {
    throw "OCI command failed: $($Arguments -join ' ')"
  }
  return $raw | ConvertFrom-Json
}

function Get-SubnetId {
  $vcns = Invoke-OciJson @( "network", "vcn", "list", "--compartment-id", $TenancyId, "--all" )
  foreach( $vcn in $vcns.data ) {
    $subnets = Invoke-OciJson @( "network", "subnet", "list", "--compartment-id", $TenancyId, "--vcn-id", $vcn.id, "--all" )
    $subnet = $subnets.data | Where-Object { $_."display-name" -eq $SubnetDisplayName } | Select-Object -First 1
    if( $subnet ) {
      return $subnet.id
    }
  }
  throw "Subnet '$SubnetDisplayName' was not found."
}

function New-LaunchFiles {
  param(
    [decimal] $Ocpus,
    [decimal] $MemoryInGbs
  )

  $sshPublicKey = ( Get-Content $SshPublicKeyPath -Raw ).Trim()
  $shapeConfigPath = Join-Path $env:TEMP "top-racing-oci-shape.json"
  $metadataPath = Join-Path $env:TEMP "top-racing-oci-metadata.json"
  Set-Content -Path $shapeConfigPath -Encoding ascii -Value ( @{ ocpus = $Ocpus; memoryInGBs = $MemoryInGbs } | ConvertTo-Json -Compress )
  Set-Content -Path $metadataPath -Encoding ascii -Value ( @{ ssh_authorized_keys = $sshPublicKey } | ConvertTo-Json -Compress )
  return @{ ShapeConfigPath = $shapeConfigPath; MetadataPath = $metadataPath }
}

function Try-LaunchInstance {
  param(
    [string] $DisplayName,
    [string] $Shape,
    [string] $ImageId,
    [string] $FaultDomain,
    [string] $SubnetId,
    [string] $ShapeConfigPath,
    [string] $MetadataPath
  )

  Write-Output "Trying $Shape in $FaultDomain..."
  $arguments = @(
    "compute", "instance", "launch",
    "--compartment-id", $TenancyId,
    "--availability-domain", $AvailabilityDomain,
    "--fault-domain", $FaultDomain,
    "--display-name", $DisplayName,
    "--shape", $Shape,
    "--image-id", $ImageId,
    "--subnet-id", $SubnetId,
    "--assign-public-ip", "true",
    "--metadata", "file://$MetadataPath",
    "--wait-for-state", "RUNNING",
    "--max-wait-seconds", "900",
    "--wait-interval-seconds", "20",
    "--output", "json"
  )

  if( $Shape -eq "VM.Standard.A1.Flex" ) {
    $arguments += @( "--shape-config", "file://$ShapeConfigPath" )
  }

  & $OciPath @arguments
  return $LASTEXITCODE -eq 0
}

function Get-RunningTopRacingInstance {
  $instances = Invoke-OciJson @( "compute", "instance", "list", "--compartment-id", $TenancyId, "--all" )
  return $instances.data |
    Where-Object {
      $_."display-name" -like "top-racing-server*" -and
      $_."lifecycle-state" -notin @( "TERMINATED", "TERMINATING" )
    } |
    Select-Object -First 1
}

$existingInstance = Get-RunningTopRacingInstance
if( $existingInstance ) {
  Write-Output "A TOP-Racing Oracle instance already exists: $($existingInstance.'display-name') state=$($existingInstance.'lifecycle-state') id=$($existingInstance.id)"
  exit 0
}

$subnetId = Get-SubnetId
$launchFiles = New-LaunchFiles -Ocpus 1 -MemoryInGbs 2
$faultDomains = @( "FAULT-DOMAIN-1", "FAULT-DOMAIN-2", "FAULT-DOMAIN-3" )

foreach( $faultDomain in $faultDomains ) {
  if( Try-LaunchInstance -DisplayName "top-racing-server-a1-$($faultDomain.ToLower())" -Shape "VM.Standard.A1.Flex" -ImageId $ArmImageId -FaultDomain $faultDomain -SubnetId $subnetId -ShapeConfigPath $launchFiles.ShapeConfigPath -MetadataPath $launchFiles.MetadataPath ) {
    Write-Output "Created an A1 Always Free candidate in $faultDomain."
    exit 0
  }
}

foreach( $faultDomain in $faultDomains ) {
  if( Try-LaunchInstance -DisplayName "top-racing-server-micro-$($faultDomain.ToLower())" -Shape "VM.Standard.E2.1.Micro" -ImageId $X86ImageId -FaultDomain $faultDomain -SubnetId $subnetId -ShapeConfigPath $launchFiles.ShapeConfigPath -MetadataPath $launchFiles.MetadataPath ) {
    Write-Output "Created an E2 Micro Always Free candidate in $faultDomain."
    exit 0
  }
}

throw "Oracle returned no Always Free capacity in the tested fault domains. Retry later."
