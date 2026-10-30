#include "roteador_vizinho.hpp"
#include <limits>
#include <unordered_map>
#include <unordered_set>

ResultadoRota vizinho_mais_proximo(
    const std::vector<std::string>& nos,
    const std::vector<std::vector<ArestaRota>>& adj,
    const std::string& origem,
    const std::string& destino) {

    ResultadoRota resultado{{}, 0, false};

    if (adj.size() != nos.size()) {
        return resultado;
    }

    std::unordered_map<std::string, int> indices;
    for (size_t i = 0; i < nos.size(); i++) {
        indices.emplace(nos[i], static_cast<int>(i));
    }
    if (indices.find(origem) == indices.end() || indices.find(destino) == indices.end()) {
        return resultado;
    }

    const std::int64_t maxCusto = std::numeric_limits<std::int64_t>::max();
    std::string atual = origem;
    std::unordered_set<std::string> visitados;
    resultado.rota.push_back(atual);

    while (atual != destino) {
        visitados.insert(atual);
        const int indiceAtual = indices.at(atual);
        const ArestaRota* proxima = nullptr;

        for (const auto& aresta : adj[indiceAtual]) {
            if (aresta.peso < 0 || indices.find(aresta.destino) == indices.end()
                    || visitados.find(aresta.destino) != visitados.end()) {
                continue;
            }
            if (proxima == nullptr || aresta.peso < proxima->peso
                    || (aresta.peso == proxima->peso && aresta.destino < proxima->destino)) {
                proxima = &aresta;
            }
        }

        if (proxima == nullptr || resultado.custo > maxCusto - proxima->peso) {
            return resultado;
        }
        atual = proxima->destino;
        resultado.custo += proxima->peso;
        resultado.rota.push_back(atual);
    }

    resultado.encontrado = true;
    return resultado;
}
