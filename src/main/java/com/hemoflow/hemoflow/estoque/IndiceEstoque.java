package com.hemoflow.hemoflow.estoque;

import java.time.Clock;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.hemoflow.hemoflow.dominio.Bolsa;
import com.hemoflow.hemoflow.dominio.Hemocomponente;
import com.hemoflow.hemoflow.dominio.TipoSanguineo;
import com.hemoflow.hemoflow.estruturas.TabelaHash;

/**
 * Índice em memória do estoque disponível, por tabela hash (Unidade 2).
 *
 * <p>Versão C/C++ equivalente: {@code c_structures/tabela_hash.cpp}
 * ({@code indice_registrar}, {@code indice_proxima}).</p>
 *
 * <p>A chave é "tipo sanguíneo + hemocomponente" (ex.: {@code A_POS|HEMACIAS}).
 * Cada chave leva a uma {@link FilaFEFO}: a lista, ordenada por validade, das
 * bolsas disponíveis daquele grupo. Assim, achar o grupo certo não exige
 * percorrer o estoque inteiro.</p>
 *
 * <p>O banco continua sendo a fonte da verdade; este índice é reconstruído
 * quando a aplicação sobe ({@link IndiceEstoqueCarga}) e atualizado pelo
 * {@link EstoqueService} quando uma bolsa é registrada, alocada, retirada ou
 * vence. Como é compartilhado entre requisições, os métodos são
 * {@code synchronized}.</p>
 */
@Component
public class IndiceEstoque {

    private final TabelaHash<FilaFEFO> grupos = new TabelaHash<>();
    private final Clock relogio;

    @Autowired
    public IndiceEstoque() {
        this(Clock.systemDefaultZone());
    }

    /** Permite fixar a data "de hoje" (usado nos testes). */
    IndiceEstoque(Clock relogio) {
        this.relogio = relogio;
    }

    /** Chave do grupo, ex.: {@code A_POS|HEMACIAS}. */
    public static String chave(TipoSanguineo tipo, Hemocomponente hemocomponente) {
        return tipo.name() + "|" + hemocomponente.name();
    }

    private static String chave(Bolsa bolsa) {
        return chave(bolsa.getTipoSanguineo(), bolsa.getHemocomponente());
    }

    /**
     * Coloca a bolsa no grupo da sua chave (criando o grupo se preciso).
     *
     * @return {@code true} se entrou; {@code false} se a bolsa é nula ou já está vencida
     */
    public synchronized boolean registrar(Bolsa bolsa) {
        if (bolsa == null) {
            return false;
        }
        String chave = chave(bolsa);
        FilaFEFO fila = grupos.buscar(chave);
        boolean grupoNovo = fila == null;
        if (grupoNovo) {
            fila = new FilaFEFO(relogio);
        }
        boolean entrou = fila.adicionar(bolsa);
        if (entrou && grupoNovo) {
            grupos.inserir(chave, fila);
        }
        return entrou;
    }

    /**
     * Tira a bolsa do índice (procurando pelo id, só dentro do grupo dela).
     * Se o grupo ficar vazio, ele sai da tabela.
     *
     * @return {@code true} se a bolsa estava no índice
     */
    public synchronized boolean retirar(Bolsa bolsa) {
        if (bolsa == null || bolsa.getId() == null) {
            return false;
        }
        String chave = chave(bolsa);
        FilaFEFO fila = grupos.buscar(chave);
        if (fila == null) {
            return false;
        }
        boolean removeu = fila.removerPorId(bolsa.getId());
        if (fila.estaVazia()) {
            grupos.remover(chave);
        }
        return removeu;
    }

    /**
     * Retira e devolve a bolsa válida que vence primeiro no grupo, ou
     * {@code null} se não houver nenhuma.
     */
    public synchronized Bolsa proxima(TipoSanguineo tipo, Hemocomponente hemocomponente) {
        String chave = chave(tipo, hemocomponente);
        FilaFEFO fila = grupos.buscar(chave);
        if (fila == null) {
            return null;
        }
        Bolsa bolsa = fila.proxima();
        if (fila.estaVazia()) {
            grupos.remover(chave);
        }
        return bolsa;
    }

    /** Quantas bolsas válidas há no grupo (as vencidas são descartadas antes de contar). */
    public synchronized int quantidade(TipoSanguineo tipo, Hemocomponente hemocomponente) {
        String chave = chave(tipo, hemocomponente);
        FilaFEFO fila = grupos.buscar(chave);
        if (fila == null) {
            return 0;
        }
        fila.descartarVencidas();
        if (fila.estaVazia()) {
            grupos.remover(chave);
            return 0;
        }
        return fila.tamanho();
    }

    /**
     * Descarta de todos os grupos as bolsas que já venceram e remove os grupos
     * que ficarem vazios.
     *
     * @return quantas bolsas foram descartadas
     */
    public synchronized int descartarVencidas() {
        int total = 0;
        for (String chave : grupos.chaves()) {
            FilaFEFO fila = grupos.buscar(chave);
            total += fila.descartarVencidas();
            if (fila.estaVazia()) {
                grupos.remover(chave);
            }
        }
        return total;
    }

    /** Descarta o conteúdo atual e refaz o índice com as bolsas informadas. */
    public synchronized void reconstruir(Collection<Bolsa> bolsas) {
        grupos.limpar();
        for (Bolsa bolsa : bolsas) {
            registrar(bolsa);
        }
    }

    /** Quantos grupos (chaves) existem hoje no índice. */
    public synchronized int totalDeGrupos() {
        return grupos.tamanho();
    }

    // ------------------------------------------------------------------
    // Atualização junto com o banco: só depois que a transação é confirmada
    // ------------------------------------------------------------------

    /** Registra a bolsa quando (e se) a transação atual for confirmada; sem transação, registra já. */
    public void registrarAposCommit(Bolsa bolsa) {
        aposCommit(() -> registrar(bolsa));
    }

    /** Retira a bolsa quando (e se) a transação atual for confirmada; sem transação, retira já. */
    public void retirarAposCommit(Bolsa bolsa) {
        aposCommit(() -> retirar(bolsa));
    }

    private static void aposCommit(Runnable acao) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    acao.run();
                }
            });
        } else {
            acao.run();
        }
    }
}
