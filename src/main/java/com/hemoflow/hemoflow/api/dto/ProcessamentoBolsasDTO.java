package com.hemoflow.hemoflow.api.dto;

import com.hemoflow.hemoflow.domain.enums.FatorRh;
import com.hemoflow.hemoflow.domain.enums.ModoProcessamento;
import com.hemoflow.hemoflow.domain.enums.TipoABO;
import com.hemoflow.hemoflow.domain.enums.TipoComponente;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class ProcessamentoBolsasDTO {
    
    public record Requisicao(
            @NotNull(message = "Quantidade de registros é obrigatória")
            @Min(value = 1, message = "Quantidade deve ser maior que zero")
            Integer quantidadeRegistros,
            
            @NotNull(message = "Modo de processamento é obrigatório")
            ModoProcessamento modo,
            
            @Min(value = 1, message = "Número de threads deve ser maior que zero")
            Integer numeroThreads,
            
            @NotNull(message = "Tipo ABO é obrigatório")
            TipoABO tipoAbo,
            
            @NotNull(message = "Fator Rh é obrigatório")
            FatorRh fatorRh,
            
            @NotNull(message = "Tipo de componente é obrigatório")
            TipoComponente tipoComponente
    ) {
        public Requisicao {
            // Se modo for SEQUENCIAL, threads deve ser 1
            if (modo == ModoProcessamento.SEQUENCIAL) {
                numeroThreads = 1;
            }
            
            // Validação adicional para modo PARALELO
            if (modo == ModoProcessamento.PARALELO && (numeroThreads == null || numeroThreads < 2)) {
                throw new IllegalArgumentException("Modo PARALELO requer pelo menos 2 threads");
            }
        }
    }
    
    public record Resposta(
            int quantidadeProcessada,
            ModoProcessamento modo,
            int numeroThreads,
            int totalCompativeis,
            int bolsasVencidas,
            int bolsasProximasVencimento,
            long tempoProcessamentoMs,
            double tempoProcessamentoSegundos,
            String tipoReceptor,
            String componenteSolicitado
    ) {}
    
    public record EstatisticasBolsa(
            Long id,
            String codigoRastreio,
            String tipoSanguineo,
            String componente,
            String dataValidade,
            int diasParaVencimento,
            boolean vencida,
            String status
    ) {}
}
