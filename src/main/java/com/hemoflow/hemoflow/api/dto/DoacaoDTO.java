package com.hemoflow.hemoflow.api.dto;

import com.hemoflow.hemoflow.domain.enums.FatorRh;
import com.hemoflow.hemoflow.domain.enums.TipoABO;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

public class DoacaoDTO {
    
    public record Requisicao(
            @NotNull(message = "Tipo ABO é obrigatório")
            TipoABO tipoAbo,
            
            @NotNull(message = "Fator Rh é obrigatório")
            FatorRh fatorRh,
            
            @NotNull(message = "Data de coleta é obrigatória")
            LocalDate dataColeta,
            
            @Positive(message = "Volume coletado deve ser positivo")
            Integer volumeColetadoMl,
            
            Long centroColetaId,
            String centroColetaNome
    ) {}
    
    public record Resposta(
            Long id,
            TipoABO tipoAbo,
            FatorRh fatorRh,
            LocalDate dataColeta,
            Integer volumeColetadoMl,
            Long centroColetaId,
            String centroColetaNome
    ) {}
}
