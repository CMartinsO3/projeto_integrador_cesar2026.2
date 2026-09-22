package com.hemoflow.hemoflow.api.controller;

import com.hemoflow.hemoflow.api.dto.ProcessamentoBolsasDTO;
import com.hemoflow.hemoflow.domain.entity.BolsaComponente;
import com.hemoflow.hemoflow.domain.enums.ModoProcessamento;
import com.hemoflow.hemoflow.domain.model.ResultadoProcessamento;
import com.hemoflow.hemoflow.service.GeradorDadosBenchmarkService;
import com.hemoflow.hemoflow.service.ProcessamentoBolsasService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Controller para processamento e priorização de bolsas de sangue.
 * Suporta processamento sequencial e paralelo para benchmarks de desempenho.
 */
@RestController
@RequestMapping("/api/v1/processamento-bolsas")
@Tag(name = "Processamento de Bolsas", description = "Benchmark de processamento sequencial vs paralelo")
public class ProcessamentoBolsasController {
    
    private final ProcessamentoBolsasService processamentoBolsasService;
    private final GeradorDadosBenchmarkService geradorDadosBenchmarkService;
    
    public ProcessamentoBolsasController(ProcessamentoBolsasService processamentoBolsasService,
                                          GeradorDadosBenchmarkService geradorDadosBenchmarkService) {
        this.processamentoBolsasService = processamentoBolsasService;
        this.geradorDadosBenchmarkService = geradorDadosBenchmarkService;
    }
    
    @PostMapping("/executar")
    @Operation(
            summary = "Executar processamento de bolsas",
            description = "Processa bolsas em modo SEQUENCIAL ou PARALELO para benchmark. " +
                    "Gera dados sintéticos, filtra por compatibilidade ABO/Rh e prioriza por validade (FEFO)."
    )
    public ResponseEntity<ProcessamentoBolsasDTO.Resposta> executarProcessamento(
            @Valid @RequestBody ProcessamentoBolsasDTO.Requisicao requisicao) {
        
        try {
            // Gerar dados sintéticos determinísticos
            List<BolsaComponente> bolsas = geradorDadosBenchmarkService.gerarBolsas(
                    requisicao.quantidadeRegistros());
            
            // Executar processamento de acordo com o modo
            ResultadoProcessamento resultado;
            
            if (requisicao.modo() == ModoProcessamento.SEQUENCIAL) {
                resultado = processamentoBolsasService.processarSequencial(
                        bolsas,
                        requisicao.tipoAbo(),
                        requisicao.fatorRh(),
                        requisicao.tipoComponente()
                );
            } else {
                resultado = processamentoBolsasService.processarParalelo(
                        bolsas,
                        requisicao.tipoAbo(),
                        requisicao.fatorRh(),
                        requisicao.tipoComponente(),
                        requisicao.numeroThreads()
                );
            }
            
            // Construir resposta
            ProcessamentoBolsasDTO.Resposta resposta = new ProcessamentoBolsasDTO.Resposta(
                    resultado.getTotalProcessadas(),
                    requisicao.modo(),
                    requisicao.numeroThreads(),
                    resultado.getTotalCompativeis(),
                    resultado.getBolsasVencidas(),
                    resultado.getBolsasProximasVencimento(),
                    resultado.getTempoProcessamentoMs(),
                    resultado.getTempoProcessamentoMs() / 1000.0,
                    formatarTipoSanguineo(requisicao.tipoAbo(), requisicao.fatorRh()),
                    requisicao.tipoComponente().getDescricao()
            );
            
            return ResponseEntity.ok(resposta);
            
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Erro no processamento paralelo: " + e.getMessage(), e);
        }
    }
    
    @GetMapping("/info")
    @Operation(
            summary = "Informações sobre o processamento",
            description = "Retorna informações sobre a operação de processamento, " +
                    "complexidade e recomendações de uso."
    )
    public ResponseEntity<InfoProcessamento> obterInfo() {
        InfoProcessamento info = new InfoProcessamento(
                "Priorização de Bolsas por Compatibilidade e Validade (FEFO)",
                "O(n log n)",
                "Filtragem O(n) + Ordenação O(n log n) + Estatísticas O(n)",
                "Dividir bolsas em partições, processar em paralelo, agregar e ordenar resultado final",
                "Cada thread processa uma fatia independente das bolsas, " +
                        "calculando compatibilidade e estatísticas localmente. " +
                        "Ao final, os resultados são agregados e ordenados.",
                List.of(
                        "100.000 registros com 2, 4 e 8 threads",
                        "500.000 registros com 2, 4 e 8 threads",
                        "1.000.000 registros com 2, 4 e 8 threads"
                ),
                42 // Seed para dados determinísticos
        );
        
        return ResponseEntity.ok(info);
    }
    
    private String formatarTipoSanguineo(com.hemoflow.hemoflow.domain.enums.TipoABO tipo,
                                         com.hemoflow.hemoflow.domain.enums.FatorRh fatorRh) {
        return tipo.getValor() + (fatorRh == com.hemoflow.hemoflow.domain.enums.FatorRh.POSITIVO ? "+" : "-");
    }
    
    /**
     * Record para informações sobre a operação de processamento.
     */
    public record InfoProcessamento(
            String operacao,
            String complexidade,
            String detalhesComplexidade,
            String estrategiaParalelizacao,
            String descricaoImplementacao,
            List<String> cenariosRecomendados,
            int seedDeterministica
    ) {}
}
