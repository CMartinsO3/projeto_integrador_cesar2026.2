package com.hemoflow.hemoflow.estatistica;

import java.util.List;
import java.util.Map;

/**
 * DTOs de resposta para os endpoints de análise estatística descritiva.
 * Escopo: Unidade 1 — sem lógica de FEFO, ABO/Rh ou roteirização.
 */
public final class EstatisticaDTOs {

    private EstatisticaDTOs() {
    }

    /**
     * Resumo quantitativo do estoque de bolsas.
     *
     * @param totalBolsas               total de bolsas cadastradas (todos os status)
     * @param totalDisponivel           bolsas com status DISPONIVEL
     * @param totalAlocada              bolsas com status ALOCADA
     * @param totalEmTransito           bolsas com status EM_TRANSITO
     * @param totalEntregue             bolsas com status ENTREGUE
     * @param totalDescartada           bolsas com status DESCARTADA
     * @param bolsasVencidas            bolsas cuja dataValidade já passou (qualquer status)
     * @param bolsasQuaseVencidas       bolsas disponíveis com validade nos próximos 7 dias
     * @param distribuicaoPorHemocomponente contagem de bolsas DISPONIVEL por hemocomponente
     * @param distribuicaoPorTipoSanguineo  contagem de bolsas DISPONIVEL por tipo sanguíneo
     */
    public record ResumoEstoque(
            long totalBolsas,
            long totalDisponivel,
            long totalAlocada,
            long totalEmTransito,
            long totalEntregue,
            long totalDescartada,
            long bolsasVencidas,
            long bolsasQuaseVencidas,
            Map<String, Long> distribuicaoPorHemocomponente,
            Map<String, Long> distribuicaoPorTipoSanguineo
    ) {
    }

    /**
     * Resumo quantitativo das requisições hospitalares.
     *
     * @param totalRequisicoes          total de requisições cadastradas
     * @param totalPendentes            requisições com status PENDENTE
     * @param totalAguardandoEstoque    requisições com status AGUARDANDO_ESTOQUE
     * @param totalAlocadas             requisições com status ALOCADA
     * @param totalEmTransito           requisições com status EM_TRANSITO
     * @param totalEntregues            requisições com status ENTREGUE
     * @param totalCanceladas           requisições com status CANCELADA
     * @param mediaQuantidadeSolicitada média de unidades por requisição (1 casa decimal)
     * @param taxaAtendimentoPercent    percentual de requisições atendidas (ALOCADA, EM_TRANSITO ou ENTREGUE) sobre o total (1 casa decimal)
     */
    public record ResumoRequisicoes(
            long totalRequisicoes,
            long totalPendentes,
            long totalAguardandoEstoque,
            long totalAlocadas,
            long totalEmTransito,
            long totalEntregues,
            long totalCanceladas,
            double mediaQuantidadeSolicitada,
            double taxaAtendimentoPercent
    ) {
    }

    // ---------------------------------------------------------------------
    // Análise avançada (descritiva completa, inferência e probabilidade)
    // ---------------------------------------------------------------------

    /**
     * Estatística descritiva completa de uma amostra numérica.
     * Campos indefinidos (amostra pequena) são {@code null}.
     */
    public record Descritiva(
            int n,
            Double media,
            Double mediana,
            Double moda,
            Double minimo,
            Double q1,
            Double q3,
            Double maximo,
            Double amplitude,
            Double variancia,
            Double desvioPadrao,
            Double coeficienteVariacaoPercent,
            Double icInferior95,
            Double icSuperior95
    ) {
    }

    /** Faixa de classe do histograma. */
    public record FaixaHistograma(String rotulo, long frequencia, double frequenciaRelativa) {
    }

    /** Ponto da série temporal diária de demanda (unidades solicitadas). */
    public record PontoSerie(String data, double unidades, Double mediaMovel3) {
    }

    /** Reta de tendência y = intercepto + inclinacao * dia. */
    public record Tendencia(double inclinacaoUnidadesPorDia, double intercepto, double r, double r2,
                            double previsaoProximoDia) {
    }

