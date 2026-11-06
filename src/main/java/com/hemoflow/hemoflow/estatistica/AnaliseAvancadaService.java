package com.hemoflow.hemoflow.estatistica;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hemoflow.hemoflow.dominio.Bolsa;
import com.hemoflow.hemoflow.dominio.Entrega;
import com.hemoflow.hemoflow.dominio.LeituraTemperatura;
import com.hemoflow.hemoflow.dominio.Requisicao;
import com.hemoflow.hemoflow.dominio.StatusBolsa;
import com.hemoflow.hemoflow.dominio.StatusRequisicao;
import com.hemoflow.hemoflow.dominio.TipoSanguineo;
import com.hemoflow.hemoflow.estatistica.EstatisticaDTOs.*;
import com.hemoflow.hemoflow.persistencia.BolsaRepository;
import com.hemoflow.hemoflow.persistencia.EntregaRepository;
import com.hemoflow.hemoflow.persistencia.LeituraTemperaturaRepository;
import com.hemoflow.hemoflow.persistencia.RequisicaoRepository;

/**
 * Análise estatística avançada: estatística descritiva completa, séries
 * temporais, correlação/regressão, intervalos de confiança, teste de hipótese e
 * modelos de probabilidade (Poisson, Binomial, Normal, Bayes).
 * As fórmulas estão documentadas em docs/estatistica.md.
 */
@Service
public class AnaliseAvancadaService {

    /** Horizonte (dias) para o risco de desabastecimento. */
    static final int HORIZONTE_DIAS = 7;
    /** Nível de serviço alvo para o estoque de segurança. */
    static final double NIVEL_SERVICO = 0.95;
    /** Tamanho do lote usado no modelo binomial. */
    static final int N_BINOMIAL = 10;
    static final int K_BINOMIAL = 8;

    private final BolsaRepository bolsaRepository;
    private final RequisicaoRepository requisicaoRepository;
    private final LeituraTemperaturaRepository leituraRepository;
    private final EntregaRepository entregaRepository;

    public AnaliseAvancadaService(BolsaRepository bolsaRepository,
                                  RequisicaoRepository requisicaoRepository,
                                  LeituraTemperaturaRepository leituraRepository,
                                  EntregaRepository entregaRepository) {
        this.bolsaRepository = bolsaRepository;
        this.requisicaoRepository = requisicaoRepository;
        this.leituraRepository = leituraRepository;
        this.entregaRepository = entregaRepository;
    }

    // ---------------------------------------------------------------------
    // Pontos de entrada (leem do banco)
    // ---------------------------------------------------------------------

    @Transactional(readOnly = true)
    public AnaliseDemanda analiseDemanda() {
        return calcularDemanda(requisicaoRepository.findAll());
    }

    @Transactional(readOnly = true)
    public AnaliseProbabilidade analiseProbabilidade() {
        return calcularProbabilidade(requisicaoRepository.findAll());
    }

    @Transactional(readOnly = true)
    public RiscoDesabastecimento riscoDesabastecimento() {
        return calcularRisco(bolsaRepository.findAll(), requisicaoRepository.findAll(), LocalDate.now());
    }

    @Transactional(readOnly = true)
    public AnaliseTemperatura analiseTemperatura() {
        return calcularTemperatura(leituraRepository.findAll());
    }

    @Transactional(readOnly = true)
    public AnaliseDescarte analiseDescarte() {
        return calcularDescarte(bolsaRepository.findAll(), LocalDate.now());
    }

    @Transactional(readOnly = true)
    public List<DemandaHospital> demandaPorHospital() {
        return calcularDemandaPorHospital(requisicaoRepository.findAll());
    }

    @Transactional(readOnly = true)
    public AnaliseTempos analiseTempos() {
        return calcularTempos(entregaRepository.findAll(), requisicaoRepository.findAll());
    }

    // ---------------------------------------------------------------------
    // Cálculos (puros, testáveis sem banco)
    // ---------------------------------------------------------------------

