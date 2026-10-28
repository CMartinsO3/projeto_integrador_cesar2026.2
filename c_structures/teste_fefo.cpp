#include <stdio.h>
#include <stdlib.h>
#include "fefo.hpp"

static int falhas = 0;

static void confere(int cond, const char* msg) {
    if (!cond) { printf("  FALHA: %s\n", msg); falhas++; }
}

/* ---- oráculo independente: vetor ordenado com qsort ---- */
typedef struct { int id, validade, chegada; } Item;

static int cmp_item(const void* a, const void* b) {
    const Item* x = (const Item*)a; const Item* y = (const Item*)b;
    if (x->validade != y->validade) return x->validade < y->validade ? -1 : 1;
    if (x->id != y->id) return x->id < y->id ? -1 : 1;
    return x->chegada < y->chegada ? -1 : (x->chegada > y->chegada ? 1 : 0);
}

int main() {
    const int HOJE = 20261017;
    ListaFefo l;
    fefo_inicializar(&l);

    printf("--- 1. Exemplo do guia: B1 15/10, B2 25/10, B3 03/11 + NOVA 20/10 ---\n");
    /* usamos datas de 2026-10 como hoje=2026-10-10 para nao vencer */
    int hoje = 20261010;
    fefo_inserir_ordenado(&l, 1, 20261015, hoje);
    fefo_inserir_ordenado(&l, 2, 20261025, hoje);
    fefo_inserir_ordenado(&l, 3, 20261103, hoje);
    fefo_imprimir(&l);
    fefo_inserir_ordenado(&l, 4, 20261020, hoje);
    fefo_imprimir(&l);
    confere(l.cabeca->bolsa_id == 1 && l.cabeca->proximo->bolsa_id == 4
            && l.cabeca->proximo->proximo->bolsa_id == 2, "NOVA (id 4) deve ficar entre B1 e B2");
    confere(fefo_remover_primeira(&l, hoje) == 1, "primeira a sair: B1");
    confere(fefo_remover_primeira(&l, hoje) == 4, "segunda a sair: NOVA");
    confere(fefo_remover_primeira(&l, hoje) == 2, "terceira a sair: B2");
    confere(fefo_remover_primeira(&l, hoje) == 3, "quarta a sair: B3");
    confere(fefo_remover_primeira(&l, hoje) == -1, "lista vazia devolve -1");
    confere(l.tamanho == 0, "tamanho 0 no final");

    printf("--- 2. Empate de validade: menor id primeiro; ids iguais: ordem de chegada ---\n");
    fefo_inserir_ordenado(&l, 30, 20261101, hoje);
    fefo_inserir_ordenado(&l, 10, 20261101, hoje);
    fefo_inserir_ordenado(&l, 20, 20261101, hoje);
    fefo_inserir_ordenado(&l, 10, 20261101, hoje);   /* mesmo id e mesma validade */
    fefo_imprimir(&l);
    confere(fefo_remover_primeira(&l, hoje) == 10, "empate -> id 10");
    confere(fefo_remover_primeira(&l, hoje) == 10, "empate -> id 10 (segunda copia)");
    confere(fefo_remover_primeira(&l, hoje) == 20, "empate -> id 20");
    confere(fefo_remover_primeira(&l, hoje) == 30, "empate -> id 30");

    printf("--- 3. Bolsa vencida e descartada na insercao; vence na lista e e pulada ---\n");
    confere(fefo_inserir_ordenado(&l, 7, 20261009, hoje) == 0, "validade < hoje nao entra");
    confere(l.tamanho == 0, "lista continua vazia");
    confere(fefo_inserir_ordenado(&l, 8, 20261010, hoje) == 1, "vence hoje ainda e valida");
    fefo_inserir_ordenado(&l, 9, 20261020, hoje);
    confere(fefo_remover_primeira(&l, 20261015) == 9, "id 8 venceu na lista e foi pulada");
    confere(l.tamanho == 0, "as duas sairam da lista");
    fefo_inserir_ordenado(&l, 5, 20261011, hoje);
    confere(fefo_remover_primeira(&l, 20261020) == -1, "so tinha bolsa vencida -> -1");

    printf("--- 4. Aleatorio (semente fixa) contra o oraculo qsort ---\n");
    srand(12345);
    enum { N = 300 };
    Item itens[N];
    int n = 0;
    for (int i = 0; i < N; i++) {
        int dia = 17 + rand() % 14;             /* 20261017..20261030: sempre >= HOJE */
        itens[n].id = 1 + rand() % 40;          /* ids repetidos de proposito */
        itens[n].validade = 20261000 + dia;
        itens[n].chegada = i;
        confere(fefo_inserir_ordenado(&l, itens[n].id, itens[n].validade, HOJE) == 1, "insercao valida");
        n++;
    }
    confere(l.tamanho == (size_t)N, "300 bolsas na lista");
    qsort(itens, N, sizeof(Item), cmp_item);
    int ordem_ok = 1;
    for (int i = 0; i < N; i++) {
        int id = fefo_remover_primeira(&l, HOJE);
        if (id != itens[i].id) { ordem_ok = 0; printf("  divergiu na posicao %d: lista=%d oraculo=%d\n", i, id, itens[i].id); break; }
    }
    confere(ordem_ok, "ordem de saida igual a do oraculo");
    confere(l.tamanho == 0, "lista vazia no fim");

    fefo_liberar(&l);
    printf("\nFalhas: %d\n", falhas);
    return falhas == 0 ? 0 : 1;
}
