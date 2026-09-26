#ifndef FILA_H
#define FILA_H

#include <stddef.h>

/* Nó da fila FIFO para requisições */
typedef struct NoFila {
    int requisicao_id;
    struct NoFila* proximo;
} NoFila;

/* Estrutura descritora da fila com controle de frente e cauda para O(1) */
typedef struct {
    NoFila* frente;
    NoFila* cauda;
    size_t tamanho;
} Fila;

/* Inicializa uma fila vazia */
void fila_inicializar(Fila* f);

/* Enfileira uma requisição no final (O(1)) */
void fila_enfileirar(Fila* f, int requisicao_id);

/* Desenfileira a requisição da frente (O(1)). Retorna -1 se vazia */
int fila_desenfileirar(Fila* f);

/* Retorna o elemento da frente sem remover. Retorna -1 se vazia */
int fila_espiar(Fila* f);

/* Libera memória de todos os nós */
void fila_liberar(Fila* f);

/* Imprime a fila (para debug) */
void fila_imprimir(Fila* f);

#endif /* FILA_H */
