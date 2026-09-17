package com.hemoflow.hemoflow.domain.entity;

import com.hemoflow.hemoflow.domain.enums.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "bolsas_componentes")
public class BolsaComponente {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "codigo_rastreio", nullable = false, unique = true, updatable = false)
    private String codigoRastreio;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doacao_id")
    private Doacao doacao;
    
    @NotNull(message = "Tipo de componente é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_componente", nullable = false)
    private TipoComponente tipoComponente;
    
    @NotNull(message = "Tipo ABO é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_abo", nullable = false)
    private TipoABO tipoAbo;
    
    @NotNull(message = "Fator Rh é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(name = "fator_rh", nullable = false)
    private FatorRh fatorRh;
    
    @Positive(message = "Volume deve ser positivo")
    @Column(name = "volume_ml")
    private Integer volumeMl;
    
    @NotNull(message = "Data de produção é obrigatória")
    @Column(name = "data_producao", nullable = false)
    private LocalDate dataProducao;
    
    @NotNull(message = "Data de validade é obrigatória")
    @Column(name = "data_validade", nullable = false)
    private LocalDate dataValidade;
    
    @NotNull(message = "Status é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusBolsa status = StatusBolsa.DISPONIVEL;
    
    @Column(name = "localizacao_atual_id")
    private Long localizacaoAtualId;
    
    @Column(name = "nome_localizacao")
    private String nomeLocalizacao;
    
    @Column(name = "data_cadastro", nullable = false, updatable = false)
    private LocalDateTime dataCadastro;
    
    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;
    
    @PrePersist
    protected void onCreate() {
        if (codigoRastreio == null) {
            codigoRastreio = "BC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
        if (dataValidade == null && dataProducao != null && tipoComponente != null) {
            dataValidade = dataProducao.plusDays(tipoComponente.getDiasValidade());
        }
        dataCadastro = LocalDateTime.now();
        dataAtualizacao = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        dataAtualizacao = LocalDateTime.now();
    }
    
    // Business methods
    public boolean isVencida() {
        return dataValidade != null && dataValidade.isBefore(LocalDate.now());
    }
    
    public int diasParaVencimento() {
        return (int) LocalDate.now().until(dataValidade, java.time.temporal.ChronoUnit.DAYS);
    }
    
    // Constructors
    public BolsaComponente() {}
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getCodigoRastreio() {
        return codigoRastreio;
    }
    
    public void setCodigoRastreio(String codigoRastreio) {
        this.codigoRastreio = codigoRastreio;
    }
    
    public Doacao getDoacao() {
        return doacao;
    }
    
    public void setDoacao(Doacao doacao) {
        this.doacao = doacao;
    }
    
    public TipoComponente getTipoComponente() {
        return tipoComponente;
    }
    
    public void setTipoComponente(TipoComponente tipoComponente) {
        this.tipoComponente = tipoComponente;
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
    
    public Integer getVolumeMl() {
        return volumeMl;
    }
    
    public void setVolumeMl(Integer volumeMl) {
        this.volumeMl = volumeMl;
    }
    
    public LocalDate getDataProducao() {
        return dataProducao;
    }
    
    public void setDataProducao(LocalDate dataProducao) {
        this.dataProducao = dataProducao;
    }
    
    public LocalDate getDataValidade() {
        return dataValidade;
    }
    
    public void setDataValidade(LocalDate dataValidade) {
        this.dataValidade = dataValidade;
    }
    
    public StatusBolsa getStatus() {
        return status;
    }
    
    public void setStatus(StatusBolsa status) {
        this.status = status;
    }
    
    public Long getLocalizacaoAtualId() {
        return localizacaoAtualId;
    }
    
    public void setLocalizacaoAtualId(Long localizacaoAtualId) {
        this.localizacaoAtualId = localizacaoAtualId;
    }
    
    public String getNomeLocalizacao() {
        return nomeLocalizacao;
    }
    
    public void setNomeLocalizacao(String nomeLocalizacao) {
        this.nomeLocalizacao = nomeLocalizacao;
    }
    
    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }
    
    public LocalDateTime getDataAtualizacao() {
        return dataAtualizacao;
    }
}
