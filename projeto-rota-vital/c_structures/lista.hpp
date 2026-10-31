#ifndef LISTA_H
#define LISTA_H

#include <stddef.h>

/* Nó da lista encadeada para armazenar IDs de bolsas no estoque */
typedef struct NoLista {
    int bolsa_id;
    struct NoLista* proximo;
} NoLista;

/* Estrutura descritora da lista */
typedef struct {
    NoLista* cabeca;
    size_t tamanho;
} Lista;

/* Inicializa uma lista vazia */
void lista_inicializar(Lista* l);

/* Insere uma bolsa no início da lista (O(1)) */
void lista_inserir_inicio(Lista* l, int bolsa_id);

/* Insere uma bolsa no final da lista (O(n)) */
void lista_inserir_fim(Lista* l, int bolsa_id);

/* Remove e retorna a primeira bolsa da lista (O(1)). Retorna -1 se vazia */
int lista_remover_inicio(Lista* l);

/* Verifica se uma bolsa existe na lista. Retorna 1 se sim, 0 se não */
int lista_contem(Lista* l, int bolsa_id);

/* Libera toda a memória alocada para os nós da lista */
void lista_liberar(Lista* l);

/* Imprime a lista (para debug) */
void lista_imprimir(Lista* l);

#endif /* LISTA_H */
