package com.hemoflow.hemoflow.estatistica;

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
     * @param taxaAtendimentoPercent    percentual de requisições ALOCADA sobre o total (1 casa decimal)
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
}
