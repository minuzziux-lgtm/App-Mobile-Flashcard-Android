<#
.SYNOPSIS
  Compila APK debug conectado exclusivamente ao projeto Firebase mydeck-kotlin.
.DESCRIPTION
  Interrompe antes da compilação quando app/google-services.json não existe,
  pertence a outro projeto ou não inclui o applicationId Android do MyDeck.
  Este script não altera nem envia dados ao Firebase.
.EXAMPLE
  powershell -ExecutionPolicy Bypass -File .\scripts\build-firebase-apk.ps1
.EXAMPLE
  powershell -ExecutionPolicy Bypass -File .\scripts\build-firebase-apk.ps1 -Install
#>
param(
    [switch]$Install
)
$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

$root = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$configPath = Join-Path $root "app\google-services.json"
if (-not (Test-Path -LiteralPath $configPath -PathType Leaf)) {
    throw "Arquivo app\google-services.json ausente. Baixe-o do Firebase Console > Configuracoes do projeto > Seus aplicativos > Android e coloque-o na pasta app. Nada foi compilado."
}

try {
    $config = Get-Content -LiteralPath $configPath -Raw -Encoding UTF8 | ConvertFrom-Json -ErrorAction Stop
} catch {
    throw "O arquivo google-services.json nao e um JSON valido: $($_.Exception.Message)"
}

$projectId = [string]$config.project_info.project_id
if ($projectId -ne "mydeck-kotlin") {
    throw "Firebase incorreto: esperado mydeck-kotlin, encontrado '$projectId'. Nada foi compilado."
}

$packageName = "com.example.myapplication"
$matches = @($config.client | Where-Object {
    $_.client_info.android_client_info.package_name -eq $packageName
})
if ($matches.Count -eq 0) {
    throw "Este arquivo Firebase nao contem o aplicativo Android $packageName. Registre esse identificador no projeto mydeck-kotlin e baixe o JSON correto."
}

$gradlew = Join-Path $root "gradlew.bat"
if (-not (Test-Path -LiteralPath $gradlew -PathType Leaf)) {
    throw "gradlew.bat nao encontrado. Execute no projeto Android do MyDeck."
}

$apk = Join-Path $root "app\build\outputs\apk\debug\app-debug.apk"
$output = Join-Path $root "dist\MyDeck-Firebase-Test-debug.apk"
Push-Location $root
try {
    Write-Host "Projeto Firebase validado: $projectId"
    Write-Host "Aplicativo Android validado: $packageName"
    Write-Host "Compilando APK debug com Firebase e executando testes unitarios..."
    & $gradlew --no-daemon --console=plain :app:processDebugGoogleServices :app:testDebugUnitTest :app:assembleDebug
    if ($LASTEXITCODE -ne 0) { throw "Falha na compilacao ou nos testes Gradle. Verifique o primeiro erro no terminal." }
    if (-not (Test-Path -LiteralPath $apk -PathType Leaf)) {
        throw "Gradle finalizou sem criar o APK no caminho esperado: $apk"
    }

    New-Item -ItemType Directory -Path (Split-Path -Parent $output) -Force | Out-Null
    Copy-Item -LiteralPath $apk -Destination $output -Force
    Write-Host "APK GERADO: $output"
    Write-Host "SHA256: $((Get-FileHash -LiteralPath $output -Algorithm SHA256).Hash)"

    if ($Install) {
        Write-Host "Instalando o APK no Android conectado/emulador..."
        & $gradlew --no-daemon --console=plain :app:installDebug
        if ($LASTEXITCODE -ne 0) { throw "APK criado, mas a instalacao falhou. Verifique se o emulador ou celular esta conectado." }
        Write-Host "INSTALADO. Abra o MyDeck no dispositivo."
    }
} finally {
    Pop-Location
}

Write-Host "O APK foi compilado para mydeck-kotlin; verificar gravacoes exige login e regras Firestore publicadas."
