package com.hemoflow.hemoflow.api.dto;

import java.time.LocalDateTime;

import com.hemoflow.hemoflow.dominio.Hemocomponente;
import com.hemoflow.hemoflow.dominio.Requisicao;
import com.hemoflow.hemoflow.dominio.StatusRequisicao;
import com.hemoflow.hemoflow.dominio.TipoSanguineo;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public final class RequisicaoDTOs {
    private RequisicaoDTOs() {
    }

    public record Cadastro(
            @NotNull Long hospitalId,
            @NotNull TipoSanguineo tipoSanguineo,
            @NotNull Hemocomponente hemocomponente,
            @Min(1) int quantidade,
            @NotNull LocalDateTime janelaEntrega
    ) {
    }

    public record AtualizacaoStatus(
            @NotNull StatusRequisicao status
    ) {
    }

    public record Resposta(
            Long id,
            Long hospitalId,
            String nomeHospital,
            TipoSanguineo tipoSanguineo,
            Hemocomponente hemocomponente,
            int quantidade,
            LocalDateTime janelaEntrega,
            StatusRequisicao status,
            LocalDateTime dataSolicitacao
    ) {
        public static Resposta de(Requisicao requisicao) {
            return new Resposta(
                    requisicao.getId(),
                    requisicao.getHospital().getId(),
                    requisicao.getHospital().getNome(),
                    requisicao.getTipoSanguineo(),
                    requisicao.getHemocomponente(),
                    requisicao.getQuantidade(),
                    requisicao.getJanelaEntrega(),
                    requisicao.getStatus(),
                    requisicao.getDataSolicitacao()
            );
        }
    }
}
