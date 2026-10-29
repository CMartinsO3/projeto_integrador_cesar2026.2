package com.hemoflow.hemoflow.estoque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.hemoflow.hemoflow.dominio.Bolsa;
import com.hemoflow.hemoflow.dominio.Hemocomponente;
import com.hemoflow.hemoflow.dominio.NoRede;
import com.hemoflow.hemoflow.dominio.TipoNo;
import com.hemoflow.hemoflow.dominio.TipoSanguineo;

class IndiceEstoqueTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 10, 10);
    private static final NoRede LOCAL = new NoRede("HC-TESTE", "Hemocentro Teste", TipoNo.HEMOCENTRO);

    private static class RelogioMutavel extends Clock {
        private LocalDate hoje;

        RelogioMutavel(LocalDate hoje) {
            this.hoje = hoje;
        }

        void avancarPara(LocalDate novo) {
            this.hoje = novo;
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return hoje.atStartOfDay(ZoneOffset.UTC).toInstant();
        }
    }

    private static Bolsa bolsa(long id, TipoSanguineo tipo, Hemocomponente hemo, LocalDate validade) {
        Bolsa b = new Bolsa(tipo, hemo, validade.minusDays(30), validade, "L-" + id, LOCAL);
        try {
            Field campo = Bolsa.class.getDeclaredField("id");
            campo.setAccessible(true);
            campo.set(b, id);          // o id real é gerado pelo banco; aqui simulamos
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
        return b;
    }

    private static IndiceEstoque indiceEm(LocalDate hoje) {
        return new IndiceEstoque(new RelogioMutavel(hoje));
    }

    @Test
    @DisplayName("A chave do grupo é tipo sanguíneo + hemocomponente")
    void formatoDaChave() {
        assertEquals("A_POS|HEMACIAS", IndiceEstoque.chave(TipoSanguineo.A_POS, Hemocomponente.HEMACIAS));
        assertEquals("AB_NEG|CRIOPRECIPITADO", IndiceEstoque.chave(TipoSanguineo.AB_NEG, Hemocomponente.CRIOPRECIPITADO));
    }

    @Test
    @DisplayName("Cada chave tem o seu grupo: tipo ou hemocomponente diferente = grupo diferente")
    void gruposIndependentes() {
        IndiceEstoque indice = indiceEm(HOJE);
        indice.registrar(bolsa(1, TipoSanguineo.A_POS, Hemocomponente.HEMACIAS, HOJE.plusDays(5)));
        indice.registrar(bolsa(2, TipoSanguineo.A_POS, Hemocomponente.HEMACIAS, HOJE.plusDays(15)));
        indice.registrar(bolsa(3, TipoSanguineo.A_POS, Hemocomponente.PLASMA, HOJE.plusDays(5)));
        indice.registrar(bolsa(4, TipoSanguineo.O_NEG, Hemocomponente.HEMACIAS, HOJE.plusDays(5)));

        assertEquals(3, indice.totalDeGrupos());
        assertEquals(2, indice.quantidade(TipoSanguineo.A_POS, Hemocomponente.HEMACIAS));
        assertEquals(1, indice.quantidade(TipoSanguineo.A_POS, Hemocomponente.PLASMA));
        assertEquals(1, indice.quantidade(TipoSanguineo.O_NEG, Hemocomponente.HEMACIAS));
        assertEquals(0, indice.quantidade(TipoSanguineo.B_POS, Hemocomponente.HEMACIAS));
    }

    @Test
    @DisplayName("Dentro do grupo, a bolsa que vence primeiro sai primeiro (FEFO)")
    void fefoDentroDoGrupo() {
        IndiceEstoque indice = indiceEm(HOJE);
        Bolsa b25 = bolsa(2, TipoSanguineo.A_POS, Hemocomponente.HEMACIAS, HOJE.plusDays(15));
        Bolsa b15 = bolsa(1, TipoSanguineo.A_POS, Hemocomponente.HEMACIAS, HOJE.plusDays(5));
        indice.registrar(b25);
        indice.registrar(b15);

        assertSame(b15, indice.proxima(TipoSanguineo.A_POS, Hemocomponente.HEMACIAS));
        assertSame(b25, indice.proxima(TipoSanguineo.A_POS, Hemocomponente.HEMACIAS));
        assertNull(indice.proxima(TipoSanguineo.A_POS, Hemocomponente.HEMACIAS));
    }

    @Test
    @DisplayName("Grupo inexistente: proxima() devolve null")
    void grupoInexistente() {
        IndiceEstoque indice = indiceEm(HOJE);
        assertNull(indice.proxima(TipoSanguineo.B_NEG, Hemocomponente.PLASMA));
    }

    @Test
    @DisplayName("retirar() acha a bolsa pelo id (mesmo sendo outra instância) e o grupo vazio sai da tabela")
    void retirarPorId() {
        IndiceEstoque indice = indiceEm(HOJE);
        indice.registrar(bolsa(1, TipoSanguineo.A_POS, Hemocomponente.HEMACIAS, HOJE.plusDays(5)));
        indice.registrar(bolsa(2, TipoSanguineo.A_POS, Hemocomponente.HEMACIAS, HOJE.plusDays(9)));
        indice.registrar(bolsa(3, TipoSanguineo.A_POS, Hemocomponente.HEMACIAS, HOJE.plusDays(15)));

        Bolsa outraInstanciaDoId2 = bolsa(2, TipoSanguineo.A_POS, Hemocomponente.HEMACIAS, HOJE.plusDays(9));
        assertTrue(indice.retirar(outraInstanciaDoId2));
        assertEquals(2, indice.quantidade(TipoSanguineo.A_POS, Hemocomponente.HEMACIAS));
        assertFalse(indice.retirar(outraInstanciaDoId2));

        assertTrue(indice.retirar(bolsa(1, TipoSanguineo.A_POS, Hemocomponente.HEMACIAS, HOJE.plusDays(5))));
        assertTrue(indice.retirar(bolsa(3, TipoSanguineo.A_POS, Hemocomponente.HEMACIAS, HOJE.plusDays(15))));
        assertEquals(0, indice.totalDeGrupos());
    }

    @Test
    @DisplayName("retirar() de bolsa que não está no índice, sem id ou nula devolve false")
    void retirarInexistente() {
        IndiceEstoque indice = indiceEm(HOJE);
        indice.registrar(bolsa(1, TipoSanguineo.A_POS, Hemocomponente.HEMACIAS, HOJE.plusDays(5)));

        assertFalse(indice.retirar(bolsa(99, TipoSanguineo.A_POS, Hemocomponente.HEMACIAS, HOJE.plusDays(5))));
        assertFalse(indice.retirar(bolsa(1, TipoSanguineo.O_NEG, Hemocomponente.PLASMA, HOJE.plusDays(5))));
        assertFalse(indice.retirar(new Bolsa(TipoSanguineo.A_POS, Hemocomponente.HEMACIAS,
                HOJE.minusDays(1), HOJE.plusDays(5), "SEM-ID", LOCAL)));
        assertFalse(indice.retirar(null));
        assertEquals(1, indice.quantidade(TipoSanguineo.A_POS, Hemocomponente.HEMACIAS));
    }

    @Test
    @DisplayName("Bolsa já vencida não entra e não cria grupo")
    void bolsaVencidaNaoEntra() {
        IndiceEstoque indice = indiceEm(HOJE);
        assertFalse(indice.registrar(bolsa(1, TipoSanguineo.A_POS, Hemocomponente.HEMACIAS, HOJE.minusDays(1))));
        assertFalse(indice.registrar(null));
        assertEquals(0, indice.totalDeGrupos());
    }

    @Test
    @DisplayName("Bolsas que vencem enquanto esperam saem do índice (e os grupos vazios também)")
    void descartarVencidas() {
        RelogioMutavel relogio = new RelogioMutavel(HOJE);
        IndiceEstoque indice = new IndiceEstoque(relogio);
        indice.registrar(bolsa(1, TipoSanguineo.A_POS, Hemocomponente.HEMACIAS, HOJE.plusDays(2)));
        indice.registrar(bolsa(2, TipoSanguineo.A_POS, Hemocomponente.HEMACIAS, HOJE.plusDays(20)));
        indice.registrar(bolsa(3, TipoSanguineo.O_NEG, Hemocomponente.PLASMA, HOJE.plusDays(3)));
        assertEquals(2, indice.totalDeGrupos());

        relogio.avancarPara(HOJE.plusDays(10));

        assertEquals(2, indice.descartarVencidas());
        assertEquals(1, indice.totalDeGrupos());
        assertEquals(1, indice.quantidade(TipoSanguineo.A_POS, Hemocomponente.HEMACIAS));
        assertEquals(0, indice.quantidade(TipoSanguineo.O_NEG, Hemocomponente.PLASMA));
        assertEquals(0, indice.descartarVencidas());
    }

    @Test
    @DisplayName("quantidade() não conta bolsas vencidas")
    void quantidadeIgnoraVencidas() {
        RelogioMutavel relogio = new RelogioMutavel(HOJE);
        IndiceEstoque indice = new IndiceEstoque(relogio);
        indice.registrar(bolsa(1, TipoSanguineo.B_POS, Hemocomponente.PLAQUETAS, HOJE.plusDays(1)));
        relogio.avancarPara(HOJE.plusDays(5));
        assertEquals(0, indice.quantidade(TipoSanguineo.B_POS, Hemocomponente.PLAQUETAS));
        assertEquals(0, indice.totalDeGrupos());
    }

    @Test
    @DisplayName("reconstruir() troca todo o conteúdo pelo das bolsas informadas")
    void reconstruir() {
        IndiceEstoque indice = indiceEm(HOJE);
        indice.registrar(bolsa(1, TipoSanguineo.A_POS, Hemocomponente.HEMACIAS, HOJE.plusDays(5)));

        indice.reconstruir(List.of(
                bolsa(10, TipoSanguineo.O_NEG, Hemocomponente.PLASMA, HOJE.plusDays(5)),
                bolsa(11, TipoSanguineo.O_NEG, Hemocomponente.PLASMA, HOJE.plusDays(8))));

        assertEquals(1, indice.totalDeGrupos());
        assertEquals(0, indice.quantidade(TipoSanguineo.A_POS, Hemocomponente.HEMACIAS));
        assertEquals(2, indice.quantidade(TipoSanguineo.O_NEG, Hemocomponente.PLASMA));
    }

    // ---- sincronização com a transação do banco ----

    @Test
    @DisplayName("Sem transação, registrarAposCommit() e retirarAposCommit() agem na hora")
    void semTransacaoAplicaNaHora() {
        IndiceEstoque indice = indiceEm(HOJE);
        Bolsa b = bolsa(1, TipoSanguineo.A_POS, Hemocomponente.HEMACIAS, HOJE.plusDays(5));

        indice.registrarAposCommit(b);
        assertEquals(1, indice.quantidade(TipoSanguineo.A_POS, Hemocomponente.HEMACIAS));
        indice.retirarAposCommit(b);
        assertEquals(0, indice.quantidade(TipoSanguineo.A_POS, Hemocomponente.HEMACIAS));
    }

    @Test
    @DisplayName("Dentro de uma transação, o índice só muda depois do commit")
    void commitAtualizaOIndice() {
        IndiceEstoque indice = indiceEm(HOJE);
        Bolsa b = bolsa(1, TipoSanguineo.A_POS, Hemocomponente.HEMACIAS, HOJE.plusDays(5));

        TransactionSynchronizationManager.initSynchronization();
        try {
            indice.registrarAposCommit(b);
            assertEquals(0, indice.totalDeGrupos());                 // ainda não confirmou

            for (TransactionSynchronization sync : TransactionSynchronizationManager.getSynchronizations()) {
                sync.afterCommit();                                   // simula o commit
            }
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
        assertEquals(1, indice.quantidade(TipoSanguineo.A_POS, Hemocomponente.HEMACIAS));
    }

    @Test
    @DisplayName("Se a transação sofre rollback, o índice não muda")
    void rollbackNaoAtualizaOIndice() {
        IndiceEstoque indice = indiceEm(HOJE);
        Bolsa b = bolsa(1, TipoSanguineo.A_POS, Hemocomponente.HEMACIAS, HOJE.plusDays(5));
        indice.registrar(b);

        TransactionSynchronizationManager.initSynchronization();
        try {
            indice.retirarAposCommit(b);
            // rollback: afterCommit nunca é chamado
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
        assertEquals(1, indice.quantidade(TipoSanguineo.A_POS, Hemocomponente.HEMACIAS));
    }

    // ---- verificação em volume ----

    @Test
    @DisplayName("400 bolsas espalhadas pelas 32 chaves: cada grupo entrega na ordem FEFO e nada se perde")
    void volumeContraOrdenacaoCompleta() {
        Random sorteio = new Random(99);
        IndiceEstoque indice = indiceEm(HOJE);
        List<Bolsa> todas = new ArrayList<>();
        for (long id = 1; id <= 400; id++) {
            TipoSanguineo tipo = TipoSanguineo.values()[sorteio.nextInt(TipoSanguineo.values().length)];
            Hemocomponente hemo = Hemocomponente.values()[sorteio.nextInt(Hemocomponente.values().length)];
            Bolsa b = bolsa(id, tipo, hemo, HOJE.plusDays(sorteio.nextInt(40)));
            todas.add(b);
            assertTrue(indice.registrar(b));
        }

        int retiradas = 0;
        for (TipoSanguineo tipo : TipoSanguineo.values()) {
            for (Hemocomponente hemo : Hemocomponente.values()) {
                List<Bolsa> esperado = new ArrayList<>();
                for (Bolsa b : todas) {
                    if (b.getTipoSanguineo() == tipo && b.getHemocomponente() == hemo) {
                        esperado.add(b);
                    }
                }
                esperado.sort(Comparator.comparing(Bolsa::getDataValidade).thenComparing(Bolsa::getId));
                assertEquals(esperado.size(), indice.quantidade(tipo, hemo));
                for (Bolsa b : esperado) {
                    assertSame(b, indice.proxima(tipo, hemo));
                    retiradas++;
                }
                assertNull(indice.proxima(tipo, hemo));
            }
        }
        assertEquals(400, retiradas);
        assertEquals(0, indice.totalDeGrupos());
    }
}
