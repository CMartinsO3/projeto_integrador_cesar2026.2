package com.hemoflow.hemoflow.api.dto;

import java.util.List;

import com.hemoflow.hemoflow.dominio.Hospital;

public final class HospitalDTOs {
    private HospitalDTOs() {
    }

    public record Resposta(Long id, String nome, Long noRedeId, String codigoNo, boolean ativo) {
        public static Resposta de(Hospital hospital) {
            return new Resposta(
                    hospital.getId(),
                    hospital.getNome(),
                    hospital.getLocalizacao().getId(),
                    hospital.getLocalizacao().getCodigo(),
                    hospital.isAtivo()
            );
        }
    }

    public record Lista(List<Resposta> hospitais) {
    }
}
