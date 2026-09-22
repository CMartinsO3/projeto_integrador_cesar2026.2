package com.hemoflow.hemoflow.api.dto;

import com.hemoflow.hemoflow.domain.enums.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

public class BolsaComponenteDTO {
    
    public record Requisicao(
            Long doacaoId,
            
            @NotNull(message = "Tipo de componente é obrigatório")
            TipoComponente tipoComponente,
            
            @NotNull(message = "Tipo ABO é obrigatório")
            TipoABO tipoAbo,
            
            @NotNull(message = "Fator Rh é obrigatório")
            FatorRh fatorRh,
            
            @Positive(message = "Volume deve ser positivo")
            Integer volumeMl,
            
            @NotNull(message = "Data de produção é obrigatória")
            LocalDate dataProducao,
            
            Long localizacaoAtualId
    ) {}
    
    public record AtualizacaoStatus(
            @NotNull(message = "Status é obrigatório")
            StatusBolsa status
    ) {}
    
    public record Resposta(
            Long id,
            String codigoRastreio,
            Long doacaoId,
            TipoComponente tipoComponente,
            TipoABO tipoAbo,
            FatorRh fatorRh,
            Integer volumeMl,
            LocalDate dataProducao,
            LocalDate dataValidade,
            StatusBolsa status,
            Long localizacaoAtualId,
            String nomeLocalizacao,
            boolean vencida,
            int diasParaVencimento
    ) {}
}
