# Script unico para subir o stack, validar endpoints/targets e criar o usuario Viewer do professor no Grafana
# Uso: .\scripts\subir-e-validar.ps1 [-SenhaProfessor "ProfHemo@2026"]

param(
    [string]$SenhaProfessor = "ProfHemo@2026"
)

$ErrorActionPreference = "Stop"

Write-Host "1) Verificando Docker..." -ForegroundColor Cyan
docker info *> $null
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERRO: Docker Desktop nao esta rodando. Abra o Docker Desktop, espere ficar verde e rode este script novamente." -ForegroundColor Red
    exit 1
}

Write-Host "2) Subindo containers com docker compose up -d --build..." -ForegroundColor Cyan
docker compose up -d --build
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERRO ao subir containers. Se o erro for no volume /dev/disk do cadvisor, remova essa linha do docker-compose.yml." -ForegroundColor Red
    exit 1
}

Write-Host "`n3) Status dos containers (tire o PRINT 1 desta tela com data/hora visivel):" -ForegroundColor Yellow
docker compose ps

Write-Host "`n4) Aguardando HemoFlow (:8080) e Grafana (:3000) ficarem prontos..." -ForegroundColor Cyan
for ($i = 0; $i -lt 45; $i++) {
    try {
        $appOk = (Invoke-RestMethod -Uri "http://localhost:8080/actuator/health" -TimeoutSec 2).status -eq "UP"
        $grafOk = (Invoke-RestMethod -Uri "http://localhost:3000/api/health" -TimeoutSec 2).database -eq "ok"
        if ($appOk -and $grafOk) { break }
    } catch {}
    Start-Sleep -Seconds 2
}

Write-Host "5) Criando usuario 'professor' (papel Viewer) no Grafana..." -ForegroundColor Cyan
$adminPass = "hemoflow@2026"
if (Test-Path ".env") {
    $envLine = Get-Content ".env" | Where-Object { $_ -match "^GF_SECURITY_ADMIN_PASSWORD=" } | Select-Object -First 1
    if ($envLine) {
        $adminPass = ($envLine -split "=", 2)[1].Trim()
    }
}

$pair = "admin:$adminPass"
$encodedCreds = [System.Convert]::ToBase64String([System.Text.Encoding]::ASCII.GetBytes($pair))
$headers = @{ Authorization = "Basic $encodedCreds" }

$userBody = @{
    name     = "Professor Avaliador"
    email    = "professor@cesar.school"
    login    = "professor"
    password = $SenhaProfessor
    OrgId    = 1
} | ConvertTo-Json

try {
    Invoke-RestMethod -Uri "http://localhost:3000/api/admin/users" -Method Post -Headers $headers -ContentType "application/json" -Body $userBody | Out-Null
    Write-Host "   Usuario 'professor' criado com sucesso (role Viewer)." -ForegroundColor Green
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    if ($statusCode -eq 412 -or $statusCode -eq 409) {
        Write-Host "   Usuario 'professor' ja existe no Grafana (OK)." -ForegroundColor Green
    } else {
        Write-Host "   Aviso: nao foi possivel criar via API ($($_.Exception.Message)). Crie pela UI se necessario." -ForegroundColor Yellow
    }
}

Write-Host "`n6) Conferindo targets no Prometheus (http://localhost:9090/targets)..." -ForegroundColor Cyan
Start-Sleep -Seconds 5
try {
    $targets = Invoke-RestMethod -Uri "http://localhost:9090/api/v1/targets" -TimeoutSec 5
    foreach ($t in $targets.data.activeTargets) {
        Write-Host ("   - job={0,-15} health={1}" -f $t.labels.job, $t.health)
    }
} catch {
    Write-Host "   Nao foi possivel consultar targets ainda. Verifique http://localhost:9090/targets no navegador." -ForegroundColor Yellow
}

Write-Host "`n7) Conferindo metricas no /actuator/prometheus..." -ForegroundColor Cyan
try {
    $prom = (Invoke-WebRequest -Uri "http://localhost:8080/actuator/prometheus" -UseBasicParsing).Content
    $linhas = ($prom -split "`n") | Where-Object { $_ -match '^hemoflow_|^http_server_requests_seconds_count' } | Select-Object -First 5
    $linhas | ForEach-Object { Write-Host "   $_" }
} catch {
    Write-Host "   Nao foi possivel consultar /actuator/prometheus." -ForegroundColor Yellow
}

Write-Host "`n=== PROXIMOS PASSOS COM VOCE ===" -ForegroundColor Green
Write-Host "1. Em outra janela do PowerShell, rode:  .\scripts\gerar-carga.ps1"
Write-Host "2. Em outra janela do PowerShell, rode:  cloudflared tunnel --url http://localhost:3000"
Write-Host "3. Tire os prints com data/hora visivel (Grafana: http://localhost:3000 | Prometheus: http://localhost:9090/targets)"
Write-Host "4. Usuario do professor: login 'professor' / senha '$SenhaProfessor' (informar apenas no PDF/Classroom!)"
