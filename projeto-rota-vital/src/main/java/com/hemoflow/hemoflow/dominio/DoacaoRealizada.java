package com.hemoflow.hemoflow.dominio;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "doacao_realizada")
public class DoacaoRealizada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "doador_id")
    private Doador doador;

    @ManyToOne(optional = false)
    @JoinColumn(name = "hospital_id")
    private Hospital hospital;

    @Column(nullable = false)
    private LocalDate dataDoacao;

    @Column(nullable = false)
    private int volumeMl;

    protected DoacaoRealizada() {}

    public DoacaoRealizada(Doador doador, Hospital hospital, LocalDate dataDoacao, int volumeMl) {
        this.doador = doador;
        this.hospital = hospital;
        this.dataDoacao = dataDoacao;
        this.volumeMl = volumeMl;
    }

    public Long getId() { return id; }
    public Doador getDoador() { return doador; }
    public Hospital getHospital() { return hospital; }
    public LocalDate getDataDoacao() { return dataDoacao; }
    public int getVolumeMl() { return volumeMl; }
}
