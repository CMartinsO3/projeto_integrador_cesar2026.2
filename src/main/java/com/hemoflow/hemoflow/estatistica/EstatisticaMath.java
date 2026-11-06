package com.hemoflow.hemoflow.estatistica;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Biblioteca de funções estatísticas e probabilísticas (sem dependências externas).
 * Todas as funções são puras e determinísticas, o que facilita testes e a
 * conferência manual das fórmulas (ver docs/estatistica.md).
 *
 * Convenção: quando a medida não é definida (amostra pequena demais),
 * o retorno é {@code null} para que a serialização JSON permaneça válida.
 */
public final class EstatisticaMath {

    /** Valor crítico z para 95% de confiança (bilateral). */
    public static final double Z_95 = 1.959964;

    /** Valores críticos t de Student bilateral 95% para gl = 1..30. */
    private static final double[] T_95 = {
        12.706, 4.303, 3.182, 2.776, 2.571, 2.447, 2.365, 2.306, 2.262, 2.228,
        2.201, 2.179, 2.160, 2.145, 2.131, 2.120, 2.110, 2.101, 2.093, 2.086,
        2.080, 2.074, 2.069, 2.064, 2.060, 2.056, 2.052, 2.048, 2.045, 2.042
    };

    private EstatisticaMath() {
    }

    // ---------------------------------------------------------------------
    // Medidas de posição
    // ---------------------------------------------------------------------

    public static Double media(List<Double> x) {
        if (x.isEmpty()) {
            return null;
        }
        double soma = 0;
        for (double v : x) {
            soma += v;
        }
        return soma / x.size();
    }

    public static Double mediana(List<Double> x) {
        return percentil(x, 50);
    }

    /** Moda (menor valor entre os mais frequentes); null se não há repetição. */
    public static Double moda(List<Double> x) {
        if (x.isEmpty()) {
            return null;
        }
        List<Double> s = ordenada(x);
        double melhor = s.get(0);
        int melhorFreq = 1;
        int freq = 1;
        for (int i = 1; i < s.size(); i++) {
            freq = s.get(i).equals(s.get(i - 1)) ? freq + 1 : 1;
            if (freq > melhorFreq) {
                melhorFreq = freq;
                melhor = s.get(i);
            }
        }
        return melhorFreq == 1 && s.size() > 1 ? null : melhor;
    }

    /** Percentil p (0..100) por interpolação linear (mesmo método do Excel PERCENTIL.INC). */
    public static Double percentil(List<Double> x, double p) {
        if (x.isEmpty()) {
            return null;
        }
        List<Double> s = ordenada(x);
        double pos = (p / 100.0) * (s.size() - 1);
        int baixo = (int) Math.floor(pos);
        int alto = (int) Math.ceil(pos);
        return s.get(baixo) + (pos - baixo) * (s.get(alto) - s.get(baixo));
    }

    // ---------------------------------------------------------------------
    // Medidas de dispersão
    // ---------------------------------------------------------------------

    /** Variância amostral (divisor n-1). */
    public static Double variancia(List<Double> x) {
        if (x.size() < 2) {
            return null;
        }
        double m = media(x);
        double soma = 0;
        for (double v : x) {
            soma += (v - m) * (v - m);
        }
        return soma / (x.size() - 1);
    }

    public static Double desvioPadrao(List<Double> x) {
        Double v = variancia(x);
        return v == null ? null : Math.sqrt(v);
    }

    public static Double amplitude(List<Double> x) {
        if (x.isEmpty()) {
            return null;
        }
        return Collections.max(x) - Collections.min(x);
    }

    /** Coeficiente de variação em % (desvio / |média| * 100). */
    public static Double coeficienteVariacao(List<Double> x) {
        Double dp = desvioPadrao(x);
        Double m = media(x);
        if (dp == null || m == null || m == 0) {
            return null;
        }
        return dp / Math.abs(m) * 100.0;
    }

    // ---------------------------------------------------------------------
    // Associação: correlação e regressão
    // ---------------------------------------------------------------------

    /** Coeficiente de correlação de Pearson. */
    public static Double correlacao(List<Double> x, List<Double> y) {
        if (x.size() != y.size() || x.size() < 3) {
            return null;
        }
        double mx = media(x);
        double my = media(y);
        double sxy = 0;
        double sxx = 0;
        double syy = 0;
        for (int i = 0; i < x.size(); i++) {
            double dx = x.get(i) - mx;
            double dy = y.get(i) - my;
            sxy += dx * dy;
            sxx += dx * dx;
            syy += dy * dy;
        }
        if (sxx == 0 || syy == 0) {
            return null;
        }
        return sxy / Math.sqrt(sxx * syy);
    }

    /** Resultado da regressão linear simples y = intercepto + inclinacao * x. */
    public record Regressao(double inclinacao, double intercepto, double r, double r2) {
        public double prever(double x) {
            return intercepto + inclinacao * x;
        }
    }

    /** Regressão linear por mínimos quadrados; null se x for constante ou n &lt; 3. */
    public static Regressao regressaoLinear(List<Double> x, List<Double> y) {
        Double r = correlacao(x, y);
        if (r == null) {
            return null;
        }
        double mx = media(x);
        double my = media(y);
        double sxy = 0;
        double sxx = 0;
        for (int i = 0; i < x.size(); i++) {
            sxy += (x.get(i) - mx) * (y.get(i) - my);
            sxx += (x.get(i) - mx) * (x.get(i) - mx);
        }
        double b = sxy / sxx;
        return new Regressao(b, my - b * mx, r, r * r);
    }

