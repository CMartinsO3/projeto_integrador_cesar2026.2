package com.hemoflow.hemoflow.api.dto;

import com.hemoflow.hemoflow.dominio.Ligacao;
import com.hemoflow.hemoflow.dominio.NoRede;
import com.hemoflow.hemoflow.dominio.TipoNo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public final class NoRedeDTOs {
    private NoRedeDTOs() {
    }

    public record Cadastro(
            @NotBlank String codigo,
            @NotBlank String nome,
            @NotNull TipoNo tipo
    ) {
    }

    public record Resposta(Long id, String codigo, String nome, TipoNo tipo) {
        public static Resposta de(NoRede no) {
            return new Resposta(no.getId(), no.getCodigo(), no.getNome(), no.getTipo());
        }
    }

    public record LigacaoCadastro(
            @NotNull Long origemId,
            @NotNull Long destinoId,
            @NotNull Integer tempoMinutos
    ) {
    }

    public record LigacaoResposta(
            Long id,
            Long origemId,
            String origemCodigo,
            Long destinoId,
            String destinoCodigo,
            int tempoMinutos
    ) {
        public static LigacaoResposta de(Ligacao ligacao) {
            return new LigacaoResposta(
                    ligacao.getId(),
                    ligacao.getOrigem().getId(),
                    ligacao.getOrigem().getCodigo(),
                    ligacao.getDestino().getId(),
                    ligacao.getDestino().getCodigo(),
                    ligacao.getTempoMinutos()
            );
        }
    }
}
