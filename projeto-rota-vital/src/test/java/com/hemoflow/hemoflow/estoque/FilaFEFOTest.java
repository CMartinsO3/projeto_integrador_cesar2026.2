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

import com.hemoflow.hemoflow.dominio.Bolsa;
import com.hemoflow.hemoflow.dominio.Hemocomponente;
import com.hemoflow.hemoflow.dominio.NoRede;
import com.hemoflow.hemoflow.dominio.TipoNo;
import com.hemoflow.hemoflow.dominio.TipoSanguineo;

class FilaFEFOTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 10, 10);
    private static final NoRede LOCAL = new NoRede("HC-TESTE", "Hemocentro Teste", TipoNo.HEMOCENTRO);

    /** Relógio que podemos avançar, para simular bolsas que vencem enquanto esperam. */
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

    private static FilaFEFO filaEm(LocalDate hoje) {
        return new FilaFEFO(new RelogioMutavel(hoje));
    }

    private static Bolsa bolsa(Long id, LocalDate validade) {
        Bolsa b = new Bolsa(TipoSanguineo.O_NEG, Hemocomponente.HEMACIAS,
                validade.minusDays(30), validade, "L-" + id, LOCAL);
        if (id != null) {
            definirId(b, id);   // o id real é gerado pelo banco; aqui simulamos
        }
        return b;
    }

    private static void definirId(Bolsa b, Long id) {
        try {
            Field campo = Bolsa.class.getDeclaredField("id");
            campo.setAccessible(true);
            campo.set(b, id);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    @DisplayName("Bolsa que vence antes sai antes, independente da ordem de chegada")
    void deveSairPorOrdemDeValidade() {
        FilaFEFO fila = filaEm(HOJE);
        Bolsa b2 = bolsa(2L, LocalDate.of(2026, 10, 25));
        Bolsa b3 = bolsa(3L, LocalDate.of(2026, 11, 3));
        Bolsa b1 = bolsa(1L, LocalDate.of(2026, 10, 15));
        fila.adicionar(b2);
        fila.adicionar(b3);
        fila.adicionar(b1);

        assertSame(b1, fila.proxima());
        assertSame(b2, fila.proxima());
        assertSame(b3, fila.proxima());
    }

    @Test
    @DisplayName("Exemplo do guia: nova bolsa que vence em 20/10 entra entre 15/10 e 25/10")
    void novaBolsaDeveEntrarNoLugarCerto() {
        FilaFEFO fila = filaEm(HOJE);
        Bolsa b1 = bolsa(1L, LocalDate.of(2026, 10, 15));
        Bolsa b2 = bolsa(2L, LocalDate.of(2026, 10, 25));
        Bolsa b3 = bolsa(3L, LocalDate.of(2026, 11, 3));
        Bolsa nova = bolsa(4L, LocalDate.of(2026, 10, 20));
        fila.adicionarTodas(List.of(b1, b2, b3));
        fila.adicionar(nova);

        assertEquals(4, fila.tamanho());
        assertSame(b1, fila.proxima());
        assertSame(nova, fila.proxima());
        assertSame(b2, fila.proxima());
        assertSame(b3, fila.proxima());
    }

    @Test
    @DisplayName("Empate de validade: menor id sai primeiro")
    void empateDeValidadeDeveDesempatarPeloId() {
        FilaFEFO fila = filaEm(HOJE);
        LocalDate mesma = LocalDate.of(2026, 11, 1);
        Bolsa b30 = bolsa(30L, mesma);
        Bolsa b10 = bolsa(10L, mesma);
        Bolsa b20 = bolsa(20L, mesma);
        fila.adicionar(b30);
        fila.adicionar(b10);
        fila.adicionar(b20);

        assertSame(b10, fila.proxima());
        assertSame(b20, fila.proxima());
        assertSame(b30, fila.proxima());
    }

    @Test
    @DisplayName("Empate total (mesma validade, sem id): vale a ordem de chegada")
    void empateTotalDeveManterOrdemDeChegada() {
        FilaFEFO fila = filaEm(HOJE);
        LocalDate mesma = LocalDate.of(2026, 11, 1);
        Bolsa primeira = bolsa(null, mesma);
        Bolsa segunda = bolsa(null, mesma);
        Bolsa terceira = bolsa(null, mesma);
        fila.adicionar(primeira);
        fila.adicionar(segunda);
        fila.adicionar(terceira);

        assertSame(primeira, fila.proxima());
        assertSame(segunda, fila.proxima());
        assertSame(terceira, fila.proxima());
    }

    @Test
    @DisplayName("Fila vazia: proxima() devolve null e estaVazia() é true")
    void filaVazia() {
        FilaFEFO fila = filaEm(HOJE);
        assertTrue(fila.estaVazia());
        assertEquals(0, fila.tamanho());
        assertNull(fila.proxima());
    }

    @Test
    @DisplayName("Bolsa já vencida é descartada na entrada; a que vence hoje ainda vale")
    void bolsaVencidaNaoDeveEntrar() {
        FilaFEFO fila = filaEm(HOJE);
        Bolsa vencida = bolsa(1L, HOJE.minusDays(1));
        Bolsa venceHoje = bolsa(2L, HOJE);

        assertFalse(fila.adicionar(vencida));
        assertTrue(fila.adicionar(venceHoje));
        assertFalse(fila.adicionar(null));
        assertEquals(1, fila.tamanho());
        assertSame(venceHoje, fila.proxima());
    }

    @Test
    @DisplayName("Bolsa que vence enquanto espera na fila é pulada ao retirar")
    void bolsaQueVenceNaFilaDeveSerPulada() {
        RelogioMutavel relogio = new RelogioMutavel(HOJE);
        FilaFEFO fila = new FilaFEFO(relogio);
        Bolsa venceLogo = bolsa(1L, LocalDate.of(2026, 10, 12));
        Bolsa venceDepois = bolsa(2L, LocalDate.of(2026, 10, 30));
        fila.adicionar(venceLogo);
        fila.adicionar(venceDepois);

        relogio.avancarPara(LocalDate.of(2026, 10, 20));

        assertSame(venceDepois, fila.proxima());
        assertNull(fila.proxima());
    }

    @Test
    @DisplayName("Mesma ordem de saída que ordenar a lista inteira (200 bolsas aleatórias)")
    void ordemDeSaidaDeveCoincidirComOrdenacaoCompleta() {
        Random sorteio = new Random(42);
        FilaFEFO fila = filaEm(HOJE);
        List<Bolsa> entrada = new ArrayList<>();
        for (long i = 1; i <= 200; i++) {
            Bolsa b = bolsa(i, HOJE.plusDays(sorteio.nextInt(40)));
            entrada.add(b);
            fila.adicionar(b);
        }

        List<Bolsa> esperado = new ArrayList<>(entrada);
        esperado.sort(Comparator.comparing(Bolsa::getDataValidade).thenComparing(Bolsa::getId));

        for (Bolsa b : esperado) {
            assertSame(b, fila.proxima());
        }
        assertTrue(fila.estaVazia());
    }
}
