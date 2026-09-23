# Builds a standalone Windows app (bundled Java runtime, no JDK needed on the target PC)
# and puts a "DSA Progress Tracker" shortcut on the Desktop.
# Usage:  powershell -ExecutionPolicy Bypass -File package-exe.ps1
$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot

if (-not $env:JAVA_HOME) {
    $jdk = Get-ChildItem 'C:\Program Files\Java' -Directory -Filter 'jdk-*' | Sort-Object Name -Descending | Select-Object -First 1
    if (-not $jdk) { throw 'No JDK found. Install a JDK (17+) on the BUILD machine only.' }
    $env:JAVA_HOME = $jdk.FullName
}
$jpackage = Join-Path $env:JAVA_HOME 'bin\jpackage.exe'

& .\mvnw.cmd clean package -DskipTests
if ($LASTEXITCODE -ne 0) { throw 'Maven build failed' }

$dist = Join-Path $PSScriptRoot 'dist'
if (Test-Path $dist) { Remove-Item $dist -Recurse -Force }

$input = Join-Path $PSScriptRoot 'target\jpackage-input'
if (Test-Path $input) { Remove-Item $input -Recurse -Force }
New-Item -ItemType Directory $input | Out-Null
Copy-Item target\dsa-progress-tracker.jar $input

& $jpackage --type app-image --name 'DSA Progress Tracker' --app-version 1.0.0 `
    --input $input --main-jar dsa-progress-tracker.jar --main-class com.dsatracker.Main `
    --dest $dist --vendor 'DSA Tracker' --java-options '-Dfile.encoding=UTF-8'
if ($LASTEXITCODE -ne 0) { throw 'jpackage failed' }

$exe = Join-Path $dist 'DSA Progress Tracker\DSA Progress Tracker.exe'
$lnk = Join-Path ([Environment]::GetFolderPath('Desktop')) 'DSA Progress Tracker.lnk'
$s = (New-Object -ComObject WScript.Shell).CreateShortcut($lnk)
$s.TargetPath = $exe
$s.WorkingDirectory = Split-Path $exe
$s.Save()
Write-Host "Done. App: $exe"
Write-Host "Shortcut: $lnk"
