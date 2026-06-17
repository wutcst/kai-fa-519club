# 启动 F6 联机 Spring Boot 服务端（默认 8080）
# PowerShell 下请勿把 -D 参数裸写，易被拆成多个 token。

$ErrorActionPreference = "Stop"
Set-Location (Split-Path $PSScriptRoot -Parent)

$mvn = Join-Path $PSScriptRoot ".." ".tools" "apache-maven-3.9.9" "bin" "mvn.cmd"
if (-not (Test-Path $mvn)) {
    $mvn = "mvn"
}

Write-Host "Starting ServerApplication on http://localhost:8080 ..."
Write-Host "Vue portal: cd vue-portal; npm run dev -> http://localhost:5173"
Write-Host ""

& $mvn spring-boot:run
