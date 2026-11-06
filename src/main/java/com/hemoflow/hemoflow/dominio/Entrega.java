package com.hemoflow.hemoflow.dominio;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "entrega")
public class Entrega {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "requisicao_id", nullable = false)
    private Requisicao requisicao;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "bolsa_id", nullable = false)
    private Bolsa bolsa;

    @ElementCollection
    @CollectionTable(name = "entrega_rota", joinColumns = @JoinColumn(name = "entrega_id"))
    @OrderColumn(name = "ordem")
    @Column(name = "no_rede_id", nullable = false)
    private List<Long> rotaNoIds = new ArrayList<>();

    @Column(nullable = false)
    private long tempoEstimadoMinutos;

    @Column(nullable = false)
    private LocalDateTime previsaoChegada;

    @Column(nullable = false)
    private double distanciaEstimadaKm;

    @Column(nullable = false)
    private boolean alertaCadeiaFria;

    @Column(nullable = false)
    private int indiceProximoNo;

    protected Entrega() {
    }

    public Entrega(
            Requisicao requisicao,
            Bolsa bolsa,
            List<NoRede> rota,
            long tempoEstimadoMinutos,
            double distanciaEstimadaKm,
            boolean alertaCadeiaFria
    ) {
        this.requisicao = requisicao;
        this.bolsa = bolsa;
        this.rotaNoIds = rota.stream().map(NoRede::getId).toList();
        this.tempoEstimadoMinutos = tempoEstimadoMinutos;
        this.previsaoChegada = LocalDateTime.now().plusMinutes(tempoEstimadoMinutos);
        this.distanciaEstimadaKm = distanciaEstimadaKm;
        this.alertaCadeiaFria = alertaCadeiaFria;
        this.indiceProximoNo = Math.min(1, Math.max(0, rotaNoIds.size() - 1));
    }

    public Long getId() {
        return id;
    }

    public Requisicao getRequisicao() {
        return requisicao;
    }

    public Bolsa getBolsa() {
        return bolsa;
    }

    public List<Long> getRotaNoIds() {
        return List.copyOf(rotaNoIds);
    }

    public long getTempoEstimadoMinutos() {
        return tempoEstimadoMinutos;
    }

    public LocalDateTime getPrevisaoChegada() {
        return previsaoChegada;
    }

    public double getDistanciaEstimadaKm() {
        return distanciaEstimadaKm;
    }

    public boolean isAlertaCadeiaFria() {
        return alertaCadeiaFria;
    }

    public int getIndiceProximoNo() {
        return indiceProximoNo;
    }

    public void avancarProximoNo() {
        if (indiceProximoNo >= rotaNoIds.size() - 1) {
            throw new IllegalStateException("A entrega já está no último ponto da rota");
        }
        indiceProximoNo++;
    }
}
