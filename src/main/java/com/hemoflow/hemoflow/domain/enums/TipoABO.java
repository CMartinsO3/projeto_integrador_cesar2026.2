package com.hemoflow.hemoflow.domain.enums;

public enum TipoABO {
    A("A"),
    B("B"),
    AB("AB"),
    O("O");

    private final String valor;

    TipoABO(String valor) {
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }
}
