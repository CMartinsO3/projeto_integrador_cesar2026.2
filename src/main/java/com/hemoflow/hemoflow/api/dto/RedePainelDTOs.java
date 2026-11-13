package com.hemoflow.hemoflow.api.dto;

import java.util.List;
import java.util.Map;

public final class RedePainelDTOs {

    private RedePainelDTOs() {}

    public record EstatisticaRotaDTO(
            String metodo,
            String rota,
            long totalRequisicoes,
            double tempoMedioMs,
            long tempoMaximoMs,
            double p95Ms,
            Map<Integer, Long> contagemPorStatus
    ) {}

    public record StatusRedeServidorDTO(
            String uptime,
            long uptimeMs,
            String versaoJava,
            int porta,
            String perfilAtivo,
            long numeroNos,
            long numeroLigacoes,
            List<EstatisticaRotaDTO> estatisticasRotas
    ) {}

    public record LigacaoLatenciaDTO(
            Long id,
            Long origemId,
            String origemCodigo,
            String origemNome,
            Long destinoId,
            String destinoCodigo,
            String destinoNome,
            int tempoMinutos,
            Double distanciaKm
    ) {}

    public record CaminhoMinimoHospitalDTO(
            Long hospitalId,
            String hospitalCodigo,
            String hospitalNome,
            List<NoRedeDTOs.Resposta> rotaNos,
            int tempoTotalMinutos,
            double distanciaTotalKm,
            boolean existeRota
    ) {}

    public record LatenciaERotasDTO(
            NoRedeDTOs.Resposta hemocentro,
            List<LigacaoLatenciaDTO> ligacoes,
            List<CaminhoMinimoHospitalDTO> caminhosMinimos
    ) {}
}

