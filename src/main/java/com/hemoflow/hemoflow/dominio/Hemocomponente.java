package com.hemoflow.hemoflow.dominio;

public enum Hemocomponente {
    HEMACIAS(120),
    PLASMA(120),
    PLAQUETAS(90),
    CRIOPRECIPITADO(120);

    private final int limiteCadeiaFriaMinutos;

    Hemocomponente(int limiteCadeiaFriaMinutos) {
        this.limiteCadeiaFriaMinutos = limiteCadeiaFriaMinutos;
    }

    public int getLimiteCadeiaFriaMinutos() {
        return limiteCadeiaFriaMinutos;
    }
}