    /** Média móvel simples de janela k (retorna n-k+1 valores). */
    public static List<Double> mediaMovel(List<Double> x, int k) {
        List<Double> r = new ArrayList<>();
        for (int i = 0; i + k <= x.size(); i++) {
            r.add(media(x.subList(i, i + k)));
        }
        return r;
    }

    // ---------------------------------------------------------------------
    // Inferência
    // ---------------------------------------------------------------------

    /** Valor crítico bilateral 95%: t de Student para n &lt;= 31, senão z. */
    public static double criticoIc95(int n) {
        int gl = n - 1;
        return gl >= 1 && gl <= 30 ? T_95[gl - 1] : Z_95;
    }

    /** Intervalo de confiança de 95% para a média: {inferior, superior}. */
    public static double[] icMedia95(List<Double> x) {
        Double dp = desvioPadrao(x);
        if (dp == null) {
            return null;
        }
        double m = media(x);
        double margem = criticoIc95(x.size()) * dp / Math.sqrt(x.size());
        return new double[] {m - margem, m + margem};
    }

    /** Intervalo de confiança de 95% (Wald) para uma proporção: {inferior, superior}. */
    public static double[] icProporcao95(long sucessos, long n) {
        if (n == 0) {
            return null;
        }
        double p = (double) sucessos / n;
        double margem = Z_95 * Math.sqrt(p * (1 - p) / n);
        return new double[] {Math.max(0, p - margem), Math.min(1, p + margem)};
    }

    /** Estatística z do teste de média: (média - mu0) / (dp / sqrt(n)). */
    public static Double estatisticaTeste(List<Double> x, double mu0) {
        Double dp = desvioPadrao(x);
        if (dp == null || dp == 0) {
            return null;
        }
        return (media(x) - mu0) / (dp / Math.sqrt(x.size()));
    }

    /** p-valor bilateral aproximado pela Normal para uma estatística de teste. */
    public static double pValorBilateral(double z) {
        return 2.0 * (1.0 - normalCdf(Math.abs(z)));
    }

    // ---------------------------------------------------------------------
    // Probabilidade
    // ---------------------------------------------------------------------

    /** P(A|B) = P(A ∩ B) / P(B), a partir de contagens. */
    public static Double probCondicional(long aeB, long b) {
        return b == 0 ? null : (double) aeB / b;
    }

    /** Teorema de Bayes: P(A|B) = P(B|A) P(A) / [P(B|A) P(A) + P(B|¬A) P(¬A)]. */
    public static Double bayes(double pBdadoA, double pA, double pBdadoNaoA) {
        double denom = pBdadoA * pA + pBdadoNaoA * (1 - pA);
        return denom == 0 ? null : pBdadoA * pA / denom;
    }

    /** P(X = k) para X ~ Poisson(lambda). */
    public static double poissonPmf(int k, double lambda) {
        if (k < 0) {
            return 0;
        }
        if (lambda <= 0) {
            return k == 0 ? 1 : 0;
        }
        return Math.exp(k * Math.log(lambda) - lambda - logFatorial(k));
    }

    /** P(X &lt;= k) para X ~ Poisson(lambda). */
    public static double poissonCdf(int k, double lambda) {
        if (lambda <= 0) {
            return k >= 0 ? 1 : 0;
        }
        double soma = 0;
        for (int i = 0; i <= k; i++) {
            soma += poissonPmf(i, lambda);
        }
        return Math.min(1.0, soma);
    }

    /** P(X &gt; k) para X ~ Poisson(lambda). */
    public static double poissonCauda(int k, double lambda) {
        return Math.max(0.0, 1.0 - poissonCdf(k, lambda));
    }

    /** P(X = k) para X ~ Binomial(n, p). */
    public static double binomialPmf(int k, int n, double p) {
        if (k < 0 || k > n) {
            return 0;
        }
        if (p <= 0) {
            return k == 0 ? 1 : 0;
        }
        if (p >= 1) {
            return k == n ? 1 : 0;
        }
        double logC = logFatorial(n) - logFatorial(k) - logFatorial(n - k);
        return Math.exp(logC + k * Math.log(p) + (n - k) * Math.log(1 - p));
    }

    /** P(X &gt;= k) para X ~ Binomial(n, p). */
    public static double binomialCaudaSuperior(int k, int n, double p) {
        double soma = 0;
        for (int i = Math.max(k, 0); i <= n; i++) {
            soma += binomialPmf(i, n, p);
        }
        return Math.min(1.0, soma);
    }

    /** Função de distribuição acumulada da Normal(mu, sigma). */
    public static double normalCdf(double x, double mu, double sigma) {
        return normalCdf((x - mu) / sigma);
    }

    /** CDF da Normal padrão (Abramowitz &amp; Stegun 7.1.26, erro &lt; 1,5e-7). */
    public static double normalCdf(double z) {
        double t = 1.0 / (1.0 + 0.3275911 * Math.abs(z) / Math.sqrt(2));
        double poly = ((((1.061405429 * t - 1.453152027) * t) + 1.421413741) * t - 0.284496736) * t + 0.254829592;
        double erf = 1.0 - poly * t * Math.exp(-z * z / 2.0);
        return z >= 0 ? 0.5 * (1.0 + erf) : 0.5 * (1.0 - erf);
    }

    // ---------------------------------------------------------------------
    // Auxiliares
    // ---------------------------------------------------------------------

    private static List<Double> ordenada(List<Double> x) {
        Double[] a = x.toArray(new Double[0]);
        Arrays.sort(a);
        return Arrays.asList(a);
    }

    private static double logFatorial(int n) {
        double s = 0;
        for (int i = 2; i <= n; i++) {
            s += Math.log(i);
        }
        return s;
    }
}
