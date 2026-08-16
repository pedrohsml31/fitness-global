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
foreach ($f in @("index.html","manifest.webmanifest","sw.js","icon.svg","version.json","taco.json")) {
    if (Test-Path "$src\$f") {
        Copy-Item "$src\$f" "$native\www\$f" -Force
        Copy-Item "$src\$f" "$assets\$f" -Force
    }
}

# O plugin capacitor-health foi modificado (le peso e marca atividades automaticas).
# Um "npm install" desfaz isso, por isso a copia oficial vive em native-patch\.
$hp = "$native\node_modules\capacitor-health\android\src\main\java\com\fit_up\health\capacitor\HealthPlugin.kt"
if ((Test-Path "$src\native-patch\HealthPlugin.kt") -and (Test-Path $hp)) {
    Copy-Item "$src\native-patch\HealthPlugin.kt" $hp -Force
}

# Os plugins nativos proprios (canal de descanso, fone, Nao Perturbe) tambem moram em
# native-patch. Sem esta copia, mexer neles nao chegava ao APK.
$javaDir = "$native\android\app\src\main\java\com\pedro\fitnessglobal"
foreach ($p in @("RestChannelPlugin.java","HeadsetPlugin.java")) {
    if ((Test-Path "$src\native-patch\$p") -and (Test-Path $javaDir)) {
        Copy-Item "$src\native-patch\$p" "$javaDir\$p" -Force
    }
}

# O manifesto declara as permissoes de saude (peso, gordura corporal). Sem esta copia,
# uma permissao nova em native-patch nunca chega ao APK.
$mf = "$native\android\app\src\main\AndroidManifest.xml"
if ((Test-Path "$src\native-patch\AndroidManifest.xml") -and (Test-Path $mf)) {
    Copy-Item "$src\native-patch\AndroidManifest.xml" $mf -Force
}

# A versao mora SO no index.html (APP_NAME_VERSION + APP_VERSION) e e escrita daqui para
# todos os outros lugares: nome do app no celular, titulo da activity, versionName e
# versionCode do Android. Assim nao tem como um deles ficar para tras.
$idx  = Get-Content "$src\index.html" -Raw
$vName = [regex]::Match($idx, 'APP_NAME_VERSION\s*=\s*"([^"]+)"').Groups[1].Value
$vCode = [regex]::Match($idx, 'APP_VERSION\s*=\s*(\d+)').Groups[1].Value
if (-not $vName -or -not $vCode) { Write-Host "Nao achei a versao no index.html." -ForegroundColor Red; exit 1 }
$rotulo = "Fitness Global v$vName"
Write-Host "     versao: $rotulo (build $vCode)" -ForegroundColor DarkGray

$strings = "$native\android\app\src\main\res\values\strings.xml"
$sx = Get-Content $strings -Raw
$sx = [regex]::Replace($sx, '(<string name="app_name">)[^<]*(</string>)', "`${1}$rotulo`${2}")
$sx = [regex]::Replace($sx, '(<string name="title_activity_main">)[^<]*(</string>)', "`${1}$rotulo`${2}")
# UTF-8 SEM BOM: o Set-Content -Encoding utf8 do PowerShell 5.1 grava BOM e o Gradle
# quebra com "Unexpected character: '?'" na primeira linha do build.gradle.
$semBom = New-Object System.Text.UTF8Encoding $false
[System.IO.File]::WriteAllText($strings, $sx, $semBom)

$gradle = "$native\android\app\build.gradle"
$gx = Get-Content $gradle -Raw
$gx = [regex]::Replace($gx, 'versionCode\s+\d+', "versionCode $vCode")
$gx = [regex]::Replace($gx, 'versionName\s+"[^"]*"', "versionName ""$vName""")
[System.IO.File]::WriteAllText($gradle, $gx, $semBom)

Write-Host "2/3  Compilando o APK (pode levar alguns minutos)..." -ForegroundColor Cyan
Set-Location "$native\android"
& ".\gradlew.bat" assembleDebug --no-daemon | Out-Host
if ($LASTEXITCODE -ne 0) { Write-Host "Falha no build." -ForegroundColor Red; exit 1 }

Write-Host "3/3  Copiando APK para dist\..." -ForegroundColor Cyan
$apk = "$native\android\app\build\outputs\apk\debug\app-debug.apk"
New-Item -ItemType Directory -Force "$src\dist" | Out-Null
Copy-Item $apk "$src\dist\fitness-global.apk" -Force
Write-Host "PRONTO! APK em: $src\dist\fitness-global.apk" -ForegroundColor Green
