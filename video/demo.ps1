# Demonstração da função HTTP "Guia de Transporte" do HemoFlow, para gravar o vídeo.
# Como usar (num PowerShell normal):
#   powershell -ExecutionPolicy Bypass -File .\video\demo.ps1
# A cada etapa o script pausa: aperte ENTER quando terminar de explicar.

$ErrorActionPreference = "Stop"
$codigo = Join-Path $PSScriptRoot "..\codigo" | Resolve-Path
Set-Location $codigo
$url = "http://127.0.0.1:8787"

function Etapa($titulo) {
    Write-Host ""
    Write-Host ("=" * 70) -ForegroundColor DarkRed
    Write-Host "  $titulo" -ForegroundColor Red
    Write-Host ("=" * 70) -ForegroundColor DarkRed
}
function Pausa { Read-Host "`n[ENTER para continuar]" | Out-Null }

if (-not (Test-Path "node_modules")) {
    Etapa "Instalando dependências (só na primeira vez)"
    npm.cmd install --no-audit --no-fund
}

Etapa "1) Testes unitários (node:test)"
npm.cmd test
Pausa

Etapa "2) Subindo a função no runtime local da Cloudflare (wrangler dev)"
$servidor = Start-Process -FilePath "npx.cmd" -ArgumentList "wrangler", "dev", "--port", "8787" `
    -WindowStyle Hidden -PassThru
$pronto = $false
for ($i = 0; $i -lt 60 -and -not $pronto; $i++) {
    Start-Sleep -Seconds 1
    try { Invoke-RestMethod "$url/health" -TimeoutSec 2 | Out-Null; $pronto = $true } catch {}
}
if (-not $pronto) { Write-Host "O servidor não subiu em 60 s." -ForegroundColor Yellow; exit 1 }
Write-Host "Função no ar em $url" -ForegroundColor Green
curl.exe -s "$url/health"
Pausa

try {
    Etapa "3) Requisição válida -> guia APTA (201 Created)"
    Write-Host "Payload: exemplo-requisicao.json (2 bolsas de hemácias para receptor A+)" -ForegroundColor DarkGray
    curl.exe -s -i -X POST "$url/guia-transporte" -H "Content-Type: application/json" -d "@exemplo-requisicao.json"
    Pausa

    Etapa "4) Requisição com problemas -> guia BLOQUEADA (422)"
    Write-Host "Payload: exemplo-bloqueada.json (bolsa B+ para receptor A+, plaquetas, rota de 110 min)" -ForegroundColor DarkGray
    $resp = curl.exe -s -w "`nHTTP %{http_code}" -X POST "$url/guia-transporte" -H "Content-Type: application/json" -d "@exemplo-bloqueada.json"
    $status = ($resp -split "`n")[-1]
    $json = (($resp -split "`n") | Select-Object -SkipLast 1) -join "`n" | ConvertFrom-Json
    Write-Host $status -ForegroundColor Yellow
    Write-Host "status: $($json.status)" -ForegroundColor Yellow
    $json.pendencias | ForEach-Object { Write-Host "  - $_" -ForegroundColor Yellow }
    Pausa

    Etapa "5) Entrada inválida -> 400 Bad Request"
    curl.exe -s -i -X POST "$url/guia-transporte" -H "Content-Type: application/json" -d "{}"
    Pausa

    Etapa "6) Guia imprimível (?formato=html) aberta no navegador"
    $arquivo = Join-Path $env:TEMP "guia-hemoflow.html"
    curl.exe -s -X POST "$url/guia-transporte?formato=html" -H "Content-Type: application/json" -d "@exemplo-requisicao.json" -o $arquivo
    Start-Process $arquivo
    Pausa
}
finally {
    Etapa "Encerrando o servidor local"
    taskkill.exe /PID $servidor.Id /T /F | Out-Null
    Write-Host "Fim da demonstração." -ForegroundColor Green
}
