package com.hemoflow.hemoflow.dominio;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "requisicao")
public class Requisicao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "hospital_id")
    private Hospital hospital;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoSanguineo tipoSanguineo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Hemocomponente hemocomponente;

    @Column(nullable = false)
    private int quantidade;

    @Column(nullable = false)
    private LocalDateTime janelaEntrega;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusRequisicao status = StatusRequisicao.PENDENTE;

    @Column(nullable = false)
    private LocalDateTime dataSolicitacao = LocalDateTime.now();

    protected Requisicao() {
    }

    public Requisicao(
            Hospital hospital,
            TipoSanguineo tipoSanguineo,
            Hemocomponente hemocomponente,
            int quantidade,
            LocalDateTime janelaEntrega
    ) {
        this.hospital = hospital;
        this.tipoSanguineo = tipoSanguineo;
        this.hemocomponente = hemocomponente;
        this.quantidade = quantidade;
        this.janelaEntrega = janelaEntrega;
        this.status = StatusRequisicao.PENDENTE;
        this.dataSolicitacao = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Hospital getHospital() {
        return hospital;
    }

    public TipoSanguineo getTipoSanguineo() {
        return tipoSanguineo;
    }

    public Hemocomponente getHemocomponente() {
        return hemocomponente;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public LocalDateTime getJanelaEntrega() {
        return janelaEntrega;
    }

    public StatusRequisicao getStatus() {
        return status;
    }

    public void setStatus(StatusRequisicao status) {
        this.status = status;
    }

    public LocalDateTime getDataSolicitacao() {
        return dataSolicitacao;
    }
}
