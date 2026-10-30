package com.hemoflow.hemoflow.estruturas;

/**
 * Pilha genérica com política LIFO (Last-In, First-Out).
 *
 * <p>Implementação didática baseada em lista encadeada com um único
 * ponteiro de topo ({@code topo}). Empilhar e desempilhar são O(1).</p>
 *
 * <p>Sem uso de {@code java.util.Stack}, {@code Deque} ou qualquer
 * outra coleção da biblioteca padrão no núcleo da estrutura.</p>
 *
 * <p>Uso típico no domínio: registro do histórico de operações sobre
 * uma bolsa (cada operação é empilhada; o topo sempre representa
 * o estado mais recente).</p>
 *
 * @param <T> tipo do elemento armazenado
 */
public class PilhaSimples<T> {

    // ------------------------------------------------------------------
    // Nó interno — struct No { T dado; No *proximo; } em C
    // ------------------------------------------------------------------
    private static class No<T> {
        T dado;
        No<T> proximo; // aponta para o elemento empilhado antes deste

        No(T dado) {
            this.dado = dado;
            this.proximo = null;
        }
    }

    // ------------------------------------------------------------------
    // Estado da pilha
    // ------------------------------------------------------------------
    /** Ponteiro para o nó do topo (último elemento inserido). */
    private No<T> topo;

    private int tamanho;

    /** Cria uma pilha vazia. */
    public PilhaSimples() {
        this.topo = null;
        this.tamanho = 0;
    }

    // ------------------------------------------------------------------
    // Operações principais
    // ------------------------------------------------------------------

    /**
     * Empilha um elemento no topo — O(1).
     *
     * @param dado elemento a empilhar
     */
    public void empilhar(T dado) {
        No<T> novo = new No<>(dado);
        novo.proximo = topo; // o novo nó aponta para o antigo topo
        topo = novo;         // topo passa a ser o novo nó
        tamanho++;
    }

    /**
     * Remove e retorna o elemento do topo — O(1).
     *
     * @return o elemento que estava no topo
     * @throws IllegalStateException se a pilha estiver vazia
     */
    public T desempilhar() {
        if (topo == null) {
            throw new IllegalStateException("Pilha vazia — não é possível desempilhar");
        }
        T dado = topo.dado;
        topo = topo.proximo; // topo recua para o elemento anterior
        tamanho--;
        return dado;
    }

    /**
     * Retorna (sem remover) o elemento do topo — O(1).
     *
     * @return o elemento no topo
     * @throws IllegalStateException se a pilha estiver vazia
     */
    public T espiar() {
        if (topo == null) {
            throw new IllegalStateException("Pilha vazia — não há elemento para espiar");
        }
        return topo.dado;
    }

    // ------------------------------------------------------------------
    // Consultas de estado
    // ------------------------------------------------------------------

    /** @return número de elementos na pilha */
    public int tamanho() {
        return tamanho;
    }

    /** @return {@code true} se a pilha não contiver elementos */
    public boolean estaVazia() {
        return tamanho == 0;
    }

    @Override
    public String toString() {
        if (topo == null) {
            return "Pilha[]";
        }
        StringBuilder sb = new StringBuilder("Pilha[topo → ");
        No<T> atual = topo;
        while (atual != null) {
            sb.append(atual.dado);
            if (atual.proximo != null) {
                sb.append(", ");
            }
            atual = atual.proximo;
        }
        return sb.append("]").toString();
    }
}
