#ifndef FEFO_H
#define FEFO_H

#include <stddef.h>

/*
 * FEFO (First Expired, First Out) por inserção ordenada (Unidade 2).
 *
 * A lista encadeada é mantida SEMPRE ordenada por validade crescente: a bolsa
 * que vence antes fica na cabeça, então retirar a próxima bolsa é O(1).
 * A validade é um inteiro no formato AAAAMMDD (ex.: 20261020), o que permite
 * comparar datas com os operadores < e > sem biblioteca de datas.
 *
 * Desempate (mesma validade): menor id de bolsa primeiro; se o id também for
 * igual, vale a ordem de chegada (a que entrou antes sai antes).
 */

typedef struct NoFefo {
    int bolsa_id;
    int validade;               /* AAAAMMDD */
    struct NoFefo* proximo;
} NoFefo;

typedef struct {
    NoFefo* cabeca;
    size_t tamanho;
} ListaFefo;

/* Inicializa uma lista vazia */
void fefo_inicializar(ListaFefo* l);

/* Insere a bolsa no lugar certo, mantendo a lista ordenada. O(n).
 * Bolsa já vencida (validade < hoje) é descartada.
 * Retorna 1 se inseriu, 0 se descartou ou se faltou memória. */
int fefo_inserir_ordenado(ListaFefo* l, int bolsa_id, int validade, int hoje);

/* Remove a primeira bolsa válida (descartando, antes, as que já venceram
 * enquanto estavam na lista). O(1) quando nada venceu.
 * Retorna o id da bolsa, ou -1 se não houver bolsa válida. */
int fefo_remover_primeira(ListaFefo* l, int hoje);

/* Libera toda a memória da lista */
void fefo_liberar(ListaFefo* l);

/* Imprime a lista (debug): id@validade -> ... */
void fefo_imprimir(const ListaFefo* l);

#endif /* FEFO_H */
