#include "pilha.hpp"
#include <stdio.h>
#include <stdlib.h>

void pilha_inicializar(Pilha* p) {
    if (p) {
        p->topo = NULL;
        p->tamanho = 0;
    }
}

void pilha_empilhar(Pilha* p, int operacao_id) {
    if (!p) return;
    
    NoPilha* novo = (NoPilha*)malloc(sizeof(NoPilha));
    if (!novo) {
        fprintf(stderr, "Erro de alocação de memória!\n");
        return;
    }
    novo->operacao_id = operacao_id;
    novo->proximo = p->topo;
    
    p->topo = novo;
    p->tamanho++;
}

int pilha_desempilhar(Pilha* p) {
    if (!p || p->topo == NULL) return -1;
    
    NoPilha* remover = p->topo;
    int id = remover->operacao_id;
    
    p->topo = remover->proximo;
    free(remover);
    p->tamanho--;
    
    return id;
}

int pilha_espiar(Pilha* p) {
    if (!p || p->topo == NULL) return -1;
    return p->topo->operacao_id;
}

void pilha_liberar(Pilha* p) {
    if (!p) return;
    
    NoPilha* atual = p->topo;
    while (atual != NULL) {
        NoPilha* proximo = atual->proximo;
        free(atual);
        atual = proximo;
    }
    p->topo = NULL;
    p->tamanho = 0;
}

void pilha_imprimir(Pilha* p) {
    if (!p) return;
    
    printf("Pilha de Historico (tam: %lu): [topo -> ", (unsigned long)p->tamanho);
    NoPilha* atual = p->topo;
    while (atual != NULL) {
        printf("%d", atual->operacao_id);
        if (atual->proximo != NULL) printf(", ");
        atual = atual->proximo;
    }
    printf("]\n");
}
