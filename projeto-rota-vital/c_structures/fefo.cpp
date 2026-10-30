#include "fefo.hpp"
#include <stdio.h>
#include <stdlib.h>

void fefo_inicializar(ListaFefo* l) {
    if (l) {
        l->cabeca = NULL;
        l->tamanho = 0;
    }
}

/* Retorna <0 se (id_a, val_a) deve sair antes de (id_b, val_b), 0 se empatam por completo. */
static int comparar(int validade_a, int id_a, int validade_b, int id_b) {
    if (validade_a != validade_b) return validade_a < validade_b ? -1 : 1;
    if (id_a != id_b) return id_a < id_b ? -1 : 1;
    return 0;
}

int fefo_inserir_ordenado(ListaFefo* l, int bolsa_id, int validade, int hoje) {
    if (!l) return 0;
    if (validade < hoje) return 0;                  /* bolsa vencida: descarta */

    NoFefo* novo = (NoFefo*)malloc(sizeof(NoFefo));
    if (!novo) {
        fprintf(stderr, "Erro de alocacao de memoria!\n");
        return 0;
    }
    novo->bolsa_id = bolsa_id;
    novo->validade = validade;

    /* p aponta para o "ponteiro que deve receber o novo nó" (cabeça ou proximo de alguém).
     * Anda enquanto o nó atual sai antes OU empata: assim, em empate total,
     * o novo entra depois (ordem de chegada). */
    NoFefo** p = &l->cabeca;
    while (*p && comparar((*p)->validade, (*p)->bolsa_id, validade, bolsa_id) <= 0) {
        p = &(*p)->proximo;
    }
    novo->proximo = *p;
    *p = novo;
    l->tamanho++;
    return 1;
}

int fefo_remover_primeira(ListaFefo* l, int hoje) {
    if (!l) return -1;

    /* Bolsas que venceram dentro da lista estão sempre na frente (lista ordenada). */
    while (l->cabeca && l->cabeca->validade < hoje) {
        NoFefo* vencida = l->cabeca;
        l->cabeca = vencida->proximo;
        free(vencida);
        l->tamanho--;
    }
    if (!l->cabeca) return -1;

    NoFefo* remover = l->cabeca;
    int id = remover->bolsa_id;
    l->cabeca = remover->proximo;
    free(remover);
    l->tamanho--;
    return id;
}

void fefo_liberar(ListaFefo* l) {
    if (!l) return;
    NoFefo* atual = l->cabeca;
    while (atual) {
        NoFefo* proximo = atual->proximo;
        free(atual);
        atual = proximo;
    }
    l->cabeca = NULL;
    l->tamanho = 0;
}

void fefo_imprimir(const ListaFefo* l) {
    if (!l) return;
    printf("Lista FEFO (tam: %lu): [", (unsigned long)l->tamanho);
    for (const NoFefo* a = l->cabeca; a; a = a->proximo) {
        printf("%d@%d", a->bolsa_id, a->validade);
        if (a->proximo) printf(" -> ");
    }
    printf("]\n");
}
