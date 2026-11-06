package com.hemoflow.hemoflow.grafo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
}