    static AnaliseDemanda calcularDemanda(List<Requisicao> requisicoes) {
        List<Double> quantidades = requisicoes.stream().map(r -> (double) r.getQuantidade()).toList();

        // Série diária completa (dias sem requisição valem 0)
        TreeMap<LocalDate, Double> porDia = new TreeMap<>();
        for (Requisicao r : requisicoes) {
            porDia.merge(r.getDataSolicitacao().toLocalDate(), (double) r.getQuantidade(), Double::sum);
        }
        List<PontoSerie> serie = new ArrayList<>();
        List<Double> valores = new ArrayList<>();
        if (!porDia.isEmpty()) {
            for (LocalDate d = porDia.firstKey(); !d.isAfter(porDia.lastKey()); d = d.plusDays(1)) {
                valores.add(porDia.getOrDefault(d, 0.0));
            }
            List<Double> mm = EstatisticaMath.mediaMovel(valores, 3);
            LocalDate d = porDia.firstKey();
            for (int i = 0; i < valores.size(); i++) {
                Double media3 = i >= 2 ? mm.get(i - 2) : null;
                serie.add(new PontoSerie(d.plusDays(i).toString(), valores.get(i), media3));
            }
        }

        // Tendência: regressão da demanda diária sobre o índice do dia
        Tendencia tendencia = null;
        List<Double> dias = new ArrayList<>();
        for (int i = 0; i < valores.size(); i++) {
            dias.add((double) i);
        }
        EstatisticaMath.Regressao reg = EstatisticaMath.regressaoLinear(dias, valores);
        if (reg != null) {
            tendencia = new Tendencia(reg.inclinacao(), reg.intercepto(), reg.r(), reg.r2(),
                    reg.prever(valores.size()));
        }

        // Correlação entre quantidade solicitada e prazo (horas até a janela de entrega)
        List<Double> prazos = requisicoes.stream()
                .map(r -> (double) ChronoUnit.MINUTES.between(r.getDataSolicitacao(), r.getJanelaEntrega()) / 60.0)
                .toList();

        return new AnaliseDemanda(
                descritiva(quantidades),
                histograma(quantidades),
                serie,
                descritiva(valores),
                tendencia,
                EstatisticaMath.correlacao(quantidades, prazos));
    }

    static AnaliseProbabilidade calcularProbabilidade(List<Requisicao> requisicoes) {
        long total = requisicoes.size();
        Map<String, Double> freq = new LinkedHashMap<>();
        Map<String, Double> condicional = new LinkedHashMap<>();
        List<ResultadoBayes> bayes = new ArrayList<>();

        long atendidasTotal = requisicoes.stream().filter(AnaliseAvancadaService::atendida).count();
        Double pAtend = EstatisticaMath.probCondicional(atendidasTotal, total);

        for (TipoSanguineo t : TipoSanguineo.values()) {
            long doTipo = requisicoes.stream().filter(r -> r.getTipoSanguineo() == t).count();
            long atendidasDoTipo = requisicoes.stream()
                    .filter(r -> r.getTipoSanguineo() == t && atendida(r)).count();
            long atendidasForaTipo = atendidasTotal - atendidasDoTipo;
            long foraTipo = total - doTipo;

            double pTipo = total == 0 ? 0 : (double) doTipo / total;
            Double pBA = EstatisticaMath.probCondicional(atendidasDoTipo, doTipo);
            Double pBnA = EstatisticaMath.probCondicional(atendidasForaTipo, foraTipo);
            freq.put(t.name(), pTipo);
            condicional.put(t.name(), pBA == null ? 0.0 : pBA);

            if (pBA != null) {
                Double posterior = EstatisticaMath.bayes(pBA, pTipo, pBnA == null ? 0.0 : pBnA);
                bayes.add(new ResultadoBayes(t.name(), pTipo, pBA, posterior == null ? 0.0 : posterior));
            }
        }

        double[] ic = EstatisticaMath.icProporcao95(atendidasTotal, total);

        // Poisson: chegadas de requisições por dia
        double lambda = total == 0 ? 0 : (double) total / diasDeObservacao(requisicoes);
        List<PontoPoisson> dist = new ArrayList<>();
        for (int k = 0; k <= 10; k++) {
            dist.add(new PontoPoisson(k, EstatisticaMath.poissonPmf(k, lambda), EstatisticaMath.poissonCdf(k, lambda)));
        }
        ModeloPoisson poisson = new ModeloPoisson(
                lambda,
                EstatisticaMath.poissonPmf(0, lambda),
                1.0 - EstatisticaMath.poissonPmf(0, lambda),
                EstatisticaMath.poissonCauda((int) Math.floor(2 * lambda), lambda),
                dist);

        // Binomial: sucessos em N requisições independentes
        double p = pAtend == null ? 0 : pAtend;
        ModeloBinomial binomial = new ModeloBinomial(N_BINOMIAL, K_BINOMIAL, p,
                EstatisticaMath.binomialCaudaSuperior(K_BINOMIAL, N_BINOMIAL, p),
                N_BINOMIAL * p, N_BINOMIAL * p * (1 - p));

        return new AnaliseProbabilidade(total, freq, condicional, bayes, pAtend,
                ic == null ? null : ic[0], ic == null ? null : ic[1], poisson, binomial);
    }

