package com.hemoflow.hemoflow.domain.entity;

import com.hemoflow.hemoflow.domain.enums.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;

@Entity
@Table(name = "requisicoes")
public class Requisicao {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false)
    @NotNull(message = "Hospital é obrigatório")
    private Hospital hospital;
    
    @NotNull(message = "Tipo ABO é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_abo", nullable = false)
    private TipoABO tipoAbo;
    
    @NotNull(message = "Fator Rh é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(name = "fator_rh", nullable = false)
    private FatorRh fatorRh;
    
    @NotNull(message = "Tipo de componente é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_componente", nullable = false)
    private TipoComponente tipoComponente;
    
    @Positive(message = "Quantidade deve ser positiva")
    @Column(nullable = false)
    private Integer quantidade;
    
    @NotNull(message = "Nível de urgência é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NivelUrgencia urgencia;
    
    @NotNull(message = "Status é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusRequisicao status = StatusRequisicao.PENDENTE;
    
    @NotNull(message = "Data de solicitação é obrigatória")
    @Column(name = "data_solicitacao", nullable = false, updatable = false)
    private LocalDateTime dataSolicitacao;
    
    @Column(name = "prazo_limite")
    private LocalDateTime prazoLimite;
    
    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;
    
    @PrePersist
    protected void onCreate() {
        dataSolicitacao = LocalDateTime.now();
        dataAtualizacao = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        dataAtualizacao = LocalDateTime.now();
    }
    
    // Constructors
    public Requisicao() {}
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Hospital getHospital() {
        return hospital;
    }
    
    public void setHospital(Hospital hospital) {
        this.hospital = hospital;
    }
    
    public TipoABO getTipoAbo() {
        return tipoAbo;
    }
    
    public void setTipoAbo(TipoABO tipoAbo) {
        this.tipoAbo = tipoAbo;
    }
    
    public FatorRh getFatorRh() {
        return fatorRh;
    }
    
    public void setFatorRh(FatorRh fatorRh) {
        this.fatorRh = fatorRh;
    }
    
    public TipoComponente getTipoComponente() {
        return tipoComponente;
    }
    
    public void setTipoComponente(TipoComponente tipoComponente) {
        this.tipoComponente = tipoComponente;
    }
    
    public Integer getQuantidade() {
        return quantidade;
    }
    
    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }
    
    public NivelUrgencia getUrgencia() {
        return urgencia;
    }
    
    public void setUrgencia(NivelUrgencia urgencia) {
        this.urgencia = urgencia;
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
    
    public LocalDateTime getPrazoLimite() {
        return prazoLimite;
    }
    
    public void setPrazoLimite(LocalDateTime prazoLimite) {
        this.prazoLimite = prazoLimite;
    }
    
    public LocalDateTime getDataAtualizacao() {
        return dataAtualizacao;
    }
}
