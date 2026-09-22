package com.hemoflow.hemoflow.api.dto;

import jakarta.validation.constraints.NotBlank;

public class HospitalDTO {
    
    public record Requisicao(
            @NotBlank(message = "Nome é obrigatório")
            String nome,
            
            @NotBlank(message = "Localização é obrigatória")
            String localizacao
    ) {}
    
    public record Resposta(
            Long id,
            String nome,
            String localizacao,
            Boolean ativo,
            String dataCadastro
    ) {}
}