    static RiscoDesabastecimento calcularRisco(List<Bolsa> bolsas, List<Requisicao> requisicoes, LocalDate hoje) {
        double dias = requisicoes.isEmpty() ? 1 : diasDeObservacao(requisicoes);
        Map<TipoSanguineo, Long> estoque = new EnumMap<>(TipoSanguineo.class);
        Map<TipoSanguineo, Long> demanda = new EnumMap<>(TipoSanguineo.class);
        for (Bolsa b : bolsas) {
            if (b.getStatus() == StatusBolsa.DISPONIVEL && !b.getDataValidade().isBefore(hoje)) {
                estoque.merge(b.getTipoSanguineo(), 1L, Long::sum);
            }
        }
        for (Requisicao r : requisicoes) {
            if (r.getStatus() != StatusRequisicao.CANCELADA) {
                demanda.merge(r.getTipoSanguineo(), (long) r.getQuantidade(), Long::sum);
            }
        }

        List<RiscoTipo> lista = new ArrayList<>();
        for (TipoSanguineo t : TipoSanguineo.values()) {
            long est = estoque.getOrDefault(t, 0L);
            double diaria = demanda.getOrDefault(t, 0L) / dias;
            double lambda = diaria * HORIZONTE_DIAS;
            double pRuptura = EstatisticaMath.poissonCauda((int) Math.min(est, Integer.MAX_VALUE), lambda);
            String nivel = pRuptura < 0.05 ? "BAIXO" : pRuptura < 0.20 ? "MEDIO" : "ALTO";
            lista.add(new RiscoTipo(t.name(), est, diaria, lambda, pRuptura, nivel, estoqueSeguranca(lambda)));
        }
        return new RiscoDesabastecimento(HORIZONTE_DIAS, NIVEL_SERVICO, lista);
    }

    /**
     * Taxa de descarte por vencimento = descartadas por vencimento / bolsas coletadas
     * (todas as bolsas registradas, em qualquer status). IC 95% de Wald.
     */
    static AnaliseDescarte calcularDescarte(List<Bolsa> bolsas, LocalDate hoje) {
        long total = bolsas.size();
        long venc = bolsas.stream().filter(AnaliseAvancadaService::descartadaPorVencimento).count();
        long outros = bolsas.stream()
                .filter(b -> b.getStatus() == StatusBolsa.DESCARTADA && !descartadaPorVencimento(b)).count();
        long vencidasNoEstoque = bolsas.stream()
                .filter(b -> b.getStatus() == StatusBolsa.DISPONIVEL && b.getDataValidade().isBefore(hoje)).count();

        List<DescarteTipo> porTipo = new ArrayList<>();
        for (TipoSanguineo t : TipoSanguineo.values()) {
            long coletadas = bolsas.stream().filter(b -> b.getTipoSanguineo() == t).count();
            long descartadas = bolsas.stream()
                    .filter(b -> b.getTipoSanguineo() == t && descartadaPorVencimento(b)).count();
            double[] ic = EstatisticaMath.icProporcao95(descartadas, coletadas);
            porTipo.add(new DescarteTipo(t.name(), coletadas, descartadas,
                    coletadas == 0 ? null : descartadas * 100.0 / coletadas,
                    ic == null ? null : ic[0] * 100, ic == null ? null : ic[1] * 100));
        }
        double[] ic = EstatisticaMath.icProporcao95(venc, total);
        return new AnaliseDescarte(total, venc, outros, vencidasNoEstoque,
                total == 0 ? null : venc * 100.0 / total,
                ic == null ? null : ic[0] * 100, ic == null ? null : ic[1] * 100, porTipo);
    }

