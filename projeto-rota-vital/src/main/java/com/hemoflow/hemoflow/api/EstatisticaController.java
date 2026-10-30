package com.hemoflow.hemoflow.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hemoflow.hemoflow.estatistica.EstatisticaDTOs;
import com.hemoflow.hemoflow.estatistica.EstatisticaService;

/**
 * Endpoints de análise estatística descritiva do estoque e das requisições.
 *
 * <pre>
 * GET /api/v1/estatisticas/estoque      → ResumoEstoque
 * GET /api/v1/estatisticas/requisicoes  → ResumoRequisicoes
 * </pre>
 */
@RestController
@RequestMapping("/api/v1/estatisticas")
public class EstatisticaController {

    private final EstatisticaService estatisticaService;

    public EstatisticaController(EstatisticaService estatisticaService) {
        this.estatisticaService = estatisticaService;
    }

    /** Retorna contagens, alertas de vencimento e distribuição do estoque de bolsas. */
    @GetMapping("/estoque")
    public EstatisticaDTOs.ResumoEstoque resumoEstoque() {
        return estatisticaService.resumoEstoque();
    }

    /** Retorna contagens por status, média de quantidade e taxa de atendimento das requisições. */
    @GetMapping("/requisicoes")
    public EstatisticaDTOs.ResumoRequisicoes resumoRequisicoes() {
        return estatisticaService.resumoRequisicoes();
    }
}
