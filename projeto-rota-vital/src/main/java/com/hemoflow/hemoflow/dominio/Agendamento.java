package com.hemoflow.hemoflow.dominio;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "agendamento")
public class Agendamento {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false) @JoinColumn(name = "doador_id") private Doador doador;
    @ManyToOne(optional = false) @JoinColumn(name = "hospital_id") private Hospital hospital;
    @Column(nullable = false) private LocalDateTime dataHora;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private StatusAgendamento status = StatusAgendamento.AGENDADO;

    protected Agendamento() {}
    public Agendamento(Doador doador, Hospital hospital, LocalDateTime dataHora) {
        this.doador = doador; this.hospital = hospital; this.dataHora = dataHora;
    }
    public Long getId() { return id; }
    public Doador getDoador() { return doador; }
    public Hospital getHospital() { return hospital; }
    public LocalDateTime getDataHora() { return dataHora; }
    public StatusAgendamento getStatus() { return status; }
    public void setStatus(StatusAgendamento status) { this.status = status; }
}