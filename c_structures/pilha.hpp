#ifndef PILHA_H
#define PILHA_H

#include <stddef.h>

/* Nó da pilha LIFO para histórico de operações */
typedef struct NoPilha {
    int operacao_id;
    struct NoPilha* proximo;
} NoPilha;

/* Estrutura descritora da pilha */
typedef struct {
    NoPilha* topo;
    size_t tamanho;
} Pilha;

/* Inicializa uma pilha vazia */
void pilha_inicializar(Pilha* p);

/* Empilha uma operação no topo (O(1)) */
void pilha_empilhar(Pilha* p, int operacao_id);

/* Desempilha a operação do topo (O(1)). Retorna -1 se vazia */
int pilha_desempilhar(Pilha* p);

/* Retorna o elemento do topo sem remover. Retorna -1 se vazia */
int pilha_espiar(Pilha* p);

/* Libera memória de todos os nós */
void pilha_liberar(Pilha* p);

/* Imprime a pilha (para debug) */
void pilha_imprimir(Pilha* p);

#endif /* PILHA_H */
