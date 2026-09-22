# Script PowerShell para executar benchmarks de paralelismo
# HemoFlow - Rota Vital

param(
    [string]$baseUrl = "http://localhost:8080",
    [int]$repeticoes = 3
)

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Benchmarks de Paralelismo - HemoFlow  " -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Verificar se a aplicação está rodando
try {
    $response = Invoke-WebRequest -Uri "$baseUrl/api/v1/processamento-bolsas/info" -Method GET -ErrorAction Stop
    Write-Host "✓ Aplicação está rodando em $baseUrl" -ForegroundColor Green
} catch {
    Write-Host "✗ Erro: Aplicação não está rodando em $baseUrl" -ForegroundColor Red
    Write-Host "  Execute: mvn spring-boot:run" -ForegroundColor Yellow
    exit 1
}

Write-Host ""

# Configurações de teste
$volumes = @(100000, 500000, 1000000)
$configuracoes = @(
    @{ modo = "SEQUENCIAL"; threads = 1 },
    @{ modo = "PARALELO"; threads = 2 },
    @{ modo = "PARALELO"; threads = 4 },
    @{ modo = "PARALELO"; threads = 8 }
)

# Tipo sanguíneo padrão para testes
$tipoAbo = "A"
$fatorRh = "POSITIVO"
$tipoComponente = "CONCENTRADO_HEMACIAS"

# Resultados
$resultados = @()

foreach ($volume in $volumes) {
    Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
    Write-Host "  Testando com $volume registros" -ForegroundColor Cyan
    Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
    Write-Host ""
    
    foreach ($config in $configuracoes) {
        $modo = $config.modo
        $threads = $config.threads
        
        $label = if ($modo -eq "SEQUENCIAL") { "Sequencial" } else { "$threads threads" }
        Write-Host "  Executando: $label..." -NoNewline
        
        $tempos = @()
        
        for ($i = 1; $i -le $repeticoes; $i++) {
            $body = @{
                quantidadeRegistros = $volume
                modo = $modo
                numeroThreads = $threads
                tipoAbo = $tipoAbo
                fatorRh = $fatorRh
                tipoComponente = $tipoComponente
            } | ConvertTo-Json
            
            try {
                $response = Invoke-RestMethod -Uri "$baseUrl/api/v1/processamento-bolsas/executar" `
                    -Method POST `
                    -Body $body `
                    -ContentType "application/json" `
                    -ErrorAction Stop
                
                $tempos += $response.tempoProcessamentoMs
                
            } catch {
                Write-Host " ERRO!" -ForegroundColor Red
                Write-Host "  $($_.Exception.Message)" -ForegroundColor Red
                continue
            }
        }
        
        if ($tempos.Count -eq $repeticoes) {
            $tempoMedio = ($tempos | Measure-Object -Average).Average
            $tempoMin = ($tempos | Measure-Object -Minimum).Minimum
            $tempoMax = ($tempos | Measure-Object -Maximum).Maximum
            
            Write-Host " OK" -ForegroundColor Green
            Write-Host "    Média: $([math]::Round($tempoMedio, 2)) ms" -ForegroundColor White
            Write-Host "    Min: $tempoMin ms | Max: $tempoMax ms" -ForegroundColor Gray
            
            $resultados += [PSCustomObject]@{
                Volume = $volume
                Modo = $modo
                Threads = $threads
                Label = $label
                TempoMedio = [math]::Round($tempoMedio, 2)
                TempoMin = $tempoMin
                TempoMax = $tempoMax
            }
        }
    }
    
    Write-Host ""
}

# Exibir tabela de resultados
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host "  RESULTADOS CONSOLIDADOS" -ForegroundColor Cyan
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host ""

foreach ($volume in $volumes) {
    Write-Host "  $volume registros:" -ForegroundColor Yellow
    
    $sequencial = ($resultados | Where-Object { $_.Volume -eq $volume -and $_.Modo -eq "SEQUENCIAL" }).TempoMedio
    
    foreach ($resultado in ($resultados | Where-Object { $_.Volume -eq $volume })) {
        $speedup = if ($resultado.Modo -eq "PARALELO") {
            [math]::Round($sequencial / $resultado.TempoMedio, 2)
        } else {
            1.00
        }
        
        $eficiencia = if ($resultado.Modo -eq "PARALELO") {
            [math]::Round(($speedup / $resultado.Threads) * 100, 1)
        } else {
            100.0
        }
        
        Write-Host "    $($resultado.Label.PadRight(12)): $($resultado.TempoMedio.ToString().PadLeft(8)) ms | Speedup: ${speedup}x | Eficiência: ${eficiencia}%" -ForegroundColor White
    }
    
    Write-Host ""
}

# Salvar resultados em CSV
$csvPath = "resultados-benchmark-$(Get-Date -Format 'yyyyMMdd-HHmmss').csv"
$resultados | Export-Csv -Path $csvPath -NoTypeInformation -Encoding UTF8

Write-Host "✓ Resultados salvos em: $csvPath" -ForegroundColor Green
Write-Host ""

# Gerar tabela markdown para o relatório
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host "  TABELA MARKDOWN PARA O RELATÓRIO" -ForegroundColor Cyan
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host ""

Write-Host "| Registros   | Sequencial (ms) | 2 threads (ms) | 4 threads (ms) | 8 threads (ms) |"
Write-Host "|-------------|-----------------|----------------|----------------|----------------|"

foreach ($volume in $volumes) {
    $seq = ($resultados | Where-Object { $_.Volume -eq $volume -and $_.Modo -eq "SEQUENCIAL" }).TempoMedio
    $t2 = ($resultados | Where-Object { $_.Volume -eq $volume -and $_.Threads -eq 2 }).TempoMedio
    $t4 = ($resultados | Where-Object { $_.Volume -eq $volume -and $_.Threads -eq 4 }).TempoMedio
    $t8 = ($resultados | Where-Object { $_.Volume -eq $volume -and $_.Threads -eq 8 }).TempoMedio
    
    $volumeStr = if ($volume -ge 1000000) { "$([math]::Round($volume/1000000, 1))M" } `
                 elseif ($volume -ge 1000) { "$([math]::Round($volume/1000))k" } `
                 else { $volume }
    
    Write-Host "| **$volumeStr** | $seq | $t2 | $t4 | $t8 |"
}

Write-Host ""
Write-Host "✓ Copie a tabela acima para o RELATORIO_PARALELISMO.md" -ForegroundColor Green
Write-Host ""
Write-Host "Benchmarks concluídos!" -ForegroundColor Cyan
