package com.hemoflow.hemoflow.dominio;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "campanha")
public class Campanha {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false) private String titulo;
    @Column(length = 1000) private String descricao;
    @Column(nullable = false) private LocalDate dataInicio;
    @Column(nullable = false) private LocalDate dataFim;
    @ManyToOne @JoinColumn(name = "hospital_id") private Hospital hospital;
    @Column private String tiposSanguineosAlvo;
    @Column(nullable = false) private boolean ativa = true;

    protected Campanha() {}
    public Campanha(String titulo, String descricao, LocalDate dataInicio, LocalDate dataFim, Hospital hospital, String tiposSanguineosAlvo) {
        this.titulo = titulo; this.descricao = descricao; this.dataInicio = dataInicio;
        this.dataFim = dataFim; this.hospital = hospital; this.tiposSanguineosAlvo = tiposSanguineosAlvo;
    }
    public Long getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getDescricao() { return descricao; }
    public LocalDate getDataInicio() { return dataInicio; }
    public LocalDate getDataFim() { return dataFim; }
    public Hospital getHospital() { return hospital; }
    public String getTiposSanguineosAlvo() { return tiposSanguineosAlvo; }
    public boolean isAtiva() { return ativa; }
    public void setAtiva(boolean ativa) { this.ativa = ativa; }
}