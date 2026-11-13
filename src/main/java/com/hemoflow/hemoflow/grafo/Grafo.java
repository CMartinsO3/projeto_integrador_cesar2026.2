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

import com.hemoflow.hemoflow.algoritmos.RoteadorVizinhoMaisProximo;

/** Grafo direcionado ponderado por tempo de deslocamento (minutos). */
public class Grafo {
    private final Map<String, List<Aresta>> adjacencias = new HashMap<>();

    public void adicionarNo(String no) {
        adjacencias.putIfAbsent(no, new ArrayList<>());
    }

    public void adicionarAresta(String origem, String destino, int peso) {
        adicionarAresta(origem, destino, peso, null);
    }

    public void adicionarAresta(String origem, String destino, int peso, Double distanciaKm) {
        if (peso < 0) {
            throw new IllegalArgumentException("O peso da aresta não pode ser negativo");
        }
        if (distanciaKm != null && (!Double.isFinite(distanciaKm) || distanciaKm < 0)) {
            throw new IllegalArgumentException("A distância da aresta deve ser finita e não negativa");
        }
        adicionarNo(origem);
        adicionarNo(destino);
        adjacencias.get(origem).add(new Aresta(destino, peso, distanciaKm));
    }

    public List<Aresta> getVizinhos(String no) {
        return List.copyOf(adjacencias.getOrDefault(no, List.of()));
    }

    /** Constrói uma rota gulosa escolhendo o vizinho de menor tempo em cada passo. */
    public RoteadorVizinhoMaisProximo.Resultado calcularVizinhoMaisProximo(String origem, String destino) {
        Map<String, List<RoteadorVizinhoMaisProximo.Aresta>> copia = new HashMap<>();
        for (Map.Entry<String, List<Aresta>> entrada : adjacencias.entrySet()) {
            List<RoteadorVizinhoMaisProximo.Aresta> arestas = entrada.getValue().stream()
                    .map(a -> new RoteadorVizinhoMaisProximo.Aresta(a.getDestino(), a.getPeso()))
                    .toList();
            copia.put(entrada.getKey(), arestas);
        }
        return RoteadorVizinhoMaisProximo.calcular(copia, origem, destino);
    }

    public record ResultadoDijkstra(
            List<String> rota,
            int tempoTotalMinutos,
            double distanciaTotalKm,
            boolean encontrado
    ) {}

    /**
     * Calcula o caminho mínimo usando o algoritmo de Dijkstra com fila de prioridade.
     */
    public ResultadoDijkstra calcularDijkstra(String origem, String destino) {
        if (!adjacencias.containsKey(origem) || !adjacencias.containsKey(destino)) {
            return new ResultadoDijkstra(List.of(), 0, 0.0, false);
        }
        if (origem.equals(destino)) {
            return new ResultadoDijkstra(List.of(origem), 0, 0.0, true);
        }

        Map<String, Integer> distancias = new HashMap<>();
        Map<String, String> predecessores = new HashMap<>();
        Set<String> visitados = new HashSet<>();

        for (String no : adjacencias.keySet()) {
            distancias.put(no, Integer.MAX_VALUE);
        }
        distancias.put(origem, 0);

        PriorityQueue<PassoDijkstra> fila = new PriorityQueue<>(
                Comparator.comparingInt(PassoDijkstra::distanciaAcumulada)
                        .thenComparing(PassoDijkstra::noId));
        fila.add(new PassoDijkstra(origem, 0));

        while (!fila.isEmpty()) {
            PassoDijkstra atual = fila.poll();
            String u = atual.noId();

            if (!visitados.add(u)) {
                continue;
            }
            if (u.equals(destino)) {
                break;
            }

            int distU = distancias.getOrDefault(u, Integer.MAX_VALUE);
            if (distU == Integer.MAX_VALUE) {
                continue;
            }

            for (Aresta aresta : getVizinhos(u)) {
                String v = aresta.getDestino();
                int peso = aresta.getPeso();
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

        int custoFinal = distancias.getOrDefault(destino, Integer.MAX_VALUE);
        if (custoFinal == Integer.MAX_VALUE) {
            return new ResultadoDijkstra(List.of(), 0, 0.0, false);
        }

        List<String> caminho = new ArrayList<>();
        String passo = destino;
        while (passo != null) {
            caminho.add(passo);
            passo = predecessores.get(passo);
        }
        Collections.reverse(caminho);

        double distanciaTotalKm = 0.0;
        for (int i = 0; i < caminho.size() - 1; i++) {
            String de = caminho.get(i);
            String para = caminho.get(i + 1);
            for (Aresta a : getVizinhos(de)) {
                if (a.getDestino().equals(para)) {
                    if (a.getDistanciaKm() != null) {
                        distanciaTotalKm += a.getDistanciaKm();
                    }
                    break;
                }
            }
        }

        return new ResultadoDijkstra(caminho, custoFinal, distanciaTotalKm, true);
    }

    private record PassoDijkstra(String noId, int distanciaAcumulada) {}
}
