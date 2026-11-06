#include "roteador_vizinho.hpp"
#include <cassert>
#include <limits>
#include <vector>
#include <string>

int main() {
    std::vector<std::string> nos = {"H", "X", "C", "Z"};
    std::vector<std::vector<ArestaRota>> adj(4);
    adj[0] = {{"C", 4}, {"X", 2}};
    adj[1] = {{"C", 3}};

    auto rota = vizinho_mais_proximo(nos, adj, "H", "C");
    assert(rota.encontrado);
    assert(rota.custo == 5);
    assert((rota.rota == std::vector<std::string>{"H", "X", "C"}));

    auto mesmo_no = vizinho_mais_proximo(nos, adj, "H", "H");
    assert(mesmo_no.encontrado && mesmo_no.custo == 0);
    assert((mesmo_no.rota == std::vector<std::string>{"H"}));

    std::vector<std::string> nos_empate = {"A", "Z", "B"};
    std::vector<std::vector<ArestaRota>> adj_empate(3);
    adj_empate[0] = {{"Z", 5}, {"B", 5}};
    auto desempate = vizinho_mais_proximo(nos_empate, adj_empate, "A", "B");
    assert(desempate.encontrado && desempate.custo == 5);
    assert((desempate.rota == std::vector<std::string>{"A", "B"}));

    auto sem_rota = vizinho_mais_proximo(nos, adj, "H", "Z");
    assert(!sem_rota.encontrado);

    std::vector<std::string> nos_caminhos = {"A", "B", "C", "D"};
    std::vector<std::vector<ArestaRota>> adj_caminhos(4);
    adj_caminhos[0] = {{"B", 1}, {"C", 10}};
    adj_caminhos[1] = {{"D", 100}};
    adj_caminhos[2] = {{"D", 10}};

    auto vizinho_guloso = vizinho_mais_proximo(nos_caminhos, adj_caminhos, "A", "D");
    assert(vizinho_guloso.encontrado);
    assert(vizinho_guloso.custo == 101);
    assert((vizinho_guloso.rota == std::vector<std::string>{"A", "B", "D"}));

    std::vector<std::string> nos_sem_saida = {"A", "B", "C", "D"};
    std::vector<std::vector<ArestaRota>> adj_sem_saida(4);
    adj_sem_saida[0] = {{"B", 1}, {"C", 10}};
    adj_sem_saida[2] = {{"D", 10}};
    auto sem_caminho_guloso = vizinho_mais_proximo(nos_sem_saida, adj_sem_saida, "A", "D");
    assert(!sem_caminho_guloso.encontrado);
    assert(sem_caminho_guloso.custo == 1);
    assert((sem_caminho_guloso.rota == std::vector<std::string>{"A", "B"}));

    std::vector<std::string> nos_custo_longo = {"A", "B", "C"};
    std::vector<std::vector<ArestaRota>> adj_custo_longo(3);
    adj_custo_longo[0] = {{"B", std::numeric_limits<int>::max()}};
    adj_custo_longo[1] = {{"C", std::numeric_limits<int>::max()}};
    auto custo_longo = vizinho_mais_proximo(nos_custo_longo, adj_custo_longo, "A", "C");
    assert(custo_longo.encontrado);
    assert(custo_longo.custo == 2LL * std::numeric_limits<int>::max());

    auto origem_inexistente = vizinho_mais_proximo(nos, adj, "Q", "C");
    assert(!origem_inexistente.encontrado);

    return 0;
}
