#ifndef BUSCA_ORDENACAO_HPP
#define BUSCA_ORDENACAO_HPP
#include <cstddef>
int busca_sequencial(const int* v, int n, int chave);
int busca_binaria(const int* v, int n, int chave); // vetor deve estar crescente
void bubble_sort(int* v, int n);
void selection_sort(int* v, int n);
void insertion_sort(int* v, int n);
void merge_sort(int* v, int n);
#endif
