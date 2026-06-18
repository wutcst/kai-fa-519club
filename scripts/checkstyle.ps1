# Local Checkstyle check (no Maven required, Java only)
# Usage: powershell -ExecutionPolicy Bypass -File scripts/checkstyle.ps1

$ErrorActionPreference = "Stop"
$ProjectRoot = Split-Path $PSScriptRoot -Parent
$ToolsDir = Join-Path $ProjectRoot ".tools"
$JarPath = Join-Path $ToolsDir "checkstyle-9.3-all.jar"
$ConfigPath = Join-Path $ProjectRoot "checkstyle.xml"
$SourcePath = Join-Path $ProjectRoot "src"

if (-not (Get-Command java -ErrorAction SilentlyContinue)) {
    Write-Error "Java not found. Install JDK and add it to PATH."
}

if (-not (Test-Path $JarPath)) {
    New-Item -ItemType Directory -Force -Path $ToolsDir | Out-Null
    $Url = "https://github.com/checkstyle/checkstyle/releases/download/checkstyle-9.3/checkstyle-9.3-all.jar"
    Write-Host "Downloading Checkstyle 9.3 (same version as CI)..."
    Invoke-WebRequest -Uri $Url -OutFile $JarPath -UseBasicParsing
}

Write-Host "Running Checkstyle on: $SourcePath"
java -jar $JarPath -c $ConfigPath -f plain $SourcePath
exit $LASTEXITCODE
