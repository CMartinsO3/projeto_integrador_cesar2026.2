package com.hemoflow.hemoflow.domain.entity;

import com.hemoflow.hemoflow.domain.enums.FatorRh;
import com.hemoflow.hemoflow.domain.enums.TipoABO;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "doacoes")
public class Doacao {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "Tipo ABO é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_abo", nullable = false)
    private TipoABO tipoAbo;
    
    @NotNull(message = "Fator Rh é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(name = "fator_rh", nullable = false)
    private FatorRh fatorRh;
    
    @NotNull(message = "Data de coleta é obrigatória")
    @Column(name = "data_coleta", nullable = false)
    private LocalDate dataColeta;
    
    @Positive(message = "Volume coletado deve ser positivo")
    @Column(name = "volume_coletado_ml")
    private Integer volumeColetadoMl;
    
    @Column(name = "centro_coleta_id")
    private Long centroColetaId;
    
    @Column(name = "centro_coleta_nome")
    private String centroColetaNome;
    
    @Column(name = "data_cadastro", nullable = false, updatable = false)
    private LocalDateTime dataCadastro;
    
    @PrePersist
    protected void onCreate() {
        dataCadastro = LocalDateTime.now();
    }
    
    // Constructors
    public Doacao() {}
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
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
    
    public LocalDate getDataColeta() {
        return dataColeta;
    }
    
    public void setDataColeta(LocalDate dataColeta) {
        this.dataColeta = dataColeta;
    }
    
    public Integer getVolumeColetadoMl() {
        return volumeColetadoMl;
    }
    
    public void setVolumeColetadoMl(Integer volumeColetadoMl) {
        this.volumeColetadoMl = volumeColetadoMl;
    }
    
    public Long getCentroColetaId() {
        return centroColetaId;
    }
    
    public void setCentroColetaId(Long centroColetaId) {
        this.centroColetaId = centroColetaId;
    }
    
    public String getCentroColetaNome() {
        return centroColetaNome;
    }
    
    public void setCentroColetaNome(String centroColetaNome) {
        this.centroColetaNome = centroColetaNome;
    }
    
    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }
}
