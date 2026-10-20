package com.hemoflow.hemoflow.grafo;

import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GrafoTest {

    @Test
    void deveEncontrarCaminhoIndiretoMaisCurtoQueArestaDireta() {
        Grafo grafo = new Grafo();
        grafo.adicionarAresta(1L, 3L, 180);
        grafo.adicionarAresta(1L, 2L, 70);
        grafo.adicionarAresta(2L, 3L, 60);

        Grafo.ResultadoCaminhoMinimo res = grafo.calcularCaminhoMinimo(1L, 3L);

        assertTrue(res.existeRota());
        assertEquals(130, res.custoTotalMinutos());
        assertEquals(List.of(1L, 2L, 3L), res.caminho());
    }

    @Test
    void deveRetornarCustoZeroECaminhoUnitarioQuandoOrigemIgualAoDestino() {
        Grafo grafo = new Grafo();
        grafo.adicionarVertice(10L);

        Grafo.ResultadoCaminhoMinimo res = grafo.calcularCaminhoMinimo(10L, 10L);

        assertTrue(res.existeRota());
        assertEquals(0, res.custoTotalMinutos());
        assertEquals(List.of(10L), res.caminho());
    }

    @Test
    void deveRetornarExisteRotaFalseQuandoVerticesDesconexos() {
        Grafo grafo = new Grafo();
        grafo.adicionarAresta(1L, 2L, 30);
        grafo.adicionarVertice(3L);

        Grafo.ResultadoCaminhoMinimo res = grafo.calcularCaminhoMinimo(1L, 3L);

        assertFalse(res.existeRota());
        assertEquals(0, res.custoTotalMinutos());
        assertTrue(res.caminho().isEmpty());
    }

    @Test
    void deveFuncionarNosDoisSentidosPorSerGrafoNaoDirecionado() {
        Grafo grafo = new Grafo();
        grafo.adicionarAresta(1L, 2L, 45);

        Grafo.ResultadoCaminhoMinimo ida = grafo.calcularCaminhoMinimo(1L, 2L);
        Grafo.ResultadoCaminhoMinimo volta = grafo.calcularCaminhoMinimo(2L, 1L);

        assertEquals(45, ida.custoTotalMinutos());
        assertEquals(List.of(1L, 2L), ida.caminho());
        assertEquals(45, volta.custoTotalMinutos());
        assertEquals(List.of(2L, 1L), volta.caminho());
    }

    @Test
    void deveRejeitarArestaComPesoNaoPositivo() {
        Grafo grafo = new Grafo();
        assertThrows(IllegalArgumentException.class, () -> grafo.adicionarAresta(1L, 2L, 0));
    }
}