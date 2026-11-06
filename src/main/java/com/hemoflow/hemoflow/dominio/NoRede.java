package com.hemoflow.hemoflow.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "no_rede")
public class NoRede {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 32)
    private String codigo;

    @Column(nullable = false)
    private String nome;

    @Column
    private Double latitude;

    @Column
    private Double longitude;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoNo tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(32) default 'ONLINE'")
    private StatusConectividade statusConectividade = StatusConectividade.ONLINE;

    @Column(nullable = false, columnDefinition = "timestamp default current_timestamp")
    private LocalDateTime atualizadoStatusEm = LocalDateTime.now();

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean offlineManual;

    protected NoRede() {
    }

    public NoRede(String codigo, String nome, TipoNo tipo) {
        this(codigo, nome, tipo, null, null);
    }

    public NoRede(String codigo, String nome, TipoNo tipo, Double latitude, Double longitude) {
        this.codigo = codigo;
        this.nome = nome;
        this.tipo = tipo;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Long getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public TipoNo getTipo() {
        return tipo;
    }

    public void setTipo(TipoNo tipo) {
        this.tipo = tipo;
    }

    public StatusConectividade getStatusConectividade() {
        return statusConectividade;
    }

    public LocalDateTime getAtualizadoStatusEm() {
        return atualizadoStatusEm;
    }

    public void atualizarStatusConectividade(StatusConectividade statusConectividade) {
        this.statusConectividade = statusConectividade;
        this.offlineManual = statusConectividade == StatusConectividade.OFFLINE;
        this.atualizadoStatusEm = LocalDateTime.now();
    }

    public void registrarHeartbeat() {
        if (offlineManual) {
            return;
        }
        if (this.statusConectividade == StatusConectividade.OFFLINE) {
            this.statusConectividade = StatusConectividade.ONLINE;
        }
        this.atualizadoStatusEm = LocalDateTime.now();
    }

    public void marcarOffline() {
        this.statusConectividade = StatusConectividade.OFFLINE;
        this.offlineManual = false;
        this.atualizadoStatusEm = LocalDateTime.now();
    }
}
