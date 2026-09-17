package com.hemoflow.hemoflow.grafo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class GrafoTest {

    @Test
    void calculaCaminhoMinimoComNoIntermediario() {
        Grafo grafo = new Grafo();
        grafo.adicionarAresta("HC", "H1", 25);
        grafo.adicionarAresta("HC", "INT", 15);
        grafo.adicionarAresta("INT", "H3", 20);
        grafo.adicionarAresta("HC", "H3", 50);

        ResultadoCaminho resultado = grafo.caminhoMinimo("HC", "H3");

        assertTrue(resultado.existeRota());
        assertEquals(35, resultado.custoTotalMinutos());
        assertEquals(java.util.List.of("HC", "INT", "H3"), resultado.caminho());
    }

    @Test
    void informaQuandoNaoHaRota() {
        Grafo grafo = new Grafo();
        grafo.adicionarNo("HC");
        grafo.adicionarNo("ISOLADO");

        ResultadoCaminho resultado = grafo.caminhoMinimo("HC", "ISOLADO");

        assertFalse(resultado.existeRota());
    }
}
