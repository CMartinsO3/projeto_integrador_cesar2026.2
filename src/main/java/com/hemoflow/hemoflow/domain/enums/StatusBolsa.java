package com.hemoflow.hemoflow.domain.enums;

public enum StatusBolsa {
    DISPONIVEL("Disponível"),
    ALOCADA("Alocada"),
    EM_TRANSITO("Em Trânsito"),
    ENTREGUE("Entregue"),
    DESCARTADA("Descartada");

    private final String descricao;

    StatusBolsa(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
