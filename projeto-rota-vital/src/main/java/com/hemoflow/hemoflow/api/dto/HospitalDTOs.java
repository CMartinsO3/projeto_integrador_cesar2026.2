package com.hemoflow.hemoflow.api.dto;

import java.util.List;

import com.hemoflow.hemoflow.dominio.Hospital;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public final class HospitalDTOs {
    private HospitalDTOs() {
    }

    /** Body de criação: associa o hospital a um nó de rede existente. */
    public record Cadastro(
            @NotBlank String nome,
            @NotNull Long noRedeId,
            Double latitude,
            Double longitude
    ) {
        public Cadastro(String nome, Long noRedeId) {
            this(nome, noRedeId, null, null);
        }
    }

    /** Body de atualização: permite alterar nome e estado ativo/inativo. */
    public record Atualizacao(
            @NotBlank String nome,
            boolean ativo
    ) {
    }

    public record Resposta(Long id, String nome, Long noRedeId, String codigoNo, boolean ativo, Double latitude, Double longitude) {
        public static Resposta de(Hospital hospital) {
            return new Resposta(
                    hospital.getId(),
                    hospital.getNome(),
                    hospital.getLocalizacao().getId(),
                    hospital.getLocalizacao().getCodigo(),
                    hospital.isAtivo(),
                    hospital.getLatitude(),
                    hospital.getLongitude()
            );
        }
    }

    public record Lista(List<Resposta> hospitais) {
    }
}
