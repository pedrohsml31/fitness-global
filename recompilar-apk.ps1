# Recompila o APK do Fitness Global a partir do index.html atual.
# Uso: botao direito -> "Executar com PowerShell", ou: powershell -File recompilar-apk.ps1
$ErrorActionPreference = "Stop"
$ProgressPreference = "SilentlyContinue"

$src    = $PSScriptRoot
$dev    = "$env:USERPROFILE\fitdev"
$native = "$dev\native"
$nodeDir = (Get-ChildItem "$env:LOCALAPPDATA\Microsoft\WinGet\Packages" -Recurse -Filter node.exe -ErrorAction SilentlyContinue | Select-Object -First 1).DirectoryName
if (-not $nodeDir) { Write-Host "Node nao encontrado." -ForegroundColor Red; exit 1 }

$env:PATH = "$nodeDir;$env:PATH"
$env:JAVA_HOME = "$dev\jdk\jdk-17.0.19+10"
$env:ANDROID_SDK_ROOT = "$dev\android"
$env:ANDROID_HOME = "$dev\android"

Write-Host "1/3  Copiando arquivos web para o projeto..." -ForegroundColor Cyan
$assets = "$native\android\app\src\main\assets\public"
foreach ($f in @("index.html","manifest.webmanifest","sw.js","icon.svg","version.json")) {
    if (Test-Path "$src\$f") {
        Copy-Item "$src\$f" "$native\www\$f" -Force
        Copy-Item "$src\$f" "$assets\$f" -Force
    }
}

Write-Host "2/3  Compilando o APK (pode levar alguns minutos)..." -ForegroundColor Cyan
Set-Location "$native\android"
& ".\gradlew.bat" assembleDebug --no-daemon | Out-Host
if ($LASTEXITCODE -ne 0) { Write-Host "Falha no build." -ForegroundColor Red; exit 1 }

Write-Host "3/3  Copiando APK para dist\..." -ForegroundColor Cyan
$apk = "$native\android\app\build\outputs\apk\debug\app-debug.apk"
New-Item -ItemType Directory -Force "$src\dist" | Out-Null
Copy-Item $apk "$src\dist\fitness-global.apk" -Force
Write-Host "PRONTO! APK em: $src\dist\fitness-global.apk" -ForegroundColor Green
