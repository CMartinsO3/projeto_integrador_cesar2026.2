package com.hemoflow.hemoflow.api.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.hemoflow.hemoflow.dominio.Hemocomponente;
import com.hemoflow.hemoflow.dominio.StatusRequisicao;
import com.hemoflow.hemoflow.dominio.TipoSanguineo;

public final class EntregaDTOs {
    private EntregaDTOs() {
    }

    public record Resposta(
            Long entregaId,
            Long requisicaoId,
            StatusRequisicao statusRequisicao,
            Long bolsaId,
            String lote,
            Hemocomponente hemocomponente,
            TipoSanguineo tipoSanguineo,
            String nomeHospital,
            LocalDateTime janelaEntrega,
            List<NoRedeDTOs.Resposta> rota,
            int proximoNoIndice,
            long tempoEstimadoMinutos,
            LocalDateTime previsaoChegada,
            double distanciaEstimadaKm,
            boolean alertaCadeiaFria,
            Double ultimaTemperaturaCelsius,
            boolean alertaTemperatura,
            LocalDateTime ultimaLeituraEm
    ) {
    }
}
