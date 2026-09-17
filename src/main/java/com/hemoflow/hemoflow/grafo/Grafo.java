package com.hemoflow.hemoflow.grafo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;

public class Grafo {
    private final Map<String, List<Aresta>> adjacencias = new HashMap<>();

    public void adicionarNo(String no) {
        adjacencias.putIfAbsent(no, new ArrayList<>());
    }

    public void adicionarAresta(String origem, String destino, int peso) {
        adicionarAresta(origem, destino, peso, true);
    }

    public void adicionarAresta(String origem, String destino, int peso, boolean bidirecional) {
        adjacencias.computeIfAbsent(origem, k -> new ArrayList<>()).add(new Aresta(destino, peso));
        adjacencias.putIfAbsent(destino, new ArrayList<>());
        if (bidirecional) {
            adjacencias.computeIfAbsent(destino, k -> new ArrayList<>()).add(new Aresta(origem, peso));
        }
    }

    public List<Aresta> getVizinhos(String no) {
        return adjacencias.getOrDefault(no, List.of());
    }

    public Map<String, List<Aresta>> getAdjacencias() {
        return Collections.unmodifiableMap(adjacencias);
    }

    public ResultadoCaminho caminhoMinimo(String origem, String destino) {
        if (!adjacencias.containsKey(origem) || !adjacencias.containsKey(destino)) {
            return ResultadoCaminho.inexistente();
        }

        Map<String, Integer> distancias = new HashMap<>();
        Map<String, String> anterior = new HashMap<>();
        for (String no : adjacencias.keySet()) {
            distancias.put(no, Integer.MAX_VALUE);
        }
        distancias.put(origem, 0);

        PriorityQueue<Passo> fila = new PriorityQueue<>();
        fila.add(new Passo(origem, 0));

        while (!fila.isEmpty()) {
            Passo atual = fila.poll();
            if (atual.distancia() > distancias.getOrDefault(atual.no(), Integer.MAX_VALUE)) {
                continue;
            }
            if (Objects.equals(atual.no(), destino)) {
                break;
            }
            for (Aresta aresta : getVizinhos(atual.no())) {
                int candidato = atual.distancia() + aresta.getPeso();
                if (candidato < distancias.getOrDefault(aresta.getDestino(), Integer.MAX_VALUE)) {
                    distancias.put(aresta.getDestino(), candidato);
                    anterior.put(aresta.getDestino(), atual.no());
                    fila.add(new Passo(aresta.getDestino(), candidato));
                }
            }
        }

        if (distancias.get(destino) == Integer.MAX_VALUE) {
            return ResultadoCaminho.inexistente();
        }

        List<String> caminho = new ArrayList<>();
        String cursor = destino;
        while (cursor != null) {
            caminho.add(cursor);
            cursor = anterior.get(cursor);
        }
        Collections.reverse(caminho);
        return new ResultadoCaminho(true, caminho, distancias.get(destino));
    }

    private record Passo(String no, int distancia) implements Comparable<Passo> {
        @Override
        public int compareTo(Passo outro) {
            return Integer.compare(distancia, outro.distancia);
        }
    }
}
