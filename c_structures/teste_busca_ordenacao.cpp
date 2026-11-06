#include "busca_ordenacao.hpp"
#include <algorithm>
#include <cassert>
#include <iostream>
#include <vector>

using Ordenacao = void (*)(int*, int);

static void verificar_ordenacao(Ordenacao ordenar, const std::vector<int>& entrada) {
    std::vector<int> esperado = entrada;
    std::sort(esperado.begin(), esperado.end());

    std::vector<int> resultado = entrada;
    ordenar(resultado.empty() ? nullptr : resultado.data(),
            static_cast<int>(resultado.size()));
    assert(resultado == esperado);
}

int main() {
    Ordenacao ordenacoes[] = {
        bubble_sort, selection_sort, insertion_sort, merge_sort
    };

    const std::vector<std::vector<int>> casos = {
        {9, 3, 7, 1, 5, 2, 8, 6, 4},
        {5, 2, 5, -1, 0, 2, -8, 5},
        {},
        {42},
        {-3, -1, 0, 4, 9},
        {9, 4, 0, -1, -3}
    };

    for (Ordenacao ordenar : ordenacoes) {
        for (const auto& caso : casos) {
            verificar_ordenacao(ordenar, caso);
        }
    }

    const int ordenado[] = {1, 3, 5, 7, 9};
    const int repetidos[] = {1, 3, 3, 3, 9};
    assert(busca_sequencial(ordenado, 5, 7) == 3);
    assert(busca_sequencial(ordenado, 5, 2) == -1);
    assert(busca_sequencial(repetidos, 5, 3) == 1);
    assert(busca_sequencial(nullptr, 0, 3) == -1);
    assert(busca_sequencial(ordenado, 1, 1) == 0);

    assert(busca_binaria(ordenado, 5, 7) == 3);
    assert(busca_binaria(ordenado, 5, 2) == -1);
    int indice_repetido = busca_binaria(repetidos, 5, 3);
    assert(indice_repetido >= 1 && indice_repetido <= 3);
    assert(busca_binaria(nullptr, 0, 3) == -1);
    assert(busca_binaria(ordenado, 1, 1) == 0);

    std::cout << "Buscas e ordenacoes: OK\n";
    return 0;
}
