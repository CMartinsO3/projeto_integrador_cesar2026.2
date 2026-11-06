package com.hemoflow.hemoflow.estatistica;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.hemoflow.hemoflow.dominio.Bolsa;
import com.hemoflow.hemoflow.dominio.Hemocomponente;
import com.hemoflow.hemoflow.dominio.LeituraTemperatura;
import com.hemoflow.hemoflow.dominio.Requisicao;
import com.hemoflow.hemoflow.dominio.StatusRequisicao;
import com.hemoflow.hemoflow.dominio.TipoSanguineo;
import com.hemoflow.hemoflow.estatistica.EstatisticaDTOs.*;

class AnaliseAvancadaServiceTest {

    private static final LocalDateTime BASE = LocalDateTime.of(2026, 10, 1, 10, 0);

    private static Requisicao req(TipoSanguineo t, int qtd, int dia, StatusRequisicao status) {
        Requisicao r = new Requisicao(null, t, Hemocomponente.HEMACIAS, qtd, BASE.plusDays(dia).plusHours(6));
        ReflectionTestUtils.setField(r, "dataSolicitacao", BASE.plusDays(dia));
        r.setStatus(status);
        return r;
    }

    @Test
    void demandaComTendenciaCrescente() {
        List<Requisicao> rs = new ArrayList<>();
        for (int d = 0; d < 5; d++) {
            rs.add(req(TipoSanguineo.O_NEG, d + 1, d, StatusRequisicao.PENDENTE)); // 1,2,3,4,5
        }
        AnaliseDemanda a = AnaliseAvancadaService.calcularDemanda(rs);
        assertEquals(5, a.quantidadePorRequisicao().n());
        assertEquals(3.0, a.quantidadePorRequisicao().media(), 1e-9);
        assertEquals(3.0, a.quantidadePorRequisicao().mediana(), 1e-9);
        assertEquals(5, a.serieDiaria().size());
        assertNull(a.serieDiaria().get(1).mediaMovel3());
        assertEquals(2.0, a.serieDiaria().get(2).mediaMovel3(), 1e-9);
        assertEquals(1.0, a.tendencia().inclinacaoUnidadesPorDia(), 1e-9);
        assertEquals(6.0, a.tendencia().previsaoProximoDia(), 1e-9);
        assertEquals(1.0, a.tendencia().r2(), 1e-9);
        long soma = a.histograma().stream().mapToLong(FaixaHistograma::frequencia).sum();
        assertEquals(5, soma);
    }

    @Test
    void demandaVaziaNaoQuebra() {
        AnaliseDemanda a = AnaliseAvancadaService.calcularDemanda(List.of());
        assertEquals(0, a.quantidadePorRequisicao().n());
        assertNull(a.quantidadePorRequisicao().media());
        assertNull(a.tendencia());
        assertTrue(a.serieDiaria().isEmpty());
    }

    @Test
    void probabilidadeBayesPoissonBinomial() {
        List<Requisicao> rs = new ArrayList<>();
        // 4 O_NEG (3 atendidas), 6 A_POS (2 atendidas), em 5 dias → λ = 2/dia
        for (int i = 0; i < 4; i++) {
            rs.add(req(TipoSanguineo.O_NEG, 1, i, i < 3 ? StatusRequisicao.ENTREGUE : StatusRequisicao.PENDENTE));
        }
        for (int i = 0; i < 6; i++) {
            rs.add(req(TipoSanguineo.A_POS, 1, i % 5, i < 2 ? StatusRequisicao.ALOCADA : StatusRequisicao.CANCELADA));
        }
        AnaliseProbabilidade p = AnaliseAvancadaService.calcularProbabilidade(rs);
        assertEquals(10, p.totalRequisicoes());
        assertEquals(0.4, p.frequenciaRelativaPorTipo().get("O_NEG"), 1e-9);
        assertEquals(0.75, p.pAtendidaDadoTipo().get("O_NEG"), 1e-9);
        assertEquals(0.5, p.pAtendimentoGeral(), 1e-9);
        // Bayes: P(O_NEG | atendida) = 3/5
        ResultadoBayes b = p.bayes().stream().filter(x -> x.tipoSanguineo().equals("O_NEG")).findFirst().orElseThrow();
        assertEquals(0.6, b.pTipoDadoAtendida(), 1e-9);
        assertEquals(2.0, p.poisson().lambdaRequisicoesPorDia(), 1e-9);
        assertEquals(Math.exp(-2), p.poisson().probNenhumaRequisicao(), 1e-9);
        assertEquals(11, p.poisson().distribuicao().size());
        assertEquals(5.0, p.binomial().esperanca(), 1e-9);
        assertEquals(2.5, p.binomial().variancia(), 1e-9);
        assertTrue(p.binomial().probPeloMenosK() > 0 && p.binomial().probPeloMenosK() < 0.1);
    }