    /** Análise da demanda de hemocomponentes (requisições). */
    public record AnaliseDemanda(
            Descritiva quantidadePorRequisicao,
            List<FaixaHistograma> histograma,
            List<PontoSerie> serieDiaria,
            Descritiva demandaDiaria,
            Tendencia tendencia,
            Double correlacaoQuantidadePrazoHoras
    ) {
    }

    /** Ponto da distribuição de Poisson. */
    public record PontoPoisson(int k, double probabilidade, double acumulada) {
    }

    /** Modelo de Poisson para chegada de requisições por dia. */
    public record ModeloPoisson(double lambdaRequisicoesPorDia, double probNenhumaRequisicao,
                                double probPeloMenosUma, double probMaisQueDobroDaMedia,
                                List<PontoPoisson> distribuicao) {
    }

    /** Modelo Binomial: P(pelo menos k atendimentos em n requisições). */
    public record ModeloBinomial(int n, int kMinimo, double pAtendimento, double probPeloMenosK,
                                 double esperanca, double variancia) {
    }

    /** Resultado do Teorema de Bayes para um tipo sanguíneo. */
    public record ResultadoBayes(String tipoSanguineo, double pTipo, double pAtendidaDadoTipo,
                                 double pTipoDadoAtendida) {
    }

    /** Análise de probabilidade sobre as requisições. */
    public record AnaliseProbabilidade(
            long totalRequisicoes,
            Map<String, Double> frequenciaRelativaPorTipo,
            Map<String, Double> pAtendidaDadoTipo,
            List<ResultadoBayes> bayes,
            Double pAtendimentoGeral,
            Double icInferiorAtendimento95,
            Double icSuperiorAtendimento95,
            ModeloPoisson poisson,
            ModeloBinomial binomial
    ) {
    }

    /** Risco de desabastecimento de um tipo sanguíneo no horizonte considerado. */
    public record RiscoTipo(String tipoSanguineo, long estoqueDisponivel, double demandaMediaDiaria,
                            double lambdaHorizonte, double probRuptura, String nivel,
                            long estoqueSegurancaSugerido) {
    }

    /** Risco de desabastecimento (Poisson) por tipo sanguíneo. */
    public record RiscoDesabastecimento(int horizonteDias, double nivelServico, List<RiscoTipo> tipos) {
    }

    /** Análise estatística das leituras de temperatura (cadeia fria). */
    public record AnaliseTemperatura(
            Descritiva temperatura,
            long leituras,
            long leiturasForaDaFaixa,
            Double percentualForaDaFaixa,
            Double icInferiorForaFaixa95,
            Double icSuperiorForaFaixa95,
            Double pontoMedioFaixa,
            Double estatisticaZ,
            Double pValor,
            String conclusaoTeste,
            Double probForaDaFaixaNormal
    ) {
    }

    /** Taxa de descarte por vencimento de um tipo sanguíneo (coletadas = todas as bolsas registradas). */
    public record DescarteTipo(String tipoSanguineo, long coletadas, long descartadasPorVencimento,
                               Double taxaPercent, Double icInferior95, Double icSuperior95) {
    }

    /** Taxa de descarte por vencimento (total e por tipo) com IC de 95% (Wald). */
    public record AnaliseDescarte(long totalColetadas, long descartadasPorVencimento, long descartadasOutrosMotivos,
                                  long vencidasNaoDescartadas, Double taxaPercent, Double icInferior95,
                                  Double icSuperior95, List<DescarteTipo> porTipo) {
    }

    /** Demanda de um hospital: requisições e unidades solicitadas. */
    public record DemandaHospital(String hospital, long requisicoes, long unidades, Double mediaUnidades,
                                  Double desvioPadraoUnidades) {
    }

    /** Estatística de tempos: duração estimada das entregas e prazo de atendimento das requisições. */
    public record AnaliseTempos(Descritiva tempoEntregaMinutos, Descritiva prazoAtendimentoHoras,
                                long requisicoesSemDados) {
    }
}