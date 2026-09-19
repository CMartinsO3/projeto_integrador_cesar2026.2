package com.hemoflow.hemoflow.dominio;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "doador")
public class Doador {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false) private String nome;
    @Column(nullable = false, unique = true) private String email;
    @Column(nullable = false) private String senha;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private TipoSanguineo tipoSanguineo;
    @Column(nullable = false) private LocalDate dataNascimento;
    @Column(nullable = false) private boolean ativo = true;

    protected Doador() {}
    public Doador(String nome, String email, String senha, TipoSanguineo tipoSanguineo, LocalDate dataNascimento) {
        this.nome = nome; this.email = email; this.senha = senha;
        this.tipoSanguineo = tipoSanguineo; this.dataNascimento = dataNascimento;
    }
    public Long getId() { return id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
    public TipoSanguineo getTipoSanguineo() { return tipoSanguineo; }
    public void setTipoSanguineo(TipoSanguineo tipoSanguineo) { this.tipoSanguineo = tipoSanguineo; }
    public LocalDate getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
}