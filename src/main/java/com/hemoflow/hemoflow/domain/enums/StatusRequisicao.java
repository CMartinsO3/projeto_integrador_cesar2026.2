package com.hemoflow.hemoflow.domain.enums;

public enum StatusRequisicao {
    PENDENTE("Pendente"),
    AGUARDANDO_ESTOQUE("Aguardando Estoque"),
    ALOCADA("Alocada"),
    EM_TRANSITO("Em Trânsito"),
    ATENDIDA("Atendida"),
    CANCELADA("Cancelada");

    private final String descricao;

    StatusRequisicao(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
