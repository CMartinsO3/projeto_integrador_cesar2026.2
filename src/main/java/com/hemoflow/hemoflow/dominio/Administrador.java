package com.hemoflow.hemoflow.dominio;

import jakarta.persistence.*;

@Entity
@Table(name = "administrador")
public class Administrador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String usuario;

    @Column(nullable = false)
    private String senha;

    @Column(nullable = false)
    private String nomeCompleto;

    protected Administrador() {}

    public Administrador(String usuario, String senha, String nomeCompleto) {
        this.usuario = usuario;
        this.senha = senha;
        this.nomeCompleto = nomeCompleto;
    }

    public Long getId() { return id; }
    public String getUsuario() { return usuario; }
    public String getSenha() { return senha; }
    public String getNomeCompleto() { return nomeCompleto; }
}
