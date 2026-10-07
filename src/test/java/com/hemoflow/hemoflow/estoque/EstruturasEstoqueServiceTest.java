package com.hemoflow.hemoflow.estoque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.hemoflow.hemoflow.dominio.Bolsa;
import com.hemoflow.hemoflow.dominio.Hemocomponente;
import com.hemoflow.hemoflow.dominio.Hospital;
import com.hemoflow.hemoflow.dominio.NoRede;
import com.hemoflow.hemoflow.dominio.Requisicao;
import com.hemoflow.hemoflow.dominio.TipoNo;
import com.hemoflow.hemoflow.dominio.TipoSanguineo;

/**
 * Testes unitários (sem Spring e sem banco) do {@link EstruturasEstoqueService}:
 * casos simples de estoque (lista), requisições (fila) e histórico (pilha).
 */
class EstruturasEstoqueServiceTest {

    private EstruturasEstoqueService servico;
    private NoRede local;
    private Hospital hospital;

    @BeforeEach
    void preparar() {
        servico = new EstruturasEstoqueService();
        local = new NoRede("U1-LOCAL", "Hemocentro U1", TipoNo.HEMOCENTRO);
        hospital = new Hospital("Hospital U1", new NoRede("U1-HOSP", "Hospital U1", TipoNo.HOSPITAL));
    }

    private Bolsa novaBolsa(String lote) {
        LocalDate hoje = LocalDate.now();
        return new Bolsa(TipoSanguineo.O_NEG, Hemocomponente.HEMACIAS,
                hoje.minusDays(1), hoje.plusDays(30), lote, local);
    }

    private Requisicao novaRequisicao(int quantidade) {
        return new Requisicao(hospital, TipoSanguineo.O_NEG, Hemocomponente.HEMACIAS,
                quantidade, LocalDateTime.now().plusHours(6));
    }

    @Test
    @DisplayName("Estoque (lista): registra bolsas na ordem de cadastro, consulta e remove")
    void estoqueComListaEncadeada() {
        Bolsa a = novaBolsa("L-A");
        Bolsa b = novaBolsa("L-B");
        Bolsa c = novaBolsa("L-C");

        servico.registrarBolsa(a);
        servico.registrarBolsa(b);
        servico.registrarBolsa(c);

        assertEquals(3, servico.quantidadeEstoque());
        assertEquals(List.of(a, b, c), servico.consultarEstoque());
        assertTrue(servico.estoqueContem(b));

        assertTrue(servico.removerBolsa(b));
        assertFalse(servico.estoqueContem(b));
        assertEquals(List.of(a, c), servico.consultarEstoque());
        assertFalse(servico.removerBolsa(b), "remover de novo uma bolsa já removida retorna false");
    }

    @Test
    @DisplayName("Requisições (fila): atende na ordem de chegada (FIFO)")
    void requisicoesComFila() {
        Requisicao primeira = novaRequisicao(1);
        Requisicao segunda = novaRequisicao(2);

        servico.enfileirarRequisicao(primeira);
        servico.enfileirarRequisicao(segunda);

        assertEquals(2, servico.quantidadeRequisicoesPendentes());
        assertSame(primeira, servico.proximaRequisicao());
        assertSame(primeira, servico.atenderProximaRequisicao());
        assertSame(segunda, servico.atenderProximaRequisicao());
        assertEquals(0, servico.quantidadeRequisicoesPendentes());
        assertThrows(IllegalStateException.class, servico::atenderProximaRequisicao);
    }

    @Test
    @DisplayName("Histórico (pilha): a operação mais recente fica no topo (LIFO)")
    void historicoComPilha() {
        Bolsa a = novaBolsa("L-H1");
        servico.registrarBolsa(a);
        servico.enfileirarRequisicao(novaRequisicao(1));
        servico.removerBolsa(a);

        assertEquals(3, servico.tamanhoHistorico());
        assertEquals("BOLSA_REMOVIDA lote=L-H1", servico.ultimaOperacao());
        assertEquals("BOLSA_REMOVIDA lote=L-H1", servico.descartarUltimaOperacao());
        assertTrue(servico.ultimaOperacao().startsWith("REQUISICAO_ENFILEIRADA"));
        assertEquals(2, servico.tamanhoHistorico());
    }

    @Test
    @DisplayName("Estruturas vazias lançam exceção clara em vez de retornar null")
    void estruturasVazias() {
        assertThrows(IllegalStateException.class, servico::proximaRequisicao);
        assertThrows(IllegalStateException.class, servico::ultimaOperacao);
        assertEquals(0, servico.quantidadeEstoque());
    }
}
