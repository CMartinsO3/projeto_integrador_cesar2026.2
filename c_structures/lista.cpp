#include "lista.hpp"
#include <stdio.h>
#include <stdlib.h>

void lista_inicializar(Lista* l) {
    if (l) {
        l->cabeca = NULL;
        l->tamanho = 0;
    }
}

void lista_inserir_inicio(Lista* l, int bolsa_id) {
    if (!l) return;
    
    NoLista* novo = (NoLista*)malloc(sizeof(NoLista));
    if (!novo) {
        fprintf(stderr, "Erro de alocação de memória!\n");
        return;
    }
    novo->bolsa_id = bolsa_id;
    novo->proximo = l->cabeca;
    
    l->cabeca = novo;
    l->tamanho++;
}

void lista_inserir_fim(Lista* l, int bolsa_id) {
    if (!l) return;
    
    NoLista* novo = (NoLista*)malloc(sizeof(NoLista));
    if (!novo) {
        fprintf(stderr, "Erro de alocação de memória!\n");
        return;
    }
    novo->bolsa_id = bolsa_id;
    novo->proximo = NULL;
    
    if (l->cabeca == NULL) {
        l->cabeca = novo;
    } else {
        NoLista* atual = l->cabeca;
        while (atual->proximo != NULL) {
            atual = atual->proximo;
        }
        atual->proximo = novo;
    }
    l->tamanho++;
}

int lista_remover_inicio(Lista* l) {
    if (!l || l->cabeca == NULL) return -1;
    
    NoLista* remover = l->cabeca;
    int id = remover->bolsa_id;
    
    l->cabeca = remover->proximo;
    free(remover);
    l->tamanho--;
    
    return id;
}

int lista_contem(Lista* l, int bolsa_id) {
    if (!l) return 0;
    
    NoLista* atual = l->cabeca;
    while (atual != NULL) {
        if (atual->bolsa_id == bolsa_id) {
            return 1;
        }
        atual = atual->proximo;
    }
    return 0;
}

void lista_liberar(Lista* l) {
    if (!l) return;
    
    NoLista* atual = l->cabeca;
    while (atual != NULL) {
        NoLista* proximo = atual->proximo;
        free(atual);
        atual = proximo;
    }
    l->cabeca = NULL;
    l->tamanho = 0;
}

void lista_imprimir(Lista* l) {
    if (!l) return;
    
    printf("Lista de Bolsas (tam: %lu): [", (unsigned long)l->tamanho);
    NoLista* atual = l->cabeca;
    while (atual != NULL) {
        printf("%d", atual->bolsa_id);
        if (atual->proximo != NULL) printf(" -> ");
        atual = atual->proximo;
    }
    printf("]\n");
}
