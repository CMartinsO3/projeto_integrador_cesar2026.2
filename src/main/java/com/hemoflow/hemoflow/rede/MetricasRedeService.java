package com.hemoflow.hemoflow.rede;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

import org.springframework.stereotype.Service;

import com.hemoflow.hemoflow.api.dto.RedePainelDTOs;

@Service
public class MetricasRedeService {

    private final ConcurrentHashMap<String, MetricasRota> metricasPorRota = new ConcurrentHashMap<>();

    public void registrar(String metodo, String rota, long duracaoMs, int statusCode) {
        String chave = metodo + " " + rota;
        metricasPorRota.computeIfAbsent(chave, k -> new MetricasRota(metodo, rota))
                .registrar(duracaoMs, statusCode);
    }

    public List<RedePainelDTOs.EstatisticaRotaDTO> obterEstatisticas() {
        return metricasPorRota.values().stream()
                .map(MetricasRota::toDTO)
                .sorted(Comparator.comparing(RedePainelDTOs.EstatisticaRotaDTO::rota)
                        .thenComparing(RedePainelDTOs.EstatisticaRotaDTO::metodo))
                .toList();
    }

    public void limpar() {
        metricasPorRota.clear();
    }

    public static class MetricasRota {
        private static final int MAX_AMOSTRAS = 1000;

        private final String metodo;
        private final String rota;
        private final LongAdder totalRequisicoes = new LongAdder();
        private final LongAdder tempoTotalMs = new LongAdder();
        private final AtomicLong tempoMaximoMs = new AtomicLong(0);
        private final ConcurrentHashMap<Integer, LongAdder> contagemPorStatus = new ConcurrentHashMap<>();

        private final long[] amostrasLatencia = new long[MAX_AMOSTRAS];
        private final AtomicInteger contadorAmostras = new AtomicInteger(0);
        private final Object lockAmostras = new Object();

        public MetricasRota(String metodo, String rota) {
            this.metodo = metodo;
            this.rota = rota;
        }

        public void registrar(long duracaoMs, int statusCode) {
            totalRequisicoes.increment();
            tempoTotalMs.add(duracaoMs);
            tempoMaximoMs.accumulateAndGet(duracaoMs, Math::max);
            contagemPorStatus.computeIfAbsent(statusCode, k -> new LongAdder()).increment();

            synchronized (lockAmostras) {
                int pos = contadorAmostras.getAndIncrement() % MAX_AMOSTRAS;
                amostrasLatencia[pos] = duracaoMs;
            }
        }

        public RedePainelDTOs.EstatisticaRotaDTO toDTO() {
            long total = totalRequisicoes.sum();
            double media = total > 0 ? (double) tempoTotalMs.sum() / total : 0.0;
            long maximo = tempoMaximoMs.get();
            double p95 = calcularP95(total);

            Map<Integer, Long> statusMap = new HashMap<>();
            contagemPorStatus.forEach((codigo, contador) -> statusMap.put(codigo, contador.sum()));

            return new RedePainelDTOs.EstatisticaRotaDTO(
                    metodo,
                    rota,
                    total,
                    Math.round(media * 100.0) / 100.0,
                    maximo,
                    Math.round(p95 * 100.0) / 100.0,
                    statusMap
            );
        }

        private double calcularP95(long total) {
            if (total == 0) {
                return 0.0;
            }
            long[] copia;
            synchronized (lockAmostras) {
                int tamanho = (int) Math.min(total, MAX_AMOSTRAS);
                copia = Arrays.copyOf(amostrasLatencia, tamanho);
            }
            if (copia.length == 0) {
                return 0.0;
            }
            Arrays.sort(copia);
            int idx = (int) Math.ceil(copia.length * 0.95) - 1;
            if (idx < 0) {
                idx = 0;
            }
            if (idx >= copia.length) {
                idx = copia.length - 1;
            }
            return copia[idx];
        }
    }
}

