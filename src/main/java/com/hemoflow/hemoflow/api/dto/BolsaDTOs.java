package com.hemoflow.hemoflow.api.dto;

import java.time.LocalDate;

import com.hemoflow.hemoflow.dominio.Bolsa;
import com.hemoflow.hemoflow.dominio.Hemocomponente;
import com.hemoflow.hemoflow.dominio.StatusBolsa;
import com.hemoflow.hemoflow.dominio.TipoSanguineo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public final class BolsaDTOs {
    private BolsaDTOs() {
    }

    public record RequisicaoCadastro(
            @NotNull TipoSanguineo tipoSanguineo,
            @NotNull Hemocomponente hemocomponente,
            @NotNull LocalDate dataColeta,
            @NotNull LocalDate dataValidade,
            @NotBlank String lote,
            @NotNull Long localizacaoAtualId
    ) {
    }

    public record Resposta(
            Long id,
            TipoSanguineo tipoSanguineo,
            Hemocomponente hemocomponente,
            LocalDate dataColeta,
            LocalDate dataValidade,
            String lote,
            StatusBolsa status,
            Long localizacaoAtualId,
            String nomeLocalizacao,
            boolean vencida
    ) {
        public static Resposta de(Bolsa bolsa) {
            return new Resposta(
                    bolsa.getId(),
                    bolsa.getTipoSanguineo(),
                    bolsa.getHemocomponente(),
                    bolsa.getDataColeta(),
                    bolsa.getDataValidade(),
                    bolsa.getLote(),
                    bolsa.getStatus(),
                    bolsa.getLocalizacaoAtual().getId(),
                    bolsa.getLocalizacaoAtual().getNome(),
                    bolsa.isVencida()
            );
        }
    }
}
