package com.hemoflow.hemoflow.estruturas;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ListaEncadeadaOrdenadaTest {

    @Test
    @DisplayName("inserirOrdenado mantém a lista em ordem crescente (início, meio e fim)")
    void deveManterOrdemCrescente() {
        ListaEncadeada<Integer> lista = new ListaEncadeada<>();
        for (int n : new int[] {20, 5, 30, 10, 1, 40}) {
            lista.inserirOrdenado(n, Comparator.naturalOrder());
        }
        assertEquals(List.of(1, 5, 10, 20, 30, 40), lista.paraLista());
        assertEquals(6, lista.tamanho());
    }

    @Test
    @DisplayName("Em empate, o novo elemento entra depois dos equivalentes (estável)")
    void empateDeveSerEstavel() {
        ListaEncadeada<String> lista = new ListaEncadeada<>();
        Comparator<String> porTamanho = Comparator.comparingInt(String::length);
        lista.inserirOrdenado("bb", porTamanho);
        lista.inserirOrdenado("cc", porTamanho);
        lista.inserirOrdenado("a", porTamanho);
        lista.inserirOrdenado("dd", porTamanho);
        assertEquals(List.of("a", "bb", "cc", "dd"), lista.paraLista());
    }

    @Test
    @DisplayName("removerDoInicio depois de inserirOrdenado devolve sempre o menor")
    void removerDoInicioDevolveOMenor() {
        ListaEncadeada<Integer> lista = new ListaEncadeada<>();
        lista.inserirOrdenado(7, Comparator.naturalOrder());
        lista.inserirOrdenado(3, Comparator.naturalOrder());
        lista.inserirOrdenado(5, Comparator.naturalOrder());
        assertEquals(3, lista.removerDoInicio());
        assertEquals(5, lista.removerDoInicio());
        assertEquals(7, lista.removerDoInicio());
    }
}
