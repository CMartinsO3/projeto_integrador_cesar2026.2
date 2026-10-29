package com.hemoflow.hemoflow.estoque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.hemoflow.hemoflow.dominio.Bolsa;
import com.hemoflow.hemoflow.dominio.Hemocomponente;
import com.hemoflow.hemoflow.dominio.NoRede;
import com.hemoflow.hemoflow.dominio.Requisicao;
import com.hemoflow.hemoflow.dominio.StatusBolsa;
import com.hemoflow.hemoflow.dominio.StatusRequisicao;
import com.hemoflow.hemoflow.dominio.TipoNo;
import com.hemoflow.hemoflow.dominio.TipoSanguineo;
import com.hemoflow.hemoflow.persistencia.BolsaRepository;
import com.hemoflow.hemoflow.persistencia.HospitalRepository;
import com.hemoflow.hemoflow.persistencia.NoRedeRepository;
import com.hemoflow.hemoflow.persistencia.RequisicaoRepository;

/**
 * Confere que registrar, retirar, alocar e vencer uma bolsa no
 * {@link EstoqueService} mantêm o {@link IndiceEstoque} atualizado.
 * Os repositórios são simulados (sem banco); aqui não há transação, então o
 * índice é atualizado na hora (o comportamento "só após o commit" é testado
 * em {@link IndiceEstoqueTest}).
 */
class EstoqueServiceIndiceTest {

    private final BolsaRepository bolsaRepository = mock(BolsaRepository.class);
    private final NoRedeRepository noRedeRepository = mock(NoRedeRepository.class);
    private final HospitalRepository hospitalRepository = mock(HospitalRepository.class);
    private final RequisicaoRepository requisicaoRepository = mock(RequisicaoRepository.class);
    private final IndiceEstoque indice = new IndiceEstoque();
    private final EstoqueService servico =
            new EstoqueService(bolsaRepository, noRedeRepository, hospitalRepository, requisicaoRepository, indice);

    /** "Banco" falso: guarda as bolsas salvas, na ordem de cadastro. */
    private final Map<Long, Bolsa> banco = new LinkedHashMap<>();
    private final AtomicLong proximoId = new AtomicLong(1);
    private final LocalDate hoje = LocalDate.now();

