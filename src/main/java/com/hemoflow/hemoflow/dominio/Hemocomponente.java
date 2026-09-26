package com.hemoflow.hemoflow.dominio;

public enum Hemocomponente {
    HEMACIAS(120, "Hemácias"),
    PLASMA(120, "Plasma"),
    PLAQUETAS(90, "Plaquetas"),
    CRIOPRECIPITADO(120, "Crioprecipitado");

    private final int limiteCadeiaFriaMinutos;
    private final String rotulo;

    Hemocomponente(int limiteCadeiaFriaMinutos, String rotulo) {
        this.limiteCadeiaFriaMinutos = limiteCadeiaFriaMinutos;
        this.rotulo = rotulo;
    }

    public int getLimiteCadeiaFriaMinutos() {
        return limiteCadeiaFriaMinutos;
    }

    public String getRotulo() {
        return rotulo;
    }

    public static String rotuloDe(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return "—";
        }
        try {
            return valueOf(codigo.trim()).getRotulo();
        } catch (IllegalArgumentException e) {
            return codigo;
        }
    }
}
