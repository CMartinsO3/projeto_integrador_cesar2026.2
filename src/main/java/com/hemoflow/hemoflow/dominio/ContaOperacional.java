package com.hemoflow.hemoflow.dominio;

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
@Table(name = "conta_operacional")
public class ContaOperacional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 120)
    private String usuario;

    @Column(nullable = false)
    private String senhaHash;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean aprovado = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PerfilOperacional perfil;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "hospital_id")
    private Hospital hospital;

    protected ContaOperacional() {
    }

    public ContaOperacional(
            String usuario,
            String senhaHash,
            String nome,
            PerfilOperacional perfil,
            Hospital hospital
    ) {
        this(usuario, senhaHash, nome, perfil, hospital, true);
    }

    public ContaOperacional(
            String usuario,
            String senhaHash,
            String nome,
            PerfilOperacional perfil,
            Hospital hospital,
            boolean aprovado
    ) {
        this.usuario = usuario;
        this.senhaHash = senhaHash;
        this.nome = nome;
        this.perfil = perfil;
        this.hospital = hospital;
        this.aprovado = aprovado;
    }

    public Long getId() {
        return id;
    }

    public String getUsuario() {
        return usuario;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public String getNome() {
        return nome;
    }

    public PerfilOperacional getPerfil() {
        return perfil;
    }

    public Hospital getHospital() {
        return hospital;
    }

    public boolean isAprovado() {
        return aprovado;
    }

    public void aprovar() {
        aprovado = true;
    }
}