    static boolean descartadaPorVencimento(Bolsa b) {
        return b.getStatus() == StatusBolsa.DESCARTADA && "VENCIMENTO".equals(b.getMotivoDescarte());
    }

    /** Demanda agrupada por hospital (ordenada por unidades, decrescente). */
    static List<DemandaHospital> calcularDemandaPorHospital(List<Requisicao> requisicoes) {
        Map<String, List<Double>> porHospital = new LinkedHashMap<>();
        for (Requisicao r : requisicoes) {
            String nome = r.getHospital() == null ? "Sem hospital" : r.getHospital().getNome();
            porHospital.computeIfAbsent(nome, k -> new ArrayList<>()).add((double) r.getQuantidade());
        }
        List<DemandaHospital> lista = new ArrayList<>();
        porHospital.forEach((nome, qtds) -> lista.add(new DemandaHospital(nome, qtds.size(),
                Math.round(qtds.stream().mapToDouble(Double::doubleValue).sum()),
                EstatisticaMath.media(qtds), EstatisticaMath.desvioPadrao(qtds))));
        lista.sort((a, b) -> Long.compare(b.unidades(), a.unidades()));
        return lista;
    }

    /**
     * Tempos: duração estimada das entregas (minutos) e prazo de atendimento
     * (janelaEntrega − dataSolicitacao, em horas). Requisições com data ausente são
     * ignoradas e contadas em {@code requisicoesSemDados}.
     */
    static AnaliseTempos calcularTempos(List<Entrega> entregas, List<Requisicao> requisicoes) {
        List<Double> tempos = entregas.stream().map(e -> (double) e.getTempoEstimadoMinutos()).toList();
        List<Double> prazos = new ArrayList<>();
        long semDados = 0;
        for (Requisicao r : requisicoes) {
            if (r.getDataSolicitacao() == null || r.getJanelaEntrega() == null) {
                semDados++;
            } else {
                prazos.add(ChronoUnit.MINUTES.between(r.getDataSolicitacao(), r.getJanelaEntrega()) / 60.0);
            }
        }
        return new AnaliseTempos(descritiva(tempos), descritiva(prazos), semDados);
    }
    static AnaliseTemperatura calcularTemperatura(List<LeituraTemperatura> leituras) {
        List<Double> temps = leituras.stream().map(LeituraTemperatura::getTemperaturaCelsius).toList();
        long fora = leituras.stream().filter(l -> l.isAlerta()
                || l.getTemperaturaCelsius() < l.getMinimoConfiguradoCelsius()
                || l.getTemperaturaCelsius() > l.getMaximoConfiguradoCelsius()).count();
        long n = leituras.size();
        Double percFora = n == 0 ? null : fora * 100.0 / n;
        double[] ic = EstatisticaMath.icProporcao95(fora, n);

        Double pontoMedio = n == 0 ? null : leituras.stream()
                .mapToDouble(l -> (l.getMinimoConfiguradoCelsius() + l.getMaximoConfiguradoCelsius()) / 2.0)
                .average().orElse(0);
        Double z = pontoMedio == null ? null : EstatisticaMath.estatisticaTeste(temps, pontoMedio);
        Double pValor = z == null ? null : EstatisticaMath.pValorBilateral(z);
        String conclusao = pValor == null ? "Amostra insuficiente ou sem variação para o teste"
                : pValor < 0.05
                ? "Rejeita H0 (α=5%): a temperatura média difere do ponto médio da faixa"
                : "Não rejeita H0 (α=5%): sem evidência de desvio da temperatura média";

        // P(T fora da faixa) assumindo T ~ Normal(média, desvio)
        Double probFora = null;
        Double dp = EstatisticaMath.desvioPadrao(temps);
        if (n > 0 && dp != null && dp > 0) {
            double min = leituras.stream().mapToDouble(LeituraTemperatura::getMinimoConfiguradoCelsius).average().orElse(0);
            double max = leituras.stream().mapToDouble(LeituraTemperatura::getMaximoConfiguradoCelsius).average().orElse(0);
            double mu = EstatisticaMath.media(temps);
            probFora = EstatisticaMath.normalCdf(min, mu, dp) + (1 - EstatisticaMath.normalCdf(max, mu, dp));
        }

        return new AnaliseTemperatura(descritiva(temps), n, fora, percFora,
                ic == null ? null : ic[0] * 100, ic == null ? null : ic[1] * 100,
                pontoMedio, z, pValor, conclusao, probFora);
    }

