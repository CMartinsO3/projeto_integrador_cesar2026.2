package com.hemoflow.hemoflow.estatistica;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.hemoflow.hemoflow.dominio.Hemocomponente;
import com.hemoflow.hemoflow.dominio.Requisicao;
import com.hemoflow.hemoflow.dominio.StatusRequisicao;
import com.hemoflow.hemoflow.dominio.TipoSanguineo;
import com.hemoflow.hemoflow.persistencia.BolsaRepository;
import com.hemoflow.hemoflow.persistencia.RequisicaoRepository;

/** Garante um único critério de "atendida" em todo o módulo de estatística. */
class TaxaAtendimentoTest {

    private static Requisicao req(StatusRequisicao s) {
        Requisicao r = new Requisicao(null, TipoSanguineo.O_POS, Hemocomponente.HEMACIAS, 1, LocalDateTime.now().plusHours(4));
        r.setStatus(s);
        return r;
    }

    @Test
    void criterioPorStatus() {
        assertTrue(EstatisticaService.atendida(req(StatusRequisicao.ALOCADA)));
        assertTrue(EstatisticaService.atendida(req(StatusRequisicao.EM_TRANSITO)));
        assertTrue(EstatisticaService.atendida(req(StatusRequisicao.ENTREGUE)));
        assertFalse(EstatisticaService.atendida(req(StatusRequisicao.PENDENTE)));
        assertFalse(EstatisticaService.atendida(req(StatusRequisicao.AGUARDANDO_ESTOQUE)));
        assertFalse(EstatisticaService.atendida(req(StatusRequisicao.CANCELADA)));
    }

    @Test
    void resumoEProbabilidadeUsamOMesmoValor() {
        List<Requisicao> rs = new ArrayList<>();
        // 6 requisições: 1 de cada status → 3 atendidas de 6 = 50,0%
        for (StatusRequisicao s : StatusRequisicao.values()) {
            rs.add(req(s));
        }
        assertEquals(6, rs.size());

        RequisicaoRepository reqRepo = mock(RequisicaoRepository.class);
        when(reqRepo.findAll()).thenReturn(rs);
        EstatisticaService svc = new EstatisticaService(mock(BolsaRepository.class), reqRepo);
        EstatisticaDTOs.ResumoRequisicoes resumo = svc.resumoRequisicoes();
        assertEquals(50.0, resumo.taxaAtendimentoPercent(), 1e-9);

        EstatisticaDTOs.AnaliseProbabilidade prob = AnaliseAvancadaService.calcularProbabilidade(rs);
        assertEquals(resumo.taxaAtendimentoPercent(), prob.pAtendimentoGeral() * 100.0, 1e-9);
    }

    @Test
    void entreguesContamComoAtendidas() {
        List<Requisicao> rs = List.of(req(StatusRequisicao.ENTREGUE), req(StatusRequisicao.ENTREGUE),
                req(StatusRequisicao.ENTREGUE), req(StatusRequisicao.CANCELADA));
        RequisicaoRepository reqRepo = mock(RequisicaoRepository.class);
        when(reqRepo.findAll()).thenReturn(rs);
        EstatisticaService svc = new EstatisticaService(mock(BolsaRepository.class), reqRepo);
        assertEquals(75.0, svc.resumoRequisicoes().taxaAtendimentoPercent(), 1e-9);  // antes dava 0,0%
        assertEquals(0.0, new EstatisticaService(mock(BolsaRepository.class), emptyRepo()).resumoRequisicoes().taxaAtendimentoPercent(), 0);
    }

    private static RequisicaoRepository emptyRepo() {
        RequisicaoRepository r = mock(RequisicaoRepository.class);
        when(r.findAll()).thenReturn(List.of());
        return r;
    }
}
