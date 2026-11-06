package com.hemoflow.hemoflow.config;

import java.util.EnumMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.hemoflow.hemoflow.dominio.Hemocomponente;

@ConfigurationProperties(prefix = "hemoflow.demo")
public class DemoMonitoramentoProperties {

    private int limiteRotaMinutos = 120;
    private int intervaloLeituraSegundos = 15;
    private Map<Hemocomponente, FaixaTemperatura> faixasTemperatura = faixasFicticiasPadrao();

    public int getLimiteRotaMinutos() {
        return limiteRotaMinutos;
    }

    public void setLimiteRotaMinutos(int limiteRotaMinutos) {
        if (limiteRotaMinutos <= 0) {
            throw new IllegalArgumentException("O limite didático da rota deve ser positivo");
        }
        this.limiteRotaMinutos = limiteRotaMinutos;
    }

    public int getIntervaloLeituraSegundos() {
        return intervaloLeituraSegundos;
    }

    public void setIntervaloLeituraSegundos(int intervaloLeituraSegundos) {
        if (intervaloLeituraSegundos <= 0) {
            throw new IllegalArgumentException("O intervalo de leitura deve ser positivo");
        }
        this.intervaloLeituraSegundos = intervaloLeituraSegundos;
    }

    public Map<Hemocomponente, FaixaTemperatura> getFaixasTemperatura() {
        return faixasTemperatura;
    }

    public void setFaixasTemperatura(Map<Hemocomponente, FaixaTemperatura> faixasTemperatura) {
        this.faixasTemperatura = new EnumMap<>(faixasTemperatura);
    }

    public FaixaTemperatura faixaTemperatura(Hemocomponente hemocomponente) {
        FaixaTemperatura faixa = faixasTemperatura.get(hemocomponente);
        if (faixa == null) {
            throw new IllegalStateException("Faixa de demonstração ausente para " + hemocomponente);
        }
        return faixa;
    }

    private static Map<Hemocomponente, FaixaTemperatura> faixasFicticiasPadrao() {
        Map<Hemocomponente, FaixaTemperatura> faixas = new EnumMap<>(Hemocomponente.class);
        for (Hemocomponente hemocomponente : Hemocomponente.values()) {
            faixas.put(hemocomponente, new FaixaTemperatura(20.0, 30.0));
        }
        return faixas;
    }

    public record FaixaTemperatura(double minimoCelsius, double maximoCelsius) {
        public FaixaTemperatura {
            if (!Double.isFinite(minimoCelsius) || !Double.isFinite(maximoCelsius)
                    || minimoCelsius >= maximoCelsius) {
                throw new IllegalArgumentException("A faixa de demonstração precisa ter limites finitos e crescentes");
            }
        }
    }
}
