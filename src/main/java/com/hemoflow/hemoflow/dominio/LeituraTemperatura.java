package com.hemoflow.hemoflow.dominio;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "leitura_temperatura")
public class LeituraTemperatura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "bolsa_id", nullable = false)
    private Bolsa bolsa;

    @Column(nullable = false)
    private double temperaturaCelsius;

    @Column(nullable = false)
    private double minimoConfiguradoCelsius;

    @Column(nullable = false)
    private double maximoConfiguradoCelsius;

    @Column(nullable = false)
    private boolean alerta;

    @Column(nullable = false)
    private LocalDateTime registradaEm = LocalDateTime.now();

    protected LeituraTemperatura() {
    }

    public LeituraTemperatura(
            Bolsa bolsa,
            double temperaturaCelsius,
            double minimoConfiguradoCelsius,
            double maximoConfiguradoCelsius,
            boolean alerta
    ) {
        this.bolsa = bolsa;
        this.temperaturaCelsius = temperaturaCelsius;
        this.minimoConfiguradoCelsius = minimoConfiguradoCelsius;
        this.maximoConfiguradoCelsius = maximoConfiguradoCelsius;
        this.alerta = alerta;
        this.registradaEm = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Bolsa getBolsa() {
        return bolsa;
    }

    public double getTemperaturaCelsius() {
        return temperaturaCelsius;
    }

    public double getMinimoConfiguradoCelsius() {
        return minimoConfiguradoCelsius;
    }

    public double getMaximoConfiguradoCelsius() {
        return maximoConfiguradoCelsius;
    }

    public boolean isAlerta() {
        return alerta;
    }

    public LocalDateTime getRegistradaEm() {
        return registradaEm;
    }
}
