package com.hemoflow.hemoflow.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hemoflow.hemoflow.estatistica.AnaliseAvancadaService;
import com.hemoflow.hemoflow.estatistica.EstatisticaDTOs;
import com.hemoflow.hemoflow.estatistica.EstatisticaService;

/**
 * Endpoints de análise estatística descritiva do estoque e das requisições.
 *
 * <pre>
 * GET /api/v1/estatisticas/estoque      → ResumoEstoque
 * GET /api/v1/estatisticas/requisicoes  → ResumoRequisicoes
 * GET /api/v1/estatisticas/demanda       → AnaliseDemanda (descritiva, série, tendência)
 * GET /api/v1/estatisticas/probabilidade → AnaliseProbabilidade (Bayes, Poisson, Binomial)
 * GET /api/v1/estatisticas/risco         → RiscoDesabastecimento (Poisson por tipo)
 * GET /api/v1/estatisticas/temperatura   → AnaliseTemperatura (IC, teste de hipótese, Normal)
 * GET /api/v1/estatisticas/descarte      → AnaliseDescarte (taxa de descarte por vencimento, IC 95%)
 * GET /api/v1/estatisticas/hospitais     → DemandaHospital (demanda por hospital)
 * GET /api/v1/estatisticas/tempos        → AnaliseTempos (duração das entregas e prazo de atendimento)
 * </pre>
 */
@RestController
@RequestMapping("/api/v1/estatisticas")
public class EstatisticaController {

    private final EstatisticaService estatisticaService;
    private final AnaliseAvancadaService analiseService;

    public EstatisticaController(EstatisticaService estatisticaService, AnaliseAvancadaService analiseService) {
        this.estatisticaService = estatisticaService;
        this.analiseService = analiseService;
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

    /** Estatística descritiva da demanda, histograma, série diária e tendência. */
    @GetMapping("/demanda")
    public EstatisticaDTOs.AnaliseDemanda demanda() {
        return analiseService.analiseDemanda();
    }

    /** Probabilidades condicionais, Bayes, Poisson e Binomial sobre as requisições. */
    @GetMapping("/probabilidade")
    public EstatisticaDTOs.AnaliseProbabilidade probabilidade() {
        return analiseService.analiseProbabilidade();
    }

    /** Probabilidade de ruptura de estoque por tipo sanguíneo no horizonte de 7 dias. */
    @GetMapping("/risco")
    public EstatisticaDTOs.RiscoDesabastecimento risco() {
        return analiseService.riscoDesabastecimento();
    }

    /** Estatísticas, intervalo de confiança e teste de hipótese das leituras de temperatura. */
    @GetMapping("/temperatura")
    public EstatisticaDTOs.AnaliseTemperatura temperatura() {
        return analiseService.analiseTemperatura();
    }
    /** Taxa de descarte por vencimento (total e por tipo sanguíneo) com IC 95%. */
    @GetMapping("/descarte")
    public EstatisticaDTOs.AnaliseDescarte descarte() {
        return analiseService.analiseDescarte();
    }

    /** Demanda agrupada por hospital: requisições, unidades, média e desvio padrão. */
    @GetMapping("/hospitais")
    public java.util.List<EstatisticaDTOs.DemandaHospital> hospitais() {
        return analiseService.demandaPorHospital();
    }

    /** Estatística descritiva dos tempos de entrega e do prazo de atendimento. */
    @GetMapping("/tempos")
    public EstatisticaDTOs.AnaliseTempos tempos() {
        return analiseService.analiseTempos();
    }}
