package com.hemoflow.hemoflow.grafo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

public class Grafo {

    private final Map<Long, List<Aresta>> adjacencias = new HashMap<>();

    public void adicionarVertice(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("O ID do vértice não pode ser nulo.");
        }
        adjacencias.putIfAbsent(id, new ArrayList<>());
    }

    public void adicionarAresta(Long origemId, Long destinoId, int pesoMinutos) {
        if (pesoMinutos <= 0) {
            throw new IllegalArgumentException("O peso da aresta em minutos deve ser positivo.");
        }
        adicionarVertice(origemId);
        adicionarVertice(destinoId);
        adjacencias.get(origemId).add(new Aresta(destinoId, pesoMinutos));
        adjacencias.get(destinoId).add(new Aresta(origemId, pesoMinutos));
    }

    public void adicionarArestaNaoDirecionada(Long origemId, Long destinoId, int pesoMinutos) {
        adicionarAresta(origemId, destinoId, pesoMinutos);
    }

    public boolean contemVertice(Long id) {
        return adjacencias.containsKey(id);
    }

    public List<Aresta> vizinhosDe(Long id) {
        return adjacencias.getOrDefault(id, Collections.emptyList());
    }

    public List<Aresta> obterVizinhos(Long id) {
        return vizinhosDe(id);
    }

    public Map<Long, List<Aresta>> getAdjacencias() {
        return Collections.unmodifiableMap(adjacencias);
    }

    public ResultadoCaminhoMinimo calcularCaminhoMinimo(Long origemId, Long destinoId) {
        if (!contemVertice(origemId) || !contemVertice(destinoId)) {
            return new ResultadoCaminhoMinimo(Collections.emptyList(), 0, false);
        }

        if (origemId.equals(destinoId)) {
            return new ResultadoCaminhoMinimo(List.of(origemId), 0, true);
        }

        Map<Long, Integer> distancias = new HashMap<>();
        Map<Long, Long> predecessores = new HashMap<>();
        Set<Long> visitados = new HashSet<>();

        for (Long vertice : adjacencias.keySet()) {
            distancias.put(vertice, Integer.MAX_VALUE);
        }
        distancias.put(origemId, 0);

        PriorityQueue<PassoDijkstra> fila = new PriorityQueue<>(
                Comparator.comparingInt(PassoDijkstra::distanciaAcumulada)
                        .thenComparingLong(PassoDijkstra::verticeId));
        fila.add(new PassoDijkstra(origemId, 0));

        while (!fila.isEmpty()) {
            PassoDijkstra atual = fila.poll();
            Long u = atual.verticeId();

            if (!visitados.add(u)) {
                continue;
            }
            if (u.equals(destinoId)) {
                break;
            }

            int distU = distancias.getOrDefault(u, Integer.MAX_VALUE);
            if (distU == Integer.MAX_VALUE) {
                continue;
            }

            for (Aresta aresta : vizinhosDe(u)) {
                Long v = aresta.getDestinoId();
                int peso = aresta.getPesoMinutos();
                if (visitados.contains(v)) {
                    continue;
                }
                int novaDist = distU + peso;
                if (novaDist < distancias.getOrDefault(v, Integer.MAX_VALUE)) {
                    distancias.put(v, novaDist);
                    predecessores.put(v, u);
                    fila.add(new PassoDijkstra(v, novaDist));
                }
            }
        }

        int custoFinal = distancias.getOrDefault(destinoId, Integer.MAX_VALUE);
        if (custoFinal == Integer.MAX_VALUE) {
            return new ResultadoCaminhoMinimo(Collections.emptyList(), 0, false);
        }

        List<Long> caminho = new ArrayList<>();
        Long passo = destinoId;
        while (passo != null) {
            caminho.add(passo);
            passo = predecessores.get(passo);
        }
        Collections.reverse(caminho);

        return new ResultadoCaminhoMinimo(caminho, custoFinal, true);
    }

    private record PassoDijkstra(Long verticeId, int distanciaAcumulada) {}

    public record ResultadoCaminhoMinimo(List<Long> caminho, int custoTotalMinutos, boolean existeRota) {}
}