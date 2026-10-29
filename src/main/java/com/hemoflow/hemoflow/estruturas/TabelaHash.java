package com.hemoflow.hemoflow.estruturas;

import java.util.ArrayList;
import java.util.List;

/**
 * Tabela hash própria, com encadeamento, para chaves {@code String} (Unidade 2).
 *
 * <p>Versão C/C++ equivalente: {@code c_structures/tabela_hash.cpp}. Não usa
 * {@code HashMap} nem {@code String.hashCode()}: a função hash, o cálculo da
 * posição e o tratamento de colisão estão neste arquivo.</p>
 *
 * <p>Funcionamento: a função {@link #hash(String)} transforma a chave em um
 * número; o resto da divisão desse número pelo tamanho da tabela dá a posição
 * (o "bucket"). Chaves que caem na mesma posição (colisão) ficam encadeadas
 * em uma lista de {@code Entrada}, como em uma lista encadeada simples.</p>
 *
 * <p>Complexidade: inserir, buscar e remover custam O(1) em média, desde que
 * o fator de carga ({@link #fatorDeCarga()}) seja baixo e as chaves se
 * espalhem; no pior caso (todas as chaves no mesmo bucket) custam O(n).
 * Espaço O(capacidade + n). A tabela tem tamanho fixo (não faz rehash).</p>
 *
 * @param <V> tipo do valor guardado
 */
public class TabelaHash<V> {

    /** 67 posições (número primo): para 32 chaves possíveis, fator de carga ~ 0,48. */
    public static final int CAPACIDADE_PADRAO = 67;

    // Nó da cadeia — equivalente ao struct Grupo { char chave[]; ...; Grupo *proximo; } em C
    private static class Entrada<V> {
        final String chave;
        V valor;
        Entrada<V> proximo;

        Entrada(String chave, V valor) {
            this.chave = chave;
            this.valor = valor;
        }
    }

    private final Entrada<V>[] buckets;
    private int tamanho;

    public TabelaHash() {
        this(CAPACIDADE_PADRAO);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public TabelaHash(int capacidade) {
        if (capacidade < 1) {
            throw new IllegalArgumentException("Capacidade deve ser pelo menos 1");
        }
        this.buckets = (Entrada<V>[]) new Entrada[capacidade];
        this.tamanho = 0;
    }

    /**
     * Função hash: {@code h = h * 31 + caractere}, com aritmética de 32 bits
     * (o estouro de {@code int} equivale ao {@code uint32_t} da versão C; para
     * chaves ASCII o resultado é idêntico nas duas linguagens).
     */
    public static int hash(String chave) {
        int h = 0;
        for (int i = 0; i < chave.length(); i++) {
            h = h * 31 + chave.charAt(i);
        }
        return h;
    }

    /** Posição da chave: o hash lido como número sem sinal, módulo a capacidade. */
    public int posicao(String chave) {
        return Integer.remainderUnsigned(hash(chave), buckets.length);
    }

    /**
     * Guarda o valor para a chave. Se a chave já existia, troca o valor.
     *
     * @return o valor anterior, ou {@code null} se a chave era nova
     */
    public V inserir(String chave, V valor) {
        validar(chave);
        int pos = posicao(chave);
        for (Entrada<V> e = buckets[pos]; e != null; e = e.proximo) {
            if (e.chave.equals(chave)) {
                V anterior = e.valor;
                e.valor = valor;
                return anterior;
            }
        }
        Entrada<V> nova = new Entrada<>(chave, valor);
        nova.proximo = buckets[pos];        // encadeamento: entra no início do bucket
        buckets[pos] = nova;
        tamanho++;
        return null;
    }

    /** Devolve o valor da chave, ou {@code null} se ela não existir. */
    public V buscar(String chave) {
        validar(chave);
        for (Entrada<V> e = buckets[posicao(chave)]; e != null; e = e.proximo) {
            if (e.chave.equals(chave)) {
                return e.valor;
            }
        }
        return null;
    }

    public boolean contem(String chave) {
        validar(chave);
        for (Entrada<V> e = buckets[posicao(chave)]; e != null; e = e.proximo) {
            if (e.chave.equals(chave)) {
                return true;
            }
        }
        return false;
    }

    /** Remove a chave. Retorna {@code true} se ela existia. */
    public boolean remover(String chave) {
        validar(chave);
        int pos = posicao(chave);
        Entrada<V> anterior = null;
        for (Entrada<V> e = buckets[pos]; e != null; e = e.proximo) {
            if (e.chave.equals(chave)) {
                if (anterior == null) {
                    buckets[pos] = e.proximo;       // era a cabeça do bucket
                } else {
                    anterior.proximo = e.proximo;   // desencadeia do meio/fim da cadeia
                }
                tamanho--;
                return true;
            }
            anterior = e;
        }
        return false;
    }

    /** Remove todas as chaves. */
    public void limpar() {
        for (int i = 0; i < buckets.length; i++) {
            buckets[i] = null;
        }
        tamanho = 0;
    }

    /** Todas as chaves guardadas (ordem por bucket, não por inserção). */
    public List<String> chaves() {
        List<String> resultado = new ArrayList<>();
        for (Entrada<V> inicio : buckets) {
            for (Entrada<V> e = inicio; e != null; e = e.proximo) {
                resultado.add(e.chave);
            }
        }
        return resultado;
    }

    public int tamanho() {
        return tamanho;
    }

    public boolean estaVazia() {
        return tamanho == 0;
    }

    public int capacidade() {
        return buckets.length;
    }

    /** Chaves guardadas dividido pela capacidade. */
    public double fatorDeCarga() {
        return (double) tamanho / buckets.length;
    }

    /** Tamanho da maior cadeia (o pior bucket). */
    public int maiorCadeia() {
        int maior = 0;
        for (Entrada<V> inicio : buckets) {
            int n = 0;
            for (Entrada<V> e = inicio; e != null; e = e.proximo) {
                n++;
            }
            maior = Math.max(maior, n);
        }
        return maior;
    }

    private static void validar(String chave) {
        if (chave == null || chave.isEmpty()) {
            throw new IllegalArgumentException("Chave não pode ser nula nem vazia");
        }
    }
}
