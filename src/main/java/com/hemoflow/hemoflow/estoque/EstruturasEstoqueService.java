package com.hemoflow.hemoflow.estoque;

import java.util.List;

import org.springframework.stereotype.Service;

import com.hemoflow.hemoflow.dominio.Bolsa;
import com.hemoflow.hemoflow.dominio.Requisicao;
import com.hemoflow.hemoflow.estruturas.FilaSimples;
import com.hemoflow.hemoflow.estruturas.ListaEncadeada;
import com.hemoflow.hemoflow.estruturas.PilhaSimples;

/**
 * Serviço da Unidade 1 que expõe as estruturas de dados escritas à mão
 * ({@link ListaEncadeada}, {@link FilaSimples}, {@link PilhaSimples}) já
 * aplicadas às entidades do domínio, para que a camada de POO/API consiga
 * consumi-las sem conhecer os detalhes internos de cada estrutura.
 *
 * <ul>
 *   <li><b>Estoque de bolsas</b> → {@link ListaEncadeada}: inserção, remoção e
 *       consulta de bolsas.</li>
 *   <li><b>Requisições</b> → {@link FilaSimples}: atendimento por ordem de
 *       chegada (FIFO).</li>
 *   <li><b>Histórico de operações</b> → {@link PilhaSimples}: a operação mais
 *       recente fica no topo (LIFO).</li>
 * </ul>
 *
 * <p><b>Escopo:</b> este serviço funciona em memória e não depende de banco de
 * dados nem de {@code EstoqueService}; o fluxo atual de alocação (JPA) continua
 * como está. Nesta etapa não há FEFO, compatibilidade ABO/Rh, hash nem grafos
 * (Unidade 2). A ligação com o fluxo persistido pode ser feita na Unidade 2
 * chamando estes mesmos métodos a partir de {@code EstoqueService}.</p>
 *
 * <p>Os métodos são {@code synchronized} porque o serviço é um singleton do
 * Spring e as estruturas não são thread-safe.</p>
 *
 * <p>Exemplo de uso:</p>
 * <pre>{@code
 * estruturas.registrarBolsa(bolsa);
 * estruturas.enfileirarRequisicao(requisicao);
 * Requisicao atendida = estruturas.atenderProximaRequisicao();
 * String ultima = estruturas.ultimaOperacao();
 * }</pre>
 */
@Service
public class EstruturasEstoqueService {

    private final ListaEncadeada<Bolsa> estoque = new ListaEncadeada<>();
    private final FilaSimples<Requisicao> requisicoesPendentes = new FilaSimples<>();
    private final PilhaSimples<String> historico = new PilhaSimples<>();

    // ------------------------------------------------------------------
    // Estoque de bolsas (lista encadeada)
    // ------------------------------------------------------------------

    /** Insere a bolsa no fim do estoque e registra a operação no histórico. */
    public synchronized void registrarBolsa(Bolsa bolsa) {
        estoque.inserirNoFim(bolsa);
        historico.empilhar("BOLSA_REGISTRADA lote=" + bolsa.getLote());
    }

    /**
     * Remove a bolsa do estoque.
     *
     * @return {@code true} se a bolsa estava no estoque e foi removida
     */
    public synchronized boolean removerBolsa(Bolsa bolsa) {
        boolean removida = estoque.remover(bolsa);
        if (removida) {
            historico.empilhar("BOLSA_REMOVIDA lote=" + bolsa.getLote());
        }
        return removida;
    }

    /** @return {@code true} se a bolsa está no estoque */
    public synchronized boolean estoqueContem(Bolsa bolsa) {
        return estoque.contem(bolsa);
    }

    /** @return cópia das bolsas do estoque, na ordem de cadastro */
    public synchronized List<Bolsa> consultarEstoque() {
        return estoque.paraLista();
    }

    /** @return quantidade de bolsas no estoque */
    public synchronized int quantidadeEstoque() {
        return estoque.tamanho();
    }

    // ------------------------------------------------------------------
    // Requisições (fila FIFO)
    // ------------------------------------------------------------------

    /** Coloca a requisição no fim da fila e registra a operação no histórico. */
    public synchronized void enfileirarRequisicao(Requisicao requisicao) {
        requisicoesPendentes.enfileirar(requisicao);
        historico.empilhar("REQUISICAO_ENFILEIRADA id=" + requisicao.getId());
    }

    /**
     * Consulta, sem remover, a próxima requisição a ser atendida.
     *
     * @throws IllegalStateException se a fila estiver vazia
     */
    public synchronized Requisicao proximaRequisicao() {
        return requisicoesPendentes.espiar();
    }

    /**
     * Retira da fila a requisição mais antiga (primeira a chegar).
     *
     * @throws IllegalStateException se a fila estiver vazia
     */
    public synchronized Requisicao atenderProximaRequisicao() {
        Requisicao requisicao = requisicoesPendentes.desenfileirar();
        historico.empilhar("REQUISICAO_ATENDIDA id=" + requisicao.getId());
        return requisicao;
    }

    /** @return quantidade de requisições aguardando atendimento */
    public synchronized int quantidadeRequisicoesPendentes() {
        return requisicoesPendentes.tamanho();
    }

    // ------------------------------------------------------------------
    // Histórico de operações (pilha LIFO)
    // ------------------------------------------------------------------

    /**
     * Consulta, sem remover, a operação mais recente.
     *
     * @throws IllegalStateException se não houver operações registradas
     */
    public synchronized String ultimaOperacao() {
        return historico.espiar();
    }

    /**
     * Remove e retorna a operação mais recente do histórico. Apenas descarta o
     * registro; não desfaz o efeito da operação sobre o estoque ou a fila.
     *
     * @throws IllegalStateException se não houver operações registradas
     */
    public synchronized String descartarUltimaOperacao() {
        return historico.desempilhar();
    }

    /** @return quantidade de operações registradas no histórico */
    public synchronized int tamanhoHistorico() {
        return historico.tamanho();
    }
}
