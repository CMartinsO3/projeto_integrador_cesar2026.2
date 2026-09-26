package com.hemoflow.hemoflow.estruturas;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EstruturasTest {

    @Test
    @DisplayName("FilaSimples deve enfileirar, desenfileirar e espiar em ordem FIFO")
    void testFilaSimples() {
        FilaSimples<String> fila = new FilaSimples<>();
        assertTrue(fila.estaVazia());
        assertEquals(0, fila.tamanho());

        fila.enfileirar("A");
        fila.enfileirar("B");
        fila.enfileirar("C");

        assertFalse(fila.estaVazia());
        assertEquals(3, fila.tamanho());
        assertEquals("A", fila.espiar());

        assertEquals("A", fila.desenfileirar());
        assertEquals(2, fila.tamanho());
        assertEquals("B", fila.espiar());

        assertEquals("B", fila.desenfileirar());
        assertEquals("C", fila.desenfileirar());

        assertTrue(fila.estaVazia());
        assertThrows(IllegalStateException.class, fila::desenfileirar);
        assertThrows(IllegalStateException.class, fila::espiar);
    }

    @Test
    @DisplayName("PilhaSimples deve empilhar, desempilhar e espiar em ordem LIFO")
    void testPilhaSimples() {
        PilhaSimples<Integer> pilha = new PilhaSimples<>();
        assertTrue(pilha.estaVazia());
        assertEquals(0, pilha.tamanho());

        pilha.empilhar(10);
        pilha.empilhar(20);
        pilha.empilhar(30);

        assertFalse(pilha.estaVazia());
        assertEquals(3, pilha.tamanho());
        assertEquals(30, pilha.espiar());

        assertEquals(30, pilha.desempilhar());
        assertEquals(2, pilha.tamanho());
        assertEquals(20, pilha.espiar());

        assertEquals(20, pilha.desempilhar());
        assertEquals(10, pilha.desempilhar());

        assertTrue(pilha.estaVazia());
        assertThrows(IllegalStateException.class, pilha::desempilhar);
        assertThrows(IllegalStateException.class, pilha::espiar);
    }

    @Test
    @DisplayName("ListaEncadeada deve manipular inserções no início e fim, remoções e buscas")
    void testListaEncadeada() {
        ListaEncadeada<String> lista = new ListaEncadeada<>();
        assertTrue(lista.estaVazia());
        assertEquals(0, lista.tamanho());

        lista.inserirNoFim("B");
        lista.inserirNoInicio("A");
        lista.inserirNoFim("C");

        assertEquals(3, lista.tamanho());
        assertEquals("A", lista.obter(0));
        assertEquals("B", lista.obter(1));
        assertEquals("C", lista.obter(2));
        assertTrue(lista.contem("B"));
        assertFalse(lista.contem("Z"));

        assertEquals(List.of("A", "B", "C"), lista.paraLista());

        assertEquals("A", lista.removerDoInicio());
        assertEquals(2, lista.tamanho());

        assertTrue(lista.remover("C"));
        assertEquals(1, lista.tamanho());
        assertFalse(lista.remover("Inexistente"));

        assertThrows(IndexOutOfBoundsException.class, () -> lista.obter(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> lista.obter(5));
    }
}