    EstoqueServiceIndiceTest() {
        NoRede no = new NoRede("HC-TESTE", "Hemocentro Teste", TipoNo.HEMOCENTRO);
        when(noRedeRepository.findById(1L)).thenReturn(Optional.of(no));
        when(bolsaRepository.save(any(Bolsa.class))).thenAnswer(inv -> {
            Bolsa b = inv.getArgument(0);
            if (b.getId() == null) {
                definirId(b, proximoId.getAndIncrement());   // o banco real gera o id
            }
            banco.put(b.getId(), b);
            return b;
        });
        when(bolsaRepository.findById(anyLong())).thenAnswer(inv -> Optional.ofNullable(banco.get(inv.getArgument(0))));
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

    private Long entrada(TipoSanguineo tipo, Hemocomponente hemo, int diasParaVencer) {
        Map<String, Object> resposta = servico.registrarEntrada(
                tipo, hemo, hoje.minusDays(1), hoje.plusDays(diasParaVencer), "L-" + proximoId.get(), 1L);
        return (Long) resposta.get("id");
    }

    @Test
    @DisplayName("Registrar entrada coloca a bolsa no grupo certo do índice")
    void registrarEntradaAtualizaOIndice() {
        entrada(TipoSanguineo.A_POS, Hemocomponente.HEMACIAS, 5);
        entrada(TipoSanguineo.A_POS, Hemocomponente.HEMACIAS, 20);
        entrada(TipoSanguineo.O_NEG, Hemocomponente.PLASMA, 7);

        assertEquals(2, indice.quantidade(TipoSanguineo.A_POS, Hemocomponente.HEMACIAS));
        assertEquals(1, indice.quantidade(TipoSanguineo.O_NEG, Hemocomponente.PLASMA));
        assertEquals(2, indice.totalDeGrupos());
    }

    @Test
    @DisplayName("Registrar saída (entrega ou descarte) tira a bolsa do índice")
    void registrarSaidaAtualizaOIndice() {
        Long id = entrada(TipoSanguineo.B_POS, Hemocomponente.PLAQUETAS, 5);
        assertEquals(1, indice.quantidade(TipoSanguineo.B_POS, Hemocomponente.PLAQUETAS));

        servico.registrarSaida(id, "DESCARTE");

        assertEquals(0, indice.quantidade(TipoSanguineo.B_POS, Hemocomponente.PLAQUETAS));
        assertEquals(0, indice.totalDeGrupos());
    }

    @Test
    @DisplayName("Mudar o status da bolsa para fora de DISPONIVEL tira a bolsa do índice")
    void transicaoDeStatusAtualizaOIndice() {
        Long id = entrada(TipoSanguineo.AB_NEG, Hemocomponente.PLASMA, 5);

        servico.transicionarStatusBolsa(id, StatusBolsa.DESCARTADA);

        assertEquals(0, indice.quantidade(TipoSanguineo.AB_NEG, Hemocomponente.PLASMA));
    }

    @Test
    @DisplayName("Alocar uma bolsa para uma requisição tira a bolsa alocada do índice")
    void alocarAtualizaOIndice() {
        Long primeira = entrada(TipoSanguineo.A_POS, Hemocomponente.HEMACIAS, 5);
        Long segunda = entrada(TipoSanguineo.A_POS, Hemocomponente.HEMACIAS, 20);

        Requisicao requisicao = mock(Requisicao.class);
        when(requisicao.getStatus()).thenReturn(StatusRequisicao.PENDENTE);
        when(requisicao.getHemocomponente()).thenReturn(Hemocomponente.HEMACIAS);
        when(requisicao.getQuantidade()).thenReturn(1);
        when(requisicaoRepository.findById(10L)).thenReturn(Optional.of(requisicao));
        when(bolsaRepository.findByStatusAndHemocomponente(StatusBolsa.DISPONIVEL, Hemocomponente.HEMACIAS))
                .thenReturn(new ArrayList<>(banco.values()));

        List<Bolsa> alocadas = servico.alocar(10L);

        assertEquals(1, alocadas.size());
        assertEquals(primeira, alocadas.get(0).getId());               // U1: a primeira da lista
        assertEquals(StatusBolsa.ALOCADA, alocadas.get(0).getStatus());
        assertEquals(1, indice.quantidade(TipoSanguineo.A_POS, Hemocomponente.HEMACIAS));
        assertEquals(segunda, indice.proxima(TipoSanguineo.A_POS, Hemocomponente.HEMACIAS).getId());
    }

    @Test
    @DisplayName("Ao alocar, bolsas que venceram dentro do índice são descartadas (e o grupo vazio some)")
    void alocarDescartaVencidasDoIndice() {
        RelogioMutavel relogio = new RelogioMutavel(hoje);
        IndiceEstoque indiceComRelogio = new IndiceEstoque(relogio);
        EstoqueService servicoComRelogio = new EstoqueService(
                bolsaRepository, noRedeRepository, hospitalRepository, requisicaoRepository, indiceComRelogio);

        servicoComRelogio.registrarEntrada(TipoSanguineo.O_NEG, Hemocomponente.PLASMA,
                hoje.minusDays(1), hoje.plusDays(2), "L-PLASMA", 1L);
        Map<String, Object> hemacias = servicoComRelogio.registrarEntrada(TipoSanguineo.A_POS, Hemocomponente.HEMACIAS,
                hoje.minusDays(1), hoje.plusDays(30), "L-HEMACIAS", 1L);
        assertEquals(2, indiceComRelogio.totalDeGrupos());

        relogio.avancarPara(hoje.plusDays(10));          // a bolsa de plasma vence dentro do índice
        assertEquals(2, indiceComRelogio.totalDeGrupos()); // ninguém limpou ainda

        Requisicao requisicao = mock(Requisicao.class);
        when(requisicao.getStatus()).thenReturn(StatusRequisicao.PENDENTE);
        when(requisicao.getHemocomponente()).thenReturn(Hemocomponente.HEMACIAS);
        when(requisicao.getQuantidade()).thenReturn(1);
        when(requisicaoRepository.findById(11L)).thenReturn(Optional.of(requisicao));
        when(bolsaRepository.findByStatusAndHemocomponente(StatusBolsa.DISPONIVEL, Hemocomponente.HEMACIAS))
                .thenReturn(List.of(banco.get((Long) hemacias.get("id"))));

        servicoComRelogio.alocar(11L);

        // plasma vencida descartada pela limpeza + hemácias alocada retirada: índice vazio
        assertEquals(0, indiceComRelogio.totalDeGrupos());
    }

    private static class RelogioMutavel extends Clock {
        private LocalDate dia;

        RelogioMutavel(LocalDate dia) {
            this.dia = dia;
        }

        void avancarPara(LocalDate novo) {
            this.dia = novo;
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
            return dia.atStartOfDay(ZoneOffset.UTC).toInstant();
        }
    }
}
