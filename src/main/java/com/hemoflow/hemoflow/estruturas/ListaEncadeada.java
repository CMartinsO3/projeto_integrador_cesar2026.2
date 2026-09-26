package com.hemoflow.hemoflow.estruturas;

import java.util.ArrayList;
import java.util.List;

/**
 * Lista encadeada simples de uso genérico.
 *
 * <p>Implementação didática que simula o uso de ponteiros por meio de
 * referências de objetos (campo {@code proximo} de cada nó), sem recorrer
 * a nenhuma coleção da {@code java.util} no núcleo da estrutura.</p>
 *
 * <p>Complexidade de referência:</p>
 * <ul>
 *   <li>Inserção no início — O(1)</li>
 *   <li>Inserção no fim — O(n)</li>
 *   <li>Remoção por valor — O(n)</li>
 *   <li>Busca — O(n)</li>
 * </ul>
 *
 * @param <T> tipo do elemento armazenado
 */
public class ListaEncadeada<T> {

    // ------------------------------------------------------------------
    // Nó interno — equivalente à struct No { T dado; No *proximo; } em C
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
    // Estado da lista
    // ------------------------------------------------------------------
    private No<T> cabeca;   // primeiro nó (equivalente ao ponteiro "head")
    private int tamanho;

    /** Cria uma lista vazia. */
    public ListaEncadeada() {
        this.cabeca = null;
        this.tamanho = 0;
    }

    // ------------------------------------------------------------------
    // Operações de inserção
    // ------------------------------------------------------------------

    /**
     * Insere um elemento no início da lista — O(1).
     *
     * @param dado elemento a inserir
     */
    public void inserirNoInicio(T dado) {
        No<T> novo = new No<>(dado);
        novo.proximo = cabeca;
        cabeca = novo;
        tamanho++;
    }

    /**
     * Insere um elemento no fim da lista — O(n).
     *
     * @param dado elemento a inserir
     */
    public void inserirNoFim(T dado) {
        No<T> novo = new No<>(dado);
        if (cabeca == null) {
            cabeca = novo;
        } else {
            No<T> atual = cabeca;
            while (atual.proximo != null) {
                atual = atual.proximo;
            }
            atual.proximo = novo;
        }
        tamanho++;
    }

    // ------------------------------------------------------------------
    // Operações de remoção
    // ------------------------------------------------------------------

    /**
     * Remove a primeira ocorrência do elemento com o dado igual a {@code dado} — O(n).
     *
     * @param dado valor a remover
     * @return {@code true} se o elemento foi encontrado e removido; {@code false} caso contrário
     */
    public boolean remover(T dado) {
        if (cabeca == null) {
            return false;
        }
        // Caso especial: remoção da cabeça
        if (cabeca.dado.equals(dado)) {
            cabeca = cabeca.proximo;
            tamanho--;
            return true;
        }
        // Percorre a lista procurando o antecessor do nó a remover
        No<T> anterior = cabeca;
        while (anterior.proximo != null) {
            if (anterior.proximo.dado.equals(dado)) {
                anterior.proximo = anterior.proximo.proximo; // desencadeia o nó
                tamanho--;
                return true;
            }
            anterior = anterior.proximo;
        }
        return false;
    }

    /**
     * Remove e retorna o primeiro elemento da lista — O(1).
     *
     * @return o primeiro elemento
     * @throws IllegalStateException se a lista estiver vazia
     */
    public T removerDoInicio() {
        if (cabeca == null) {
            throw new IllegalStateException("Lista vazia");
        }
        T dado = cabeca.dado;
        cabeca = cabeca.proximo;
        tamanho--;
        return dado;
    }

    // ------------------------------------------------------------------
    // Operações de consulta
    // ------------------------------------------------------------------

    /**
     * Verifica se o elemento existe na lista — O(n).
     *
     * @param dado valor a buscar
     * @return {@code true} se encontrado
     */
    public boolean contem(T dado) {
        No<T> atual = cabeca;
        while (atual != null) {
            if (atual.dado.equals(dado)) {
                return true;
            }
            atual = atual.proximo;
        }
        return false;
    }

    /**
     * Retorna o elemento na posição {@code indice} (base 0) — O(n).
     *
     * @param indice posição desejada
     * @return elemento na posição
     * @throws IndexOutOfBoundsException se o índice estiver fora do intervalo
     */
    public T obter(int indice) {
        if (indice < 0 || indice >= tamanho) {
            throw new IndexOutOfBoundsException("Índice " + indice + " fora do intervalo [0, " + (tamanho - 1) + "]");
        }
        No<T> atual = cabeca;
        for (int i = 0; i < indice; i++) {
            atual = atual.proximo;
        }
        return atual.dado;
    }

    /** @return número de elementos na lista */
    public int tamanho() {
        return tamanho;
    }

    /** @return {@code true} se a lista não contiver elementos */
    public boolean estaVazia() {
        return tamanho == 0;
    }

    /**
     * Converte a lista encadeada em uma {@link List} Java (para interoperabilidade).
     *
     * @return nova lista com os elementos na mesma ordem
     */
    public List<T> paraLista() {
        List<T> resultado = new ArrayList<>();
        No<T> atual = cabeca;
        while (atual != null) {
            resultado.add(atual.dado);
            atual = atual.proximo;
        }
        return resultado;
    }

    @Override
    public String toString() {
        if (cabeca == null) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        No<T> atual = cabeca;
        while (atual != null) {
            sb.append(atual.dado);
            if (atual.proximo != null) {
                sb.append(" -> ");
            }
            atual = atual.proximo;
        }
        return sb.append("]").toString();
    }
}
