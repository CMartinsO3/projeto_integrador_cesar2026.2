package com.hemoflow.hemoflow.grafo;

import java.util.*;

public class Grafo {
    private Map<String, List<Aresta>> adjacencias = new HashMap<>();

    public void adicionarNo(String no) {
        adjacencias.putIfAbsent(no, new ArrayList<>());
    }

    public void adicionarAresta(String origem, String destino, int peso) {
        adjacencias.computeIfAbsent(origem, k -> new ArrayList<>())
                   .add(new Aresta(destino, peso));
    }

    public List<Aresta> getVizinhos(String no) {
        return adjacencias.getOrDefault(no, new ArrayList<>());
    }

    // Algoritmo de caminho mínimo (Dijkstra) será implementado na Semana 5-7
}
