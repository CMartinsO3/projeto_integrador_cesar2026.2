#include "busca_ordenacao.hpp"
#include <vector>

static void merge(int* v, int l, int m, int r) {
    std::vector<int> esquerda(v + l, v + m + 1);
    std::vector<int> direita(v + m + 1, v + r + 1);
    int i = 0;
    int j = 0;
    int k = l;

    while (i < static_cast<int>(esquerda.size()) &&
           j < static_cast<int>(direita.size())) {
        if (esquerda[i] <= direita[j]) {
            v[k++] = esquerda[i++];
        } else {
            v[k++] = direita[j++];
        }
    }

    while (i < static_cast<int>(esquerda.size())) {
        v[k++] = esquerda[i++];
    }
    while (j < static_cast<int>(direita.size())) {
        v[k++] = direita[j++];
    }
}

static void merge_rec(int* v, int l, int r) {
    if (l >= r) return;
    int m = l + (r - l) / 2;
    merge_rec(v, l, m);
    merge_rec(v, m + 1, r);
    merge(v, l, m, r);
}

int busca_sequencial(const int* v, int n, int chave) {
    for (int i = 0; i < n; i++) {
        if (v[i] == chave) return i;
    }
    return -1;
}

int busca_binaria(const int* v, int n, int chave) {
    int esquerda = 0;
    int direita = n - 1;
    while (esquerda <= direita) {
        int meio = esquerda + (direita - esquerda) / 2;
        if (v[meio] == chave) return meio;
        if (v[meio] < chave) esquerda = meio + 1;
        else direita = meio - 1;
    }
    return -1;
}

void bubble_sort(int* v, int n) {
    for (int i = 0; i < n - 1; i++) {
        bool trocou = false;
        for (int j = 0; j < n - i - 1; j++) {
            if (v[j] > v[j + 1]) {
                int temp = v[j];
                v[j] = v[j + 1];
                v[j + 1] = temp;
                trocou = true;
            }
        }
        if (!trocou) break;
    }
}

void selection_sort(int* v, int n) {
    for (int i = 0; i < n - 1; i++) {
        int menor = i;
        for (int j = i + 1; j < n; j++) {
            if (v[j] < v[menor]) menor = j;
        }
        int temp = v[i];
        v[i] = v[menor];
        v[menor] = temp;
    }
}

void insertion_sort(int* v, int n) {
    for (int i = 1; i < n; i++) {
        int chave = v[i];
        int j = i - 1;
        while (j >= 0 && v[j] > chave) {
            v[j + 1] = v[j];
            j--;
        }
        v[j + 1] = chave;
    }
}

void merge_sort(int* v, int n) {
    if (n > 1) merge_rec(v, 0, n - 1);
}
