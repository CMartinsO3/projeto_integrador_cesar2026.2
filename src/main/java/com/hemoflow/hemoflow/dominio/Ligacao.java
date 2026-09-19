package com.hemoflow.hemoflow.dominio;

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
@Table(name = "ligacao")
public class Ligacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "origem_id")
    private NoRede origem;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "destino_id")
    private NoRede destino;

    @Column(nullable = false)
    private int tempoMinutos;

    protected Ligacao() {
    }

    public Ligacao(NoRede origem, NoRede destino, int tempoMinutos) {
        this.origem = origem;
        this.destino = destino;
        this.tempoMinutos = tempoMinutos;
    }

    public Long getId() {
        return id;
    }

    public NoRede getOrigem() {
        return origem;
    }

    public NoRede getDestino() {
        return destino;
    }

    public int getTempoMinutos() {
        return tempoMinutos;
    }

    public void setOrigem(NoRede origem) {
        this.origem = origem;
    }

    public void setDestino(NoRede destino) {
        this.destino = destino;
    }

    public void setTempoMinutos(int tempoMinutos) {
        this.tempoMinutos = tempoMinutos;
    }
}
