#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "tabela_hash.hpp"

static int falhas = 0;

static void confere(int cond, const char* msg) {
    if (!cond) { printf("  FALHA: %s\n", msg); falhas++; }
}

int main() {
    const int HOJE = 20261010;
    TabelaHash t;
    hash_inicializar(&t);

    printf("--- 1. Inserir e buscar ---\n");
    Grupo* a = hash_inserir(&t, "A_POS|HEMACIAS");
    confere(a != NULL, "inserir devolve o grupo");
    confere(hash_buscar(&t, "A_POS|HEMACIAS") == a, "buscar acha o mesmo grupo");
    confere(hash_inserir(&t, "A_POS|HEMACIAS") == a, "inserir chave repetida nao duplica");
    confere(t.grupos == 1, "uma chave guardada");
    printf("posicoes (tabela de %d): A_POS|HEMACIAS=%u  O_NEG|PLASMA=%u  AB_NEG|PLAQUETAS=%u\n",
           HASH_TAM, hash_posicao("A_POS|HEMACIAS"), hash_posicao("O_NEG|PLASMA"), hash_posicao("AB_NEG|PLAQUETAS"));
    confere(hash_posicao("A_POS|HEMACIAS") == 21, "posicao de A_POS|HEMACIAS (mesmo valor do Java)");
    confere(hash_posicao("O_NEG|PLASMA") == 31, "posicao de O_NEG|PLASMA (mesmo valor do Java)");
    confere(hash_posicao("AB_NEG|PLAQUETAS") == 65, "posicao de AB_NEG|PLAQUETAS (mesmo valor do Java)");

    printf("--- 2. Chave inexistente e chaves invalidas ---\n");
    confere(hash_buscar(&t, "B_NEG|PLASMA") == NULL, "chave inexistente -> NULL");
    confere(hash_buscar(&t, "") == NULL, "chave vazia -> NULL");
    confere(hash_buscar(&t, NULL) == NULL, "chave nula -> NULL");
    confere(hash_inserir(&t, NULL) == NULL, "inserir chave nula -> NULL");
    confere(hash_remover(&t, "B_NEG|PLASMA") == 0, "remover inexistente -> 0");

    printf("--- 3. Colisao forcada: '!' (33) e 'd' (100) caem no mesmo bucket (100-33 = %d = HASH_TAM) ---\n", HASH_TAM);
    confere(hash_posicao("!") == hash_posicao("d"), "! e d colidem");
    Grupo* ga = hash_inserir(&t, "!");
    Grupo* gt = hash_inserir(&t, "d");
    confere(ga != NULL && gt != NULL && ga != gt, "dois grupos distintos");
    confere(hash_buscar(&t, "!") == ga && hash_buscar(&t, "d") == gt, "os dois sao encontrados");
    confere(hash_maior_cadeia(&t) == 2, "cadeia de tamanho 2");
    confere(hash_remover(&t, "d") == 1, "remove o primeiro da cadeia (inserido por ultimo)");
    confere(hash_buscar(&t, "d") == NULL && hash_buscar(&t, "!") == ga, "! continua; d sumiu");
    gt = hash_inserir(&t, "d");
    confere(hash_remover(&t, "!") == 1, "remove o ultimo da cadeia");
    confere(hash_buscar(&t, "!") == NULL && hash_buscar(&t, "d") == gt, "d continua; ! sumiu");
    confere(hash_remover(&t, "d") == 1, "limpa");
    confere(t.grupos == 1, "sobrou so A_POS|HEMACIAS");

    printf("--- 4. Indice de estoque: lista FEFO por chave ---\n");
    /* tipo 0 = A_POS, hemocomponente 0 = HEMACIAS */
    indice_registrar(&t, 0, 0, 2, 20261025, HOJE);
    indice_registrar(&t, 0, 0, 1, 20261015, HOJE);
    indice_registrar(&t, 7, 1, 9, 20261101, HOJE);      /* O_NEG | PLASMA */
    confere(indice_quantidade(&t, 0, 0) == 2, "A_POS|HEMACIAS tem 2 bolsas");
    confere(indice_quantidade(&t, 7, 1) == 1, "O_NEG|PLASMA tem 1 bolsa");
    confere(indice_quantidade(&t, 3, 2) == 0, "grupo inexistente tem 0");
    confere(indice_registrar(&t, 0, 0, 5, 20261001, HOJE) == 0, "bolsa vencida nao entra");
    confere(indice_registrar(&t, 9, 0, 5, 20261101, HOJE) == 0, "tipo invalido nao entra");
    confere(indice_proxima(&t, 0, 0, HOJE) == 1, "primeira A_POS|HEMACIAS: vence 15/10");
    confere(indice_proxima(&t, 0, 0, HOJE) == 2, "segunda: vence 25/10");
    confere(hash_buscar(&t, "A_POS|HEMACIAS") == NULL, "grupo vazio saiu da tabela");
    confere(indice_proxima(&t, 0, 0, HOJE) == -1, "grupo inexistente -> -1");
    confere(indice_proxima(&t, 7, 1, 20261201) == -1, "so tinha bolsa vencida -> -1");
    confere(t.grupos == 0, "tabela vazia");

    printf("--- 5. Todas as 32 chaves: nenhuma se perde, fator de carga e cadeias ---\n");
    char chaves[HASH_TIPOS * HASH_HEMOCOMPONENTES][HASH_CHAVE_MAX];
    int n = 0;
    for (int tp = 0; tp < HASH_TIPOS; tp++)
        for (int hm = 0; hm < HASH_HEMOCOMPONENTES; hm++) {
            hash_montar_chave(chaves[n], HASH_CHAVE_MAX, tp, hm);
            confere(indice_registrar(&t, tp, hm, 100 + n, 20261101 + n % 7, HOJE) == 1, "registrar");
            n++;
        }
    confere(t.grupos == (size_t)n, "32 grupos");
    for (int i = 0; i < n; i++) {
        Grupo* g = hash_buscar(&t, chaves[i]);
        confere(g != NULL && strcmp(g->chave, chaves[i]) == 0, "a chave encontrada e a procurada");
    }
    printf("grupos: %lu  fator de carga: %.2f  maior cadeia: %lu\n",
           (unsigned long)t.grupos, hash_fator_de_carga(&t), (unsigned long)hash_maior_cadeia(&t));
    confere(hash_fator_de_carga(&t) < 0.5, "fator de carga abaixo de 0,5");
    for (int i = 0; i < n; i++) {
        confere(hash_remover(&t, chaves[i]) == 1, "remover cada chave");
        confere(hash_buscar(&t, chaves[i]) == NULL, "chave removida some");
    }
    confere(t.grupos == 0, "tabela vazia no fim");

    hash_liberar(&t);
    printf("\nFalhas: %d\n", falhas);
    return falhas == 0 ? 0 : 1;
}
