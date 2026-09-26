package com.hemoflow.hemoflow.dominio;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "bolsa")
public class Bolsa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoSanguineo tipoSanguineo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Hemocomponente hemocomponente;

    @Column(nullable = false)
    private LocalDate dataColeta;

    @Column(nullable = false)
    private LocalDate dataValidade;

    @Column(nullable = false)
    private String lote;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusBolsa status = StatusBolsa.DISPONIVEL;

    @ManyToOne(optional = false)
    @JoinColumn(name = "localizacao_id")
    private NoRede localizacaoAtual;

    protected Bolsa() {
    }

    public Bolsa(
            TipoSanguineo tipoSanguineo,
            Hemocomponente hemocomponente,
            LocalDate dataColeta,
            LocalDate dataValidade,
            String lote,
            NoRede localizacaoAtual
    ) {
        this.tipoSanguineo = tipoSanguineo;
        this.hemocomponente = hemocomponente;
        this.dataColeta = dataColeta;
        this.dataValidade = dataValidade;
        this.lote = lote;
        this.localizacaoAtual = localizacaoAtual;
        this.status = StatusBolsa.DISPONIVEL;
    }

    public Long getId() {
        return id;
    }

    public TipoSanguineo getTipoSanguineo() {
        return tipoSanguineo;
    }

    public Hemocomponente getHemocomponente() {
        return hemocomponente;
    }

    public LocalDate getDataColeta() {
        return dataColeta;
    }

    public LocalDate getDataValidade() {
        return dataValidade;
    }

    public String getLote() {
        return lote;
    }

    public StatusBolsa getStatus() {
        return status;
    }

    public void setStatus(StatusBolsa status) {
        this.status = status;
    }

    public NoRede getLocalizacaoAtual() {
        return localizacaoAtual;
    }

    public void setLocalizacaoAtual(NoRede localizacaoAtual) {
        this.localizacaoAtual = localizacaoAtual;
    }

    public boolean isVencida() {
        return dataValidade.isBefore(LocalDate.now());
    }
}