    @Test
    void riscoDesabastecimentoPoisson() {
        LocalDate hoje = LocalDate.of(2026, 10, 10);
        List<Bolsa> bolsas = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            bolsas.add(new Bolsa(TipoSanguineo.O_NEG, Hemocomponente.HEMACIAS, hoje.minusDays(2),
                    hoje.plusDays(20), "L" + i, null));
        }
        // vencida: não conta
        bolsas.add(new Bolsa(TipoSanguineo.O_NEG, Hemocomponente.HEMACIAS, hoje.minusDays(40),
                hoje.minusDays(1), "LV", null));
        // demanda de 2 unidades/dia de O_NEG em 2 dias → λ(7d)=14 unidades, estoque 3 → risco alto
        List<Requisicao> rs = List.of(
                req(TipoSanguineo.O_NEG, 2, 0, StatusRequisicao.PENDENTE),
                req(TipoSanguineo.O_NEG, 2, 1, StatusRequisicao.PENDENTE));
        RiscoDesabastecimento r = AnaliseAvancadaService.calcularRisco(bolsas, rs, hoje);
        RiscoTipo onegativo = r.tipos().stream().filter(t -> t.tipoSanguineo().equals("O_NEG")).findFirst().orElseThrow();
        assertEquals(3, onegativo.estoqueDisponivel());
        assertEquals(2.0, onegativo.demandaMediaDiaria(), 1e-9);
        assertEquals(14.0, onegativo.lambdaHorizonte(), 1e-9);
        assertEquals("ALTO", onegativo.nivel());
        assertEquals(EstatisticaMath.poissonCauda(3, 14), onegativo.probRuptura(), 1e-12);
        assertTrue(onegativo.estoqueSegurancaSugerido() > 14);
        RiscoTipo semDemanda = r.tipos().stream().filter(t -> t.tipoSanguineo().equals("A_POS")).findFirst().orElseThrow();
        assertEquals(0.0, semDemanda.probRuptura(), 1e-12);
        assertEquals("BAIXO", semDemanda.nivel());
    }

    @Test
    void temperaturaIcTesteENormal() {
        Bolsa b = new Bolsa(TipoSanguineo.O_NEG, Hemocomponente.HEMACIAS, LocalDate.now(), LocalDate.now().plusDays(10), "L", null);
        List<LeituraTemperatura> ls = new ArrayList<>();
        double[] temps = {24, 25, 26, 25, 24, 26, 25, 25, 31, 19}; // 2 fora de [20,30]
        for (double t : temps) {
            ls.add(new LeituraTemperatura(b, t, 20, 30, t < 20 || t > 30));
        }
        AnaliseTemperatura a = AnaliseAvancadaService.calcularTemperatura(ls);
        assertEquals(10, a.leituras());
        assertEquals(2, a.leiturasForaDaFaixa());
        assertEquals(20.0, a.percentualForaDaFaixa(), 1e-9);
        assertEquals(25.0, a.pontoMedioFaixa(), 1e-9);
        assertEquals(25.0, a.temperatura().media(), 1e-9);
        assertNotNull(a.pValor());
        assertTrue(a.pValor() > 0.05); // média = ponto médio → não rejeita H0
        assertTrue(a.probForaDaFaixaNormal() > 0 && a.probForaDaFaixaNormal() < 0.2);
        assertTrue(a.icInferiorForaFaixa95() < 20.0 && a.icSuperiorForaFaixa95() > 20.0);
    }

    @Test
    void temperaturaSemLeituras() {
        AnaliseTemperatura a = AnaliseAvancadaService.calcularTemperatura(List.of());
        assertEquals(0, a.leituras());
        assertNull(a.percentualForaDaFaixa());
        assertNull(a.pValor());
    }

    // ---------------------------------------------------------------------
    // Taxa de descarte por vencimento
    // ---------------------------------------------------------------------

    private static Bolsa bolsa(TipoSanguineo t, LocalDate validade, com.hemoflow.hemoflow.dominio.StatusBolsa status) {
        LocalDate hoje = LocalDate.of(2026, 10, 10);
        Bolsa b = new Bolsa(t, Hemocomponente.HEMACIAS, hoje.minusDays(40), validade, "L", null);
        if (status != com.hemoflow.hemoflow.dominio.StatusBolsa.DISPONIVEL) {
            b.setStatus(status);
        }
        return b;
    }

    @Test
    void descartePorVencimentoTaxaEIc() {
        LocalDate hoje = LocalDate.now();
        var disp = com.hemoflow.hemoflow.dominio.StatusBolsa.DISPONIVEL;
        var desc = com.hemoflow.hemoflow.dominio.StatusBolsa.DESCARTADA;
        List<Bolsa> bolsas = new ArrayList<>();
        // 10 bolsas O_POS: 2 descartadas vencidas (VENCIMENTO), 1 descartada dentro da validade (OUTRO), 1 vencida no estoque
        bolsas.add(bolsa(TipoSanguineo.O_POS, hoje.minusDays(3), desc));
        bolsas.add(bolsa(TipoSanguineo.O_POS, hoje.minusDays(1), desc));
        bolsas.add(bolsa(TipoSanguineo.O_POS, hoje.plusDays(5), desc));
        bolsas.add(bolsa(TipoSanguineo.O_POS, hoje.minusDays(2), disp));
        for (int i = 0; i < 6; i++) {
            bolsas.add(bolsa(TipoSanguineo.O_POS, hoje.plusDays(10), disp));
        }
        // 10 bolsas A_POS, nenhuma descartada
        for (int i = 0; i < 10; i++) {
            bolsas.add(bolsa(TipoSanguineo.A_POS, hoje.plusDays(10), disp));
        }
        AnaliseDescarte d = AnaliseAvancadaService.calcularDescarte(bolsas, hoje);
        assertEquals(20, d.totalColetadas());
        assertEquals(2, d.descartadasPorVencimento());
        assertEquals(1, d.descartadasOutrosMotivos());
        assertEquals(1, d.vencidasNaoDescartadas());
        assertEquals(10.0, d.taxaPercent(), 1e-9);               // 2/20
        double margem = 1.959964 * Math.sqrt(0.1 * 0.9 / 20) * 100;
        assertEquals(Math.max(0.0, 10.0 - margem), d.icInferior95(), 1e-6);  // proporção não pode ser negativa: trunca em 0
        assertEquals(Math.min(100.0, 10.0 + margem), d.icSuperior95(), 1e-6);
        DescarteTipo opos = d.porTipo().stream().filter(t -> t.tipoSanguineo().equals("O_POS")).findFirst().orElseThrow();
        assertEquals(10, opos.coletadas());
        assertEquals(20.0, opos.taxaPercent(), 1e-9);            // 2/10
        DescarteTipo apos = d.porTipo().stream().filter(t -> t.tipoSanguineo().equals("A_POS")).findFirst().orElseThrow();
        assertEquals(0.0, apos.taxaPercent(), 1e-9);
        assertEquals(0.0, apos.icInferior95(), 1e-9);
        // tipo sem bolsas: indefinido (null), nunca NaN
        DescarteTipo ab = d.porTipo().stream().filter(t -> t.tipoSanguineo().equals("AB_NEG")).findFirst().orElseThrow();
        assertEquals(0, ab.coletadas());
        assertNull(ab.taxaPercent());
        assertNull(ab.icInferior95());
    }

    @Test
    void descarteSemBolsasNaoQuebra() {
        AnaliseDescarte d = AnaliseAvancadaService.calcularDescarte(List.of(), LocalDate.now());
        assertEquals(0, d.totalColetadas());
        assertNull(d.taxaPercent());
        assertNull(d.icInferior95());
    }

    @Test
    void descarteExplicitoPorVencimento() {
        Bolsa b = new Bolsa(TipoSanguineo.O_NEG, Hemocomponente.HEMACIAS, LocalDate.now().minusDays(10),
                LocalDate.now().plusDays(5), "L", null);
        b.descartar("VENCIMENTO");
        assertEquals("VENCIMENTO", b.getMotivoDescarte());
        assertTrue(AnaliseAvancadaService.descartadaPorVencimento(b));
    }

    // ---------------------------------------------------------------------
    // Demanda por hospital
    // ---------------------------------------------------------------------

    @Test
    void demandaPorHospitalAgrupaEOrdena() {
        com.hemoflow.hemoflow.dominio.Hospital a = new com.hemoflow.hemoflow.dominio.Hospital("Hosp A", null);
        com.hemoflow.hemoflow.dominio.Hospital b = new com.hemoflow.hemoflow.dominio.Hospital("Hosp B", null);
        List<Requisicao> rs = new ArrayList<>();
        int[] qa = {2, 4, 6};
        for (int q : qa) {
            Requisicao r = new Requisicao(a, TipoSanguineo.O_NEG, Hemocomponente.HEMACIAS, q, BASE.plusHours(5));
            rs.add(r);
        }
        rs.add(new Requisicao(b, TipoSanguineo.O_NEG, Hemocomponente.HEMACIAS, 3, BASE.plusHours(5)));
        rs.add(new Requisicao(null, TipoSanguineo.O_NEG, Hemocomponente.HEMACIAS, 1, BASE.plusHours(5)));
        List<DemandaHospital> d = AnaliseAvancadaService.calcularDemandaPorHospital(rs);
        assertEquals(3, d.size());
        assertEquals("Hosp A", d.get(0).hospital());        // 12 unidades: maior primeiro
        assertEquals(3, d.get(0).requisicoes());
        assertEquals(12, d.get(0).unidades());
        assertEquals(4.0, d.get(0).mediaUnidades(), 1e-9);
        assertEquals(2.0, d.get(0).desvioPadraoUnidades(), 1e-9);  // dp amostral de {2,4,6}
        assertEquals("Hosp B", d.get(1).hospital());
        assertEquals(3.0, d.get(1).mediaUnidades(), 1e-9);
        assertNull(d.get(1).desvioPadraoUnidades());                // n=1: indefinido
        assertEquals("Sem hospital", d.get(2).hospital());
        assertTrue(AnaliseAvancadaService.calcularDemandaPorHospital(List.of()).isEmpty());
    }

    // ---------------------------------------------------------------------
    // Tempos
    // ---------------------------------------------------------------------

    @Test
    void temposDeEntregaEPrazo() {
        Bolsa b = new Bolsa(TipoSanguineo.O_NEG, Hemocomponente.HEMACIAS, LocalDate.now(), LocalDate.now().plusDays(9), "L", null);
        List<Requisicao> rs = new ArrayList<>();
        List<com.hemoflow.hemoflow.dominio.Entrega> es = new ArrayList<>();
        long[] minutos = {10, 20, 30, 40};
        int[] prazoHoras = {2, 4, 6, 8};
        for (int i = 0; i < 4; i++) {
            Requisicao r = new Requisicao(null, TipoSanguineo.O_NEG, Hemocomponente.HEMACIAS, 1, BASE.plusHours(prazoHoras[i]));
            ReflectionTestUtils.setField(r, "dataSolicitacao", BASE);
            rs.add(r);
            es.add(new com.hemoflow.hemoflow.dominio.Entrega(r, b, List.of(), minutos[i], 5.0, false));
        }
        // requisição com dados ausentes: ignorada e contada
        Requisicao semData = new Requisicao(null, TipoSanguineo.O_NEG, Hemocomponente.HEMACIAS, 1, null);
        rs.add(semData);

        AnaliseTempos t = AnaliseAvancadaService.calcularTempos(es, rs);
        assertEquals(4, t.tempoEntregaMinutos().n());
        assertEquals(25.0, t.tempoEntregaMinutos().media(), 1e-9);
        assertEquals(25.0, t.tempoEntregaMinutos().mediana(), 1e-9);
        assertEquals(Math.sqrt(500.0 / 3.0), t.tempoEntregaMinutos().desvioPadrao(), 1e-9);  // Σ(x−25)²=500
        assertEquals(4, t.prazoAtendimentoHoras().n());
        assertEquals(5.0, t.prazoAtendimentoHoras().media(), 1e-9);
        assertEquals(5.0, t.prazoAtendimentoHoras().mediana(), 1e-9);
        assertEquals(Math.sqrt(20.0 / 3.0), t.prazoAtendimentoHoras().desvioPadrao(), 1e-9); // Σ(x−5)²=20
        assertEquals(1, t.requisicoesSemDados());
    }

    @Test
    void temposSemDadosNaoQuebram() {
        AnaliseTempos t = AnaliseAvancadaService.calcularTempos(List.of(), List.of());
        assertEquals(0, t.tempoEntregaMinutos().n());
        assertNull(t.tempoEntregaMinutos().media());
        assertNull(t.prazoAtendimentoHoras().desvioPadrao());
        assertEquals(0, t.requisicoesSemDados());
    }
}

