package com.hemoflow.hemoflow.estruturas;

/**
 * Fila genérica com política FIFO (First-In, First-Out).
 *
 * <p>Implementação didática baseada em lista encadeada com dois ponteiros
 * ({@code frente} e {@code cauda}), eliminando a necessidade de percorrer
 * a estrutura inteira para enfileirar — ambas as operações principais
 * são O(1).</p>
 *
 * <p>Sem uso de {@code java.util.Queue}, {@code LinkedList} ou qualquer
 * outra coleção da biblioteca padrão no núcleo da estrutura.</p>
 *
 * @param <T> tipo do elemento armazenado
 */
public class FilaSimples<T> {

    // ------------------------------------------------------------------
    // Nó interno — struct No { T dado; No *proximo; } em C
    // ------------------------------------------------------------------
    private static class No<T> {
        T dado;
        No<T> proximo;

        No(T dado) {
            this.dado = dado;
            this.proximo = null;
        }
    }

    // ------------------------------------------------------------------
    // Estado da fila
    // ------------------------------------------------------------------
    /** Ponteiro para o nó que será desenfileirado primeiro. */
    private No<T> frente;

    /** Ponteiro para o nó onde o próximo elemento será enfileirado. */
    private No<T> cauda;

    private int tamanho;

    /** Cria uma fila vazia. */
    public FilaSimples() {
        this.frente = null;
        this.cauda = null;
        this.tamanho = 0;
    }

    // ------------------------------------------------------------------
    // Operações principais
    // ------------------------------------------------------------------

    /**
     * Insere um elemento no fim da fila — O(1).
     *
     * @param dado elemento a enfileirar
     */
    public void enfileirar(T dado) {
        No<T> novo = new No<>(dado);
        if (cauda == null) {
            // Fila vazia: frente e cauda apontam para o único nó
            frente = novo;
        } else {
            cauda.proximo = novo;
        }
        cauda = novo;
        tamanho++;
    }

    /**
     * Remove e retorna o elemento do início da fila — O(1).
     *
     * @return o elemento que estava na frente da fila
     * @throws IllegalStateException se a fila estiver vazia
     */
    public T desenfileirar() {
        if (frente == null) {
            throw new IllegalStateException("Fila vazia — não é possível desenfileirar");
        }
        T dado = frente.dado;
        frente = frente.proximo;
        if (frente == null) {
            cauda = null; // fila ficou vazia; ajusta também o ponteiro de cauda
        }
        tamanho--;
        return dado;
    }

    /**
     * Retorna (sem remover) o elemento do início da fila — O(1).
     *
     * @return o elemento que será o próximo a sair
     * @throws IllegalStateException se a fila estiver vazia
     */
    public T espiar() {
        if (frente == null) {
            throw new IllegalStateException("Fila vazia — não há elemento para espiar");
        }
        return frente.dado;
    }

    // ------------------------------------------------------------------
    // Consultas de estado
    // ------------------------------------------------------------------

    /** @return número de elementos na fila */
    public int tamanho() {
        return tamanho;
    }

    /** @return {@code true} se a fila não contiver elementos */
    public boolean estaVazia() {
        return tamanho == 0;
    }

    @Override
    public String toString() {
        if (frente == null) {
            return "Fila[]";
        }
        StringBuilder sb = new StringBuilder("Fila[frente → ");
        No<T> atual = frente;
        while (atual != null) {
            sb.append(atual.dado);
            if (atual.proximo != null) {
                sb.append(", ");
            }
            atual = atual.proximo;
        }
        return sb.append(" ← cauda]").toString();
    }
}
