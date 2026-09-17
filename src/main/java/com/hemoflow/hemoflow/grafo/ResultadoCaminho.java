package com.hemoflow.hemoflow.grafo;

import java.util.List;

public record ResultadoCaminho(boolean existeRota, List<String> caminho, int custoTotalMinutos) {
    public static ResultadoCaminho inexistente() {
        return new ResultadoCaminho(false, List.of(), 0);
    }
}
