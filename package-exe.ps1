# Builds the Windows app with a bundled Java runtime (no JDK needed on the target PC).
#
#   powershell -ExecutionPolicy Bypass -File package-exe.ps1              -> dist\DSA Progress Tracker\  (portable folder)
#   powershell -ExecutionPolicy Bypass -File package-exe.ps1 -Installer   -> also dist\DSA-Progress-Tracker-Setup-<ver>.exe
#
# The installer needs WiX Toolset 3.x on the build machine (GitHub Actions installs it for releases).
# It installs per-user (no admin rights), adds a Desktop shortcut + Start Menu entry, and upgrades in place.
param([switch]$Installer)
$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot

if (-not $env:JAVA_HOME) {
    $jdk = Get-ChildItem 'C:\Program Files\Java' -Directory -Filter 'jdk-*' | Sort-Object Name -Descending | Select-Object -First 1
    if (-not $jdk) { throw 'No JDK found. Install a JDK (17+) on the BUILD machine only.' }
    $env:JAVA_HOME = $jdk.FullName
}
$jpackage = Join-Path $env:JAVA_HOME 'bin\jpackage.exe'

# Single source of truth for the version: AppConstants.APP_VERSION
$version = [regex]::Match((Get-Content 'src\main\java\com\dsatracker\util\AppConstants.java' -Raw), 'APP_VERSION\s*=\s*"([^"]+)"').Groups[1].Value
if (-not $version) { throw 'Could not read APP_VERSION' }

& .\mvnw.cmd clean package -DskipTests
if ($LASTEXITCODE -ne 0) { throw 'Maven build failed' }

$dist = Join-Path $PSScriptRoot 'dist'
if (Test-Path $dist) { Remove-Item $dist -Recurse -Force }

$stage = Join-Path $PSScriptRoot 'target\jpackage-input'
if (Test-Path $stage) { Remove-Item $stage -Recurse -Force }
New-Item -ItemType Directory $stage | Out-Null
Copy-Item target\dsa-progress-tracker.jar $stage

$icon = Join-Path $PSScriptRoot 'src\main\resources\com\dsatracker\images\app-icon.ico'
$common = @('--name', 'DSA Progress Tracker', '--app-version', $version,
    '--input', $stage, '--main-jar', 'dsa-progress-tracker.jar', '--main-class', 'com.dsatracker.Main',
    '--icon', $icon, '--dest', $dist, '--vendor', 'DSA Tracker', '--java-options', '-Dfile.encoding=UTF-8')

& $jpackage --type app-image @common
if ($LASTEXITCODE -ne 0) { throw 'jpackage (app-image) failed' }

if ($Installer) {
    # Fixed UUID = Windows treats every new version as an upgrade of the same product.
    & $jpackage --type exe @common --win-shortcut --win-menu --win-menu-group 'DSA Progress Tracker' `
        --win-per-user-install --win-dir-chooser --win-upgrade-uuid '6f2b7c1e-4d3a-4b58-9e0a-2c7d5a91f3b4'
    if ($LASTEXITCODE -ne 0) { throw 'jpackage (installer) failed' }
    $setup = Join-Path $dist "DSA-Progress-Tracker-Setup-$version.exe"
    Move-Item (Join-Path $dist "DSA Progress Tracker-$version.exe") $setup
    Write-Host "Installer: $setup"
}

$exe = Join-Path $dist 'DSA Progress Tracker\DSA Progress Tracker.exe'
if (-not $env:CI) {
    $lnk = Join-Path ([Environment]::GetFolderPath('Desktop')) 'DSA Progress Tracker.lnk'
    $s = (New-Object -ComObject WScript.Shell).CreateShortcut($lnk)
    $s.TargetPath = $exe
    $s.WorkingDirectory = Split-Path $exe
    $s.Save()
    Write-Host "Shortcut: $lnk"
}
Write-Host "Done. App: $exe"
