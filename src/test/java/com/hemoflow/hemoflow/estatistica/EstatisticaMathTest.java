package com.hemoflow.hemoflow.estatistica;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class EstatisticaMathTest {

    private static final List<Double> X = List.of(2.0, 4.0, 4.0, 4.0, 5.0, 5.0, 7.0, 9.0);

    @Test
    void medidasDePosicao() {
        assertEquals(5.0, EstatisticaMath.media(X), 1e-9);
        assertEquals(4.5, EstatisticaMath.mediana(X), 1e-9);
        assertEquals(4.0, EstatisticaMath.moda(X), 1e-9);
        assertEquals(4.0, EstatisticaMath.percentil(X, 25), 1e-9);
        assertEquals(5.5, EstatisticaMath.percentil(X, 75), 1e-9);
        assertNull(EstatisticaMath.moda(List.of(1.0, 2.0, 3.0)));
        assertNull(EstatisticaMath.media(List.of()));
    }

    @Test
    void medidasDeDispersao() {
        // soma dos desvios² = 32 → variância amostral 32/7
        assertEquals(32.0 / 7.0, EstatisticaMath.variancia(X), 1e-9);
        assertEquals(Math.sqrt(32.0 / 7.0), EstatisticaMath.desvioPadrao(X), 1e-9);
        assertEquals(7.0, EstatisticaMath.amplitude(X), 1e-9);
        assertEquals(Math.sqrt(32.0 / 7.0) / 5.0 * 100, EstatisticaMath.coeficienteVariacao(X), 1e-9);
        assertNull(EstatisticaMath.variancia(List.of(1.0)));
    }

    @Test
    void correlacaoERegressao() {
        List<Double> x = List.of(1.0, 2.0, 3.0, 4.0, 5.0);
        List<Double> y = List.of(2.0, 4.0, 6.0, 8.0, 10.0);
        assertEquals(1.0, EstatisticaMath.correlacao(x, y), 1e-9);
        EstatisticaMath.Regressao r = EstatisticaMath.regressaoLinear(x, y);
        assertEquals(2.0, r.inclinacao(), 1e-9);
        assertEquals(0.0, r.intercepto(), 1e-9);
        assertEquals(12.0, r.prever(6), 1e-9);
        assertEquals(-1.0, EstatisticaMath.correlacao(x, List.of(10.0, 8.0, 6.0, 4.0, 2.0)), 1e-9);
        assertNull(EstatisticaMath.correlacao(x, List.of(3.0, 3.0, 3.0, 3.0, 3.0)));
    }

    @Test
    void mediaMovel() {
        assertEquals(List.of(2.0, 3.0, 4.0), EstatisticaMath.mediaMovel(List.of(1.0, 2.0, 3.0, 4.0, 5.0), 3));
    }

    @Test
    void intervaloDeConfiancaETeste() {
        // n=8, gl=7 → t=2.365; margem = 2.365 * dp / sqrt(8)
        double dp = Math.sqrt(32.0 / 7.0);
        double margem = 2.365 * dp / Math.sqrt(8);
        double[] ic = EstatisticaMath.icMedia95(X);
        assertEquals(5.0 - margem, ic[0], 1e-9);
        assertEquals(5.0 + margem, ic[1], 1e-9);

        double z = EstatisticaMath.estatisticaTeste(X, 4.0);
        assertEquals(1.0 / (dp / Math.sqrt(8)), z, 1e-9);
        assertEquals(0.05, EstatisticaMath.pValorBilateral(1.959964), 1e-4);

        double[] icp = EstatisticaMath.icProporcao95(50, 100);
        assertEquals(0.5 - 1.959964 * 0.05, icp[0], 1e-6);
    }

    @Test
    void normal() {
        assertEquals(0.5, EstatisticaMath.normalCdf(0), 1e-6);
        assertEquals(0.8413447, EstatisticaMath.normalCdf(1), 1e-5);
        assertEquals(0.9750021, EstatisticaMath.normalCdf(1.959964), 1e-5);
        assertEquals(0.1586553, EstatisticaMath.normalCdf(-1), 1e-5);
        assertEquals(0.8413447, EstatisticaMath.normalCdf(12, 10, 2), 1e-5);
    }

    @Test
    void poisson() {
        // λ=2: P(0)=e^-2, P(1)=2e^-2, P(2)=2e^-2
        assertEquals(Math.exp(-2), EstatisticaMath.poissonPmf(0, 2), 1e-12);
        assertEquals(2 * Math.exp(-2), EstatisticaMath.poissonPmf(2, 2), 1e-12);
        assertEquals(5 * Math.exp(-2), EstatisticaMath.poissonCdf(2, 2), 1e-12);
        assertEquals(1 - 5 * Math.exp(-2), EstatisticaMath.poissonCauda(2, 2), 1e-12);
        assertEquals(1.0, EstatisticaMath.poissonPmf(0, 0), 0);
        assertEquals(0.0, EstatisticaMath.poissonCauda(0, 0), 0);
    }

    @Test
    void binomial() {
        assertEquals(0.375, EstatisticaMath.binomialPmf(2, 4, 0.5), 1e-12);
        assertEquals(5.0 / 16.0, EstatisticaMath.binomialCaudaSuperior(3, 4, 0.5), 1e-12);
        assertEquals(1.0, EstatisticaMath.binomialCaudaSuperior(0, 10, 0.3), 1e-9);
    }

    @Test
    void condicionalEBayes() {
        assertEquals(0.25, EstatisticaMath.probCondicional(1, 4), 1e-12);
        assertNull(EstatisticaMath.probCondicional(1, 0));
        // Exemplo clássico: P(A)=1%, P(B|A)=90%, P(B|¬A)=9% → ≈ 0.0917
        assertEquals(0.009 / (0.009 + 0.0891), EstatisticaMath.bayes(0.9, 0.01, 0.09), 1e-9);
        assertNotNull(EstatisticaMath.bayes(1, 0.5, 0));
        assertTrue(EstatisticaMath.bayes(1, 0.5, 0) == 1.0);
    }
}

