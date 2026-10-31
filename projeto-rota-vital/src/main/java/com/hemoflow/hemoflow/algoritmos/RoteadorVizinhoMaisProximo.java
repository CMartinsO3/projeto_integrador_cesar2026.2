package com.hemoflow.hemoflow.algoritmos;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Constrói uma rota gulosa, escolhendo a aresta de menor tempo em cada nó. */
public final class RoteadorVizinhoMaisProximo {
    private RoteadorVizinhoMaisProximo() {
    }

    public record Aresta(String destino, int peso) {
    }

    public record Resultado(List<String> rota, long custoTotal, boolean encontrado) {
    }

    public static Resultado calcular(Map<String, List<Aresta>> grafo, String origem, String destino) {
        if (!grafo.containsKey(origem) || !grafo.containsKey(destino)) {
            return new Resultado(List.of(), 0, false);
        }

        List<String> rota = new ArrayList<>();
        Set<String> visitados = new HashSet<>();
        String atual = origem;
        long custoTotal = 0;
        rota.add(atual);

        while (!atual.equals(destino)) {
            visitados.add(atual);
            Aresta proximaAresta = grafo.getOrDefault(atual, List.of()).stream()
                    .filter(aresta -> aresta.peso() >= 0)
                    .filter(aresta -> grafo.containsKey(aresta.destino()))
                    .filter(aresta -> !visitados.contains(aresta.destino()))
                    .min(Comparator.comparingInt(Aresta::peso).thenComparing(Aresta::destino))
                    .orElse(null);

            if (proximaAresta == null || custoTotal > Long.MAX_VALUE - proximaAresta.peso()) {
                return new Resultado(List.copyOf(rota), custoTotal, false);
            }

            atual = proximaAresta.destino();
            custoTotal += proximaAresta.peso();
            rota.add(atual);
        }

        return new Resultado(List.copyOf(rota), custoTotal, true);
    }
}