    // ---------------------------------------------------------------------
    // Auxiliares
    // ---------------------------------------------------------------------

    static Descritiva descritiva(List<Double> x) {
        double[] ic = EstatisticaMath.icMedia95(x);
        return new Descritiva(
                x.size(),
                EstatisticaMath.media(x),
                EstatisticaMath.mediana(x),
                EstatisticaMath.moda(x),
                x.isEmpty() ? null : java.util.Collections.min(x),
                EstatisticaMath.percentil(x, 25),
                EstatisticaMath.percentil(x, 75),
                x.isEmpty() ? null : java.util.Collections.max(x),
                EstatisticaMath.amplitude(x),
                EstatisticaMath.variancia(x),
                EstatisticaMath.desvioPadrao(x),
                EstatisticaMath.coeficienteVariacao(x),
                ic == null ? null : ic[0],
                ic == null ? null : ic[1]);
    }

    /** Histograma com número de classes pela regra de Sturges: k = ceil(1 + log2 n). */
    static List<FaixaHistograma> histograma(List<Double> x) {
        List<FaixaHistograma> faixas = new ArrayList<>();
        if (x.isEmpty()) {
            return faixas;
        }
        double min = java.util.Collections.min(x);
        double max = java.util.Collections.max(x);
        int k = (int) Math.ceil(1 + Math.log(x.size()) / Math.log(2));
        if (max == min) {
            faixas.add(new FaixaHistograma(fmt(min), x.size(), 1.0));
            return faixas;
        }
        double largura = (max - min) / k;
        long[] cont = new long[k];
        for (double v : x) {
            cont[Math.min(k - 1, (int) ((v - min) / largura))]++;
        }
        for (int i = 0; i < k; i++) {
            String rotulo = fmt(min + i * largura) + "–" + fmt(min + (i + 1) * largura);
            faixas.add(new FaixaHistograma(rotulo, cont[i], (double) cont[i] / x.size()));
        }
        return faixas;
    }

    private static String fmt(double v) {
        return String.format(java.util.Locale.ROOT, "%.1f", v);
    }

    /** Menor estoque s tal que P(D &lt;= s) &gt;= nível de serviço, com D ~ Poisson(lambda). */
    static long estoqueSeguranca(double lambda) {
        int limite = (int) (lambda + 10 * Math.sqrt(lambda) + 50);
        for (int s = 0; s <= limite; s++) {
            if (EstatisticaMath.poissonCdf(s, lambda) >= NIVEL_SERVICO) {
                return s;
            }
        }
        return limite;
    }

    /** Critério único de atendimento, compartilhado com {@link EstatisticaService}. */
    static boolean atendida(Requisicao r) {
        return EstatisticaService.atendida(r);
    }

    /** Número de dias (inclusivo) entre a primeira e a última solicitação; mínimo 1. */
    static double diasDeObservacao(List<Requisicao> requisicoes) {
        LocalDate min = requisicoes.stream().map(r -> r.getDataSolicitacao().toLocalDate()).min(LocalDate::compareTo).orElse(LocalDate.now());
        LocalDate max = requisicoes.stream().map(r -> r.getDataSolicitacao().toLocalDate()).max(LocalDate::compareTo).orElse(min);
        return Math.max(1, ChronoUnit.DAYS.between(min, max) + 1);
    }
}

