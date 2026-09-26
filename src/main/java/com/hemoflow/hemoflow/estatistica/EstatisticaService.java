package com.hemoflow.hemoflow.estatistica;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hemoflow.hemoflow.dominio.Bolsa;
import com.hemoflow.hemoflow.dominio.Hemocomponente;
import com.hemoflow.hemoflow.dominio.Requisicao;
import com.hemoflow.hemoflow.dominio.StatusBolsa;
import com.hemoflow.hemoflow.dominio.StatusRequisicao;
import com.hemoflow.hemoflow.dominio.TipoSanguineo;
import com.hemoflow.hemoflow.persistencia.BolsaRepository;
import com.hemoflow.hemoflow.persistencia.RequisicaoRepository;

/**
 * Serviço de análise estatística descritiva do estoque e das requisições.
 * Calcula indicadores quantitativos (contagens, médias, taxas) sem aplicar
 * regras de prioridade FEFO, compatibilidade ABO/Rh ou roteirização por grafo
 * — funcionalidades reservadas para Unidades futuras.
 */
@Service
public class EstatisticaService {

    /** Janela em dias para considerar uma bolsa "quase vencendo". */
    static final int DIAS_ALERTA_VENCIMENTO = 7;

    private final BolsaRepository bolsaRepository;
    private final RequisicaoRepository requisicaoRepository;

    public EstatisticaService(BolsaRepository bolsaRepository,
                              RequisicaoRepository requisicaoRepository) {
        this.bolsaRepository = bolsaRepository;
        this.requisicaoRepository = requisicaoRepository;
    }

    // -------------------------------------------------------------------------
    // Estoque
    // -------------------------------------------------------------------------

    /**
     * Calcula o resumo descritivo do estoque de bolsas.
     * A distribuição por hemocomponente e tipo sanguíneo considera apenas
     * bolsas com status DISPONIVEL.
     */
    @Transactional(readOnly = true)
    public EstatisticaDTOs.ResumoEstoque resumoEstoque() {
        List<Bolsa> todas = bolsaRepository.findAll();
        LocalDate hoje = LocalDate.now();
        LocalDate limiteAlerta = hoje.plusDays(DIAS_ALERTA_VENCIMENTO);

        long totalBolsas    = todas.size();
        long totalDisponivel = contarBolsaPorStatus(todas, StatusBolsa.DISPONIVEL);
        long totalAlocada    = contarBolsaPorStatus(todas, StatusBolsa.ALOCADA);
        long totalEmTransito = contarBolsaPorStatus(todas, StatusBolsa.EM_TRANSITO);
        long totalEntregue   = contarBolsaPorStatus(todas, StatusBolsa.ENTREGUE);
        long totalDescartada = contarBolsaPorStatus(todas, StatusBolsa.DESCARTADA);

        // Bolsas cujo prazo de validade já expirou (qualquer status)
        long bolsasVencidas = todas.stream()
                .filter(b -> b.getDataValidade().isBefore(hoje))
                .count();

        // Bolsas DISPONIVEL com validade entre hoje e hoje+7 dias
        long bolsasQuaseVencidas = todas.stream()
                .filter(b -> b.getStatus() == StatusBolsa.DISPONIVEL)
                .filter(b -> !b.getDataValidade().isBefore(hoje))
                .filter(b -> !b.getDataValidade().isAfter(limiteAlerta))
                .count();

        Map<String, Long> distHemocomponente = Arrays.stream(Hemocomponente.values())
                .collect(Collectors.toMap(
                        Hemocomponente::name,
                        h -> contarDisponiveisPorHemocomponente(todas, h),
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        Map<String, Long> distTipoSanguineo = Arrays.stream(TipoSanguineo.values())
                .collect(Collectors.toMap(
                        TipoSanguineo::name,
                        ts -> contarDisponiveisPorTipoSanguineo(todas, ts),
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        return new EstatisticaDTOs.ResumoEstoque(
                totalBolsas,
                totalDisponivel,
                totalAlocada,
                totalEmTransito,
                totalEntregue,
                totalDescartada,
                bolsasVencidas,
                bolsasQuaseVencidas,
                distHemocomponente,
                distTipoSanguineo
        );
    }

    // -------------------------------------------------------------------------
    // Requisições
    // -------------------------------------------------------------------------

    /**
     * Calcula o resumo descritivo das requisições hospitalares.
     * A taxa de atendimento é calculada como:
     *   (requisições ALOCADA / total) * 100, arredondado a 1 casa decimal.
     */
    @Transactional(readOnly = true)
    public EstatisticaDTOs.ResumoRequisicoes resumoRequisicoes() {
        List<Requisicao> todas = requisicaoRepository.findAll();

        long total      = todas.size();
        long pendentes  = contarReqPorStatus(todas, StatusRequisicao.PENDENTE);
        long aguardando = contarReqPorStatus(todas, StatusRequisicao.AGUARDANDO_ESTOQUE);
        long alocadas   = contarReqPorStatus(todas, StatusRequisicao.ALOCADA);
        long emTransito = contarReqPorStatus(todas, StatusRequisicao.EM_TRANSITO);
        long entregues  = contarReqPorStatus(todas, StatusRequisicao.ENTREGUE);
        long canceladas = contarReqPorStatus(todas, StatusRequisicao.CANCELADA);

        double mediaQuantidade = total == 0 ? 0.0
                : arredondar1(todas.stream()
                        .mapToInt(Requisicao::getQuantidade)
                        .average()
                        .orElse(0.0));

        double taxaAtendimento = total == 0 ? 0.0
                : arredondar1(alocadas * 100.0 / total);

        return new EstatisticaDTOs.ResumoRequisicoes(
                total,
                pendentes,
                aguardando,
                alocadas,
                emTransito,
                entregues,
                canceladas,
                mediaQuantidade,
                taxaAtendimento
        );
    }

    // -------------------------------------------------------------------------
    // Helpers privados
    // -------------------------------------------------------------------------

    private long contarBolsaPorStatus(List<Bolsa> bolsas, StatusBolsa status) {
        return bolsas.stream().filter(b -> b.getStatus() == status).count();
    }

    private long contarReqPorStatus(List<Requisicao> requisicoes, StatusRequisicao status) {
        return requisicoes.stream().filter(r -> r.getStatus() == status).count();
    }

    private long contarDisponiveisPorHemocomponente(List<Bolsa> bolsas, Hemocomponente h) {
        return bolsas.stream()
                .filter(b -> b.getStatus() == StatusBolsa.DISPONIVEL)
                .filter(b -> b.getHemocomponente() == h)
                .count();
    }

    private long contarDisponiveisPorTipoSanguineo(List<Bolsa> bolsas, TipoSanguineo ts) {
        return bolsas.stream()
                .filter(b -> b.getStatus() == StatusBolsa.DISPONIVEL)
                .filter(b -> b.getTipoSanguineo() == ts)
                .count();
    }

    /** Arredonda para 1 casa decimal. */
    private double arredondar1(double valor) {
        return Math.round(valor * 10.0) / 10.0;
    }
}
