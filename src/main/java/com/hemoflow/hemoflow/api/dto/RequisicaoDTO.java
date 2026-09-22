package com.hemoflow.hemoflow.api.dto;

import com.hemoflow.hemoflow.domain.enums.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;

public class RequisicaoDTO {
    
    public record Requisicao(
            @NotNull(message = "Hospital é obrigatório")
            Long hospitalId,
            
            @NotNull(message = "Tipo ABO é obrigatório")
            TipoABO tipoAbo,
            
            @NotNull(message = "Fator Rh é obrigatório")
            FatorRh fatorRh,
            
            @NotNull(message = "Tipo de componente é obrigatório")
            TipoComponente tipoComponente,
            
            @Positive(message = "Quantidade deve ser positiva")
            @NotNull(message = "Quantidade é obrigatória")
            Integer quantidade,
            
            @NotNull(message = "Nível de urgência é obrigatório")
            NivelUrgencia urgencia,
            
            LocalDateTime prazoLimite
    ) {}
    
    public record AtualizacaoStatus(
            @NotNull(message = "Status é obrigatório")
            StatusRequisicao status
    ) {}
    
    public record Resposta(
            Long id,
            Long hospitalId,
            String nomeHospital,
            TipoABO tipoAbo,
            FatorRh fatorRh,
            TipoComponente tipoComponente,
            Integer quantidade,
            NivelUrgencia urgencia,
            StatusRequisicao status,
            LocalDateTime dataSolicitacao,
            LocalDateTime prazoLimite
    ) {}
}
