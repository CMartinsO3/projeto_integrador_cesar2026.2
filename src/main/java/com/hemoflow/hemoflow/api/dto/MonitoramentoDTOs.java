package com.hemoflow.hemoflow.api.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.hemoflow.hemoflow.config.DemoMonitoramentoProperties.FaixaTemperatura;
import com.hemoflow.hemoflow.dominio.Hemocomponente;
import com.hemoflow.hemoflow.dominio.LeituraTemperatura;
import com.hemoflow.hemoflow.dominio.NoRede;
import com.hemoflow.hemoflow.dominio.StatusConectividade;
import com.hemoflow.hemoflow.dominio.TipoSanguineo;

import jakarta.validation.constraints.NotNull;

public final class MonitoramentoDTOs {
    private MonitoramentoDTOs() {
    }

    public record RegistroTemperatura(
            @NotNull Long bolsaId,
            @NotNull Double temperaturaCelsius
    ) {
    }

    public record Faixa(
            Hemocomponente hemocomponente,
            double minimoCelsius,
            double maximoCelsius,
            String aviso
    ) {
        public static Faixa de(Hemocomponente hemocomponente, FaixaTemperatura faixa) {
            return new Faixa(hemocomponente, faixa.minimoCelsius(), faixa.maximoCelsius(),
                    "Faixa fictícia de demonstração; não usar para decisão clínica.");
        }
    }

    public record Leitura(
            Long id,
            Long bolsaId,
            String lote,
            Hemocomponente hemocomponente,
            TipoSanguineo tipoSanguineo,
            double temperaturaCelsius,
            double minimoConfiguradoCelsius,
            double maximoConfiguradoCelsius,
            boolean alerta,
            LocalDateTime registradaEm
    ) {
        public static Leitura de(LeituraTemperatura leitura) {
            return new Leitura(
                    leitura.getId(),
                    leitura.getBolsa().getId(),
                    leitura.getBolsa().getLote(),
                    leitura.getBolsa().getHemocomponente(),
                    leitura.getBolsa().getTipoSanguineo(),
                    leitura.getTemperaturaCelsius(),
                    leitura.getMinimoConfiguradoCelsius(),
                    leitura.getMaximoConfiguradoCelsius(),
                    leitura.isAlerta(),
                    leitura.getRegistradaEm()
            );
        }
    }

    public record Dispositivo(
            Long noRedeId,
            String codigo,
            String nome,
            StatusConectividade status,
            LocalDateTime atualizadoEm
    ) {
        public static Dispositivo de(NoRede no) {
            return new Dispositivo(no.getId(), no.getCodigo(), no.getNome(),
                    no.getStatusConectividade(), no.getAtualizadoStatusEm());
        }
    }

    public record Painel(
            int limiteRotaMinutos,
            int intervaloLeituraSegundos,
            String aviso,
            List<Faixa> faixasTemperatura,
            List<Leitura> leiturasRecentes,
            List<Dispositivo> dispositivos
    ) {
    }
}
