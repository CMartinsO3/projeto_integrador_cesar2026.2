package com.hemoflow.hemoflow.domain.enums;

public enum NivelUrgencia {
    ROTINA("Rotina", 3),
    URGENTE("Urgente", 2),
    EMERGENCIA("Emergência", 1);

    private final String descricao;
    private final int prioridade;

    NivelUrgencia(String descricao, int prioridade) {
        this.descricao = descricao;
        this.prioridade = prioridade;
    }

    public String getDescricao() {
        return descricao;
    }

    public int getPrioridade() {
        return prioridade;
    }
}
