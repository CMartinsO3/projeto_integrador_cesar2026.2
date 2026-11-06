package com.hemoflow.hemoflow.algoritmos;

import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import org.junit.jupiter.api.Test;

class AlgoritmosU2Test {
    @Test
    void buscasSequencialEbinaria() {
        int[] ordenado = {1, 3, 5, 7, 9};
        int[] repetidos = {1, 3, 3, 3, 9};

        assertEquals(2, BuscaOrdenacao.sequencial(ordenado, 5));
        assertEquals(-1, BuscaOrdenacao.sequencial(ordenado, 2));
        assertEquals(1, BuscaOrdenacao.sequencial(repetidos, 3));
        assertEquals(-1, BuscaOrdenacao.sequencial(new int[0], 3));
        assertEquals(0, BuscaOrdenacao.sequencial(new int[]{8}, 8));

        assertEquals(3, BuscaOrdenacao.binaria(ordenado, 7));
        assertEquals(-1, BuscaOrdenacao.binaria(ordenado, 2));
        int indiceRepetido = BuscaOrdenacao.binaria(repetidos, 3);
        assertTrue(indiceRepetido >= 1 && indiceRepetido <= 3);
        assertEquals(-1, BuscaOrdenacao.binaria(new int[0], 3));
        assertEquals(0, BuscaOrdenacao.binaria(new int[]{8}, 8));
    }

    @Test
    void quatroOrdenacoesEmDiferentesEntradas() {
        List<int[]> casos = List.of(
            new int[]{9, 3, 7, 1, 5, 2, 8, 6, 4},
            new int[]{5, 2, 5, -1, 0, 2, -8, 5},
            new int[0],
            new int[]{42},
            new int[]{-3, -1, 0, 4, 9},
            new int[]{9, 4, 0, -1, -3}
        );

        for (int[] entrada : casos) {
            int[] esperado = entrada.clone();
            Arrays.sort(esperado);

            int[] bubble = entrada.clone();
            BuscaOrdenacao.bubble(bubble);
            assertArrayEquals(esperado, bubble, "Bubble Sort");

            int[] selection = entrada.clone();
            BuscaOrdenacao.selection(selection);
            assertArrayEquals(esperado, selection, "Selection Sort");

            int[] insertion = entrada.clone();
            BuscaOrdenacao.insertion(insertion);
            assertArrayEquals(esperado, insertion, "Insertion Sort");

            int[] merge = entrada.clone();
            BuscaOrdenacao.merge(merge);
            assertArrayEquals(esperado, merge, "Merge Sort");
        }
    }

    @Test
    void vizinhoMaisProximoEscolheMenorArestaDisponivel() {
        Map<String, List<RoteadorVizinhoMaisProximo.Aresta>> grafo = new HashMap<>();
        grafo.put("H", List.of(
            new RoteadorVizinhoMaisProximo.Aresta("C", 4),
            new RoteadorVizinhoMaisProximo.Aresta("X", 2)
        ));
        grafo.put("X", List.of(new RoteadorVizinhoMaisProximo.Aresta("C", 3)));
        grafo.put("C", List.of());
        grafo.put("Z", List.of());

        var resultado = RoteadorVizinhoMaisProximo.calcular(grafo, "H", "C");
        assertTrue(resultado.encontrado());
        assertEquals(List.of("H", "X", "C"), resultado.rota());
        assertEquals(5L, resultado.custoTotal());

        var mesmoNo = RoteadorVizinhoMaisProximo.calcular(grafo, "H", "H");
        assertTrue(mesmoNo.encontrado());
        assertEquals(List.of("H"), mesmoNo.rota());
        assertEquals(0, mesmoNo.custoTotal());

        var semRota = RoteadorVizinhoMaisProximo.calcular(grafo, "H", "Z");
        assertFalse(semRota.encontrado());
        assertEquals(List.of("H", "X", "C"), semRota.rota());
        assertEquals(5L, semRota.custoTotal());
        assertFalse(RoteadorVizinhoMaisProximo.calcular(grafo, "Q", "C").encontrado());
    }

    @Test
    void vizinhoMaisProximoSegueEscolhaLocalMesmoSemSerOtimoGlobal() {
        Map<String, List<RoteadorVizinhoMaisProximo.Aresta>> grafo = new HashMap<>();
        grafo.put("A", List.of(
            new RoteadorVizinhoMaisProximo.Aresta("B", 1),
            new RoteadorVizinhoMaisProximo.Aresta("C", 10)
        ));
        grafo.put("B", List.of(new RoteadorVizinhoMaisProximo.Aresta("D", 100)));
        grafo.put("C", List.of(new RoteadorVizinhoMaisProximo.Aresta("D", 10)));
        grafo.put("D", List.of());

        var resultado = RoteadorVizinhoMaisProximo.calcular(grafo, "A", "D");

        assertTrue(resultado.encontrado());
        assertEquals(List.of("A", "B", "D"), resultado.rota());
        assertEquals(101L, resultado.custoTotal());
    }

    @Test
    void vizinhoMaisProximoDesempataPeloIdentificador() {
        Map<String, List<RoteadorVizinhoMaisProximo.Aresta>> grafo = new HashMap<>();
        grafo.put("A", List.of(
            new RoteadorVizinhoMaisProximo.Aresta("Z", 5),
            new RoteadorVizinhoMaisProximo.Aresta("B", 5)
        ));
        grafo.put("B", List.of());
        grafo.put("Z", List.of());

        var resultado = RoteadorVizinhoMaisProximo.calcular(grafo, "A", "B");

        assertTrue(resultado.encontrado());
        assertEquals(List.of("A", "B"), resultado.rota());
        assertEquals(5L, resultado.custoTotal());
    }

    @Test
    void vizinhoMaisProximoPodeNaoAcharDestinoMesmoHavendoOutroCaminho() {
        Map<String, List<RoteadorVizinhoMaisProximo.Aresta>> grafo = new HashMap<>();
        grafo.put("A", List.of(
            new RoteadorVizinhoMaisProximo.Aresta("B", 1),
            new RoteadorVizinhoMaisProximo.Aresta("C", 10)
        ));
        grafo.put("B", List.of());
        grafo.put("C", List.of(new RoteadorVizinhoMaisProximo.Aresta("D", 10)));
        grafo.put("D", List.of());

        var resultado = RoteadorVizinhoMaisProximo.calcular(grafo, "A", "D");

        assertFalse(resultado.encontrado());
        assertEquals(List.of("A", "B"), resultado.rota());
        assertEquals(1L, resultado.custoTotal());
    }

    @Test
    void roteadorUsaCustoLongoSemOverflowDeInteiro() {
        Map<String, List<RoteadorVizinhoMaisProximo.Aresta>> grafo = new HashMap<>();
        grafo.put("A", List.of(new RoteadorVizinhoMaisProximo.Aresta("B", Integer.MAX_VALUE)));
        grafo.put("B", List.of(new RoteadorVizinhoMaisProximo.Aresta("C", Integer.MAX_VALUE)));
        grafo.put("C", List.of());

        var resultado = RoteadorVizinhoMaisProximo.calcular(grafo, "A", "C");

        assertTrue(resultado.encontrado());
        assertEquals(2L * Integer.MAX_VALUE, resultado.custoTotal());
    }
}
