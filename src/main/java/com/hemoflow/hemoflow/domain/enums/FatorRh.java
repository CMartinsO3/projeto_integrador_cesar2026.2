package com.hemoflow.hemoflow.domain.enums;

public enum FatorRh {
    POSITIVO("Positivo"),
    NEGATIVO("Negativo");

    private final String valor;

    FatorRh(String valor) {
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }
}
