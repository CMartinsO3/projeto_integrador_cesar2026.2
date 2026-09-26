#include "fila.hpp"
#include <stdio.h>
#include <stdlib.h>

void fila_inicializar(Fila* f) {
    if (f) {
        f->frente = NULL;
        f->cauda = NULL;
        f->tamanho = 0;
    }
}

void fila_enfileirar(Fila* f, int requisicao_id) {
    if (!f) return;
    
    NoFila* novo = (NoFila*)malloc(sizeof(NoFila));
    if (!novo) {
        fprintf(stderr, "Erro de alocação de memória!\n");
        return;
    }
    novo->requisicao_id = requisicao_id;
    novo->proximo = NULL;
    
    if (f->cauda == NULL) {
        /* Fila vazia */
        f->frente = novo;
        f->cauda = novo;
    } else {
        f->cauda->proximo = novo;
        f->cauda = novo;
    }
    f->tamanho++;
}

int fila_desenfileirar(Fila* f) {
    if (!f || f->frente == NULL) return -1;
    
    NoFila* remover = f->frente;
    int id = remover->requisicao_id;
    
    f->frente = remover->proximo;
    if (f->frente == NULL) {
        /* Fila ficou vazia */
        f->cauda = NULL;
    }
    
    free(remover);
    f->tamanho--;
    
    return id;
}

int fila_espiar(Fila* f) {
    if (!f || f->frente == NULL) return -1;
    return f->frente->requisicao_id;
}

void fila_liberar(Fila* f) {
    if (!f) return;
    
    NoFila* atual = f->frente;
    while (atual != NULL) {
        NoFila* proximo = atual->proximo;
        free(atual);
        atual = proximo;
    }
    f->frente = NULL;
    f->cauda = NULL;
    f->tamanho = 0;
}

void fila_imprimir(Fila* f) {
    if (!f) return;
    
    printf("Fila de Requisicoes (tam: %lu): [frente -> ", (unsigned long)f->tamanho);
    NoFila* atual = f->frente;
    while (atual != NULL) {
        printf("%d", atual->requisicao_id);
        if (atual->proximo != NULL) printf(", ");
        atual = atual->proximo;
    }
    printf(" <- cauda]\n");
}
