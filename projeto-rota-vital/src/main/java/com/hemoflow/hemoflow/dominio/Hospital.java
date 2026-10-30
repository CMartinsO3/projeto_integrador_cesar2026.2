package com.hemoflow.hemoflow.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "hospital")
public class Hospital {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @OneToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "no_rede_id", unique = true)
    private NoRede localizacao;

    @Column(nullable = false)
    private boolean ativo = true;

    @Column
    private Double latitude;

    @Column
    private Double longitude;


    protected Hospital() {
    }

    public Hospital(String nome, NoRede localizacao) {
        this.nome = nome;
        this.localizacao = localizacao;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public NoRede getLocalizacao() {
        return localizacao;
    }

    public void setLocalizacao(NoRede localizacao) {
        this.localizacao = localizacao;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
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
}
