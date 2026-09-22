package com.hemoflow.hemoflow.domain.enums;

public enum ModoProcessamento {
    SEQUENCIAL("Sequencial"),
    PARALELO("Paralelo");

    private final String descricao;

    ModoProcessamento(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
