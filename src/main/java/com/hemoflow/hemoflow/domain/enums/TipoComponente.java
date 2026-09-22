package com.hemoflow.hemoflow.domain.enums;

public enum TipoComponente {
    CONCENTRADO_HEMACIAS("Concentrado de Hemácias", 42),
    PLASMA_FRESCO("Plasma Fresco", 365),
    CONCENTRADO_PLAQUETAS("Concentrado de Plaquetas", 5),
    CRIOPRECIPITADO("Crioprecipitado", 365);

    private final String descricao;
    private final int diasValidade;

    TipoComponente(String descricao, int diasValidade) {
        this.descricao = descricao;
        this.diasValidade = diasValidade;
    }

    public String getDescricao() {
        return descricao;
    }

    public int getDiasValidade() {
        return diasValidade;
    }
}
