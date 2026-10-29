package com.hemoflow.hemoflow.estoque;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

import com.hemoflow.hemoflow.dominio.Bolsa;
import com.hemoflow.hemoflow.estruturas.ListaEncadeada;

/**
 * FEFO (First Expired, First Out) por inserção ordenada (Unidade 2).
 *
 * <p>Versão C/C++ equivalente: {@code c_structures/fefo.cpp}.</p>
 *
 * <p>As bolsas ficam em uma {@link ListaEncadeada} mantida sempre ordenada por
 * validade crescente: cada bolsa nova é inserida no lugar certo e a primeira
 * da lista é sempre a que vence antes. Desempate: menor id da bolsa primeiro
 * (id ainda não gerado vem por último) e, persistindo o empate, ordem de
 * chegada. Bolsas já vencidas são descartadas na entrada e também ao retirar
 * (caso tenham vencido enquanto aguardavam na lista).</p>
 *
 * <p>Complexidade: {@link #adicionar} O(n) (percorre a lista até o ponto
 * certo); {@link #proxima} O(1) quando nada venceu (só mexe na cabeça);
 * espaço O(n), um nó por bolsa.</p>
 */
public class FilaFEFO {

    /** Validade crescente; empate: menor id primeiro, id nulo por último. */
    private static final Comparator<Bolsa> ORDEM_FEFO =
            Comparator.comparing(Bolsa::getDataValidade)
                    .thenComparing(Bolsa::getId, Comparator.nullsLast(Comparator.naturalOrder()));

    private final ListaEncadeada<Bolsa> fila = new ListaEncadeada<>();
    private final Clock relogio;

    public FilaFEFO() {
        this(Clock.systemDefaultZone());
    }

    /** Permite fixar a data "de hoje" (usado nos testes). */
    public FilaFEFO(Clock relogio) {
        this.relogio = relogio;
    }

    /**
     * Insere a bolsa na posição certa da fila.
     *
     * @return {@code true} se entrou; {@code false} se a bolsa é nula ou já está vencida
     */
    public boolean adicionar(Bolsa bolsa) {
        if (bolsa == null || venceu(bolsa)) {
            return false;
        }
        fila.inserirOrdenado(bolsa, ORDEM_FEFO);
        return true;
    }

    /** Adiciona cada bolsa da lista (as vencidas são descartadas). */
    public void adicionarTodas(List<Bolsa> bolsas) {
        for (Bolsa bolsa : bolsas) {
            adicionar(bolsa);
        }
    }

    /**
     * Retira e devolve a bolsa válida que vence primeiro, ou {@code null} se
     * não houver nenhuma. Bolsas que venceram dentro da fila são descartadas
     * (elas ficam sempre na frente, pois a lista é ordenada por validade).
     */
    public Bolsa proxima() {
        while (!fila.estaVazia()) {
            Bolsa primeira = fila.removerDoInicio();
            if (!venceu(primeira)) {
                return primeira;
            }
        }
        return null;
    }

    /**
     * Remove a bolsa com o id informado, em qualquer posição da fila — O(n).
     *
     * @return {@code true} se a bolsa estava na fila
     */
    public boolean removerPorId(Long id) {
        return id != null && fila.removerSe(b -> id.equals(b.getId()));
    }

    /**
     * Descarta as bolsas que já venceram (ficam sempre na frente da fila).
     *
     * @return quantas foram descartadas
     */
    public int descartarVencidas() {
        int descartadas = 0;
        while (!fila.estaVazia() && venceu(fila.obter(0))) {
            fila.removerDoInicio();
            descartadas++;
        }
        return descartadas;
    }

    public boolean estaVazia() {
        return fila.estaVazia();
    }

    public int tamanho() {
        return fila.tamanho();
    }

    private boolean venceu(Bolsa bolsa) {
        return bolsa.getDataValidade().isBefore(LocalDate.now(relogio));
    }
}
