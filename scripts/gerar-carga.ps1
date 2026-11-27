# Script de carga para observabilidade (Metricas HTTP, Negocio, Logs no Loki e Traces no Tempo)
# Uso: .\scripts\gerar-carga.ps1

$baseUrl = "http://localhost:8080"
$iteracao = 0

Write-Host "Iniciando gerador de carga do HemoFlow em $baseUrl (Ctrl+C para parar)..."

while ($true) {
    $iteracao++

    # 1. Leituras comuns (metricas HTTP + spans)
    try { Invoke-RestMethod -Uri "$baseUrl/actuator/health" -Method Get | Out-Null } catch {}
    try { Invoke-RestMethod -Uri "$baseUrl/api/v1/bolsas" -Method Get | Out-Null } catch {}
    try { Invoke-RestMethod -Uri "$baseUrl/api/v1/requisicoes" -Method Get | Out-Null } catch {}
    try { Invoke-RestMethod -Uri "$baseUrl/api/v1/estatisticas/estoque" -Method Get | Out-Null } catch {}

    # 2. Fluxo completo de negocio a cada ciclo: cadastra bolsa -> cria requisicao -> aloca (gera multiplos spans + metricas de negocio)
    try {
        $hoje = (Get-Date).ToString("yyyy-MM-dd")
        $validade = (Get-Date).AddDays(20).ToString("yyyy-MM-dd")
        $bolsaBody = @{
            tipoSanguineo      = "O_NEG"
            hemocomponente     = "HEMACIAS"
            dataColeta         = $hoje
            dataValidade       = $validade
            lote               = "CARGA-$iteracao"
            localizacaoAtualId = 1
        } | ConvertTo-Json

        Invoke-RestMethod -Uri "$baseUrl/api/v1/bolsas" -Method Post -ContentType "application/json" -Body $bolsaBody | Out-Null

        $janela = (Get-Date).AddHours(6).ToString("yyyy-MM-ddTHH:mm:ss")
        $reqBody = @{
            hospitalId     = 2
            tipoSanguineo  = "O_NEG"
            hemocomponente = "HEMACIAS"
            quantidade     = 1
            janelaEntrega  = $janela
        } | ConvertTo-Json

        $req = Invoke-RestMethod -Uri "$baseUrl/api/v1/requisicoes" -Method Post -ContentType "application/json" -Body $reqBody
        if ($req -and $req.id) {
            Invoke-RestMethod -Uri "$baseUrl/api/v1/requisicoes/$($req.id)/alocar" -Method Post | Out-Null
        }
    } catch {}

    # 3. A cada 3 ciclos, gera erros propositais (404 + 500) para evidenciar logs WARN/ERROR no Loki e taxa de erros no Grafana
    if ($iteracao % 3 -eq 0) {
        try { Invoke-RestMethod -Uri "$baseUrl/api/v1/requisicoes/999999" -Method Get | Out-Null } catch {}
        try { Invoke-RestMethod -Uri "$baseUrl/api/v1/bolsas/entrada" -Method Post -ContentType "application/json" -Body "{}" | Out-Null } catch {}
    }

    if ($iteracao % 5 -eq 0) {
        Write-Host "[$(Get-Date -Format 'HH:mm:ss')] Ciclo $iteracao concluido (metricas, logs e traces enviados)."
    }

    Start-Sleep -Seconds 5
}
