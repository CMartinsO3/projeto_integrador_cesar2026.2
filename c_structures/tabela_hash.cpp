#include "tabela_hash.hpp"
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

static const char* NOMES_TIPO[HASH_TIPOS] = {
    "A_POS", "A_NEG", "B_POS", "B_NEG", "AB_POS", "AB_NEG", "O_POS", "O_NEG"
};

static const char* NOMES_HEMOCOMPONENTE[HASH_HEMOCOMPONENTES] = {
    "HEMACIAS", "PLASMA", "PLAQUETAS", "CRIOPRECIPITADO"
};

unsigned int hash_funcao(const char* chave) {
    uint32_t h = 0;
    for (const char* c = chave; *c != '\0'; c++) {
        h = h * 31u + (unsigned char)(*c);
    }
    return h;
}

unsigned int hash_posicao(const char* chave) {
    return hash_funcao(chave) % HASH_TAM;
}

void hash_inicializar(TabelaHash* t) {
    if (!t) return;
    for (int i = 0; i < HASH_TAM; i++) {
        t->buckets[i] = NULL;
    }
    t->grupos = 0;
}

static int chave_valida(const char* chave) {
    return chave != NULL && chave[0] != '\0' && strlen(chave) < HASH_CHAVE_MAX;
}

Grupo* hash_buscar(const TabelaHash* t, const char* chave) {
    if (!t || !chave_valida(chave)) return NULL;
    for (Grupo* g = t->buckets[hash_posicao(chave)]; g != NULL; g = g->proximo) {
        if (strcmp(g->chave, chave) == 0) return g;
    }
    return NULL;
}

Grupo* hash_inserir(TabelaHash* t, const char* chave) {
    if (!t || !chave_valida(chave)) return NULL;

    Grupo* existente = hash_buscar(t, chave);
    if (existente) return existente;

    Grupo* novo = (Grupo*)malloc(sizeof(Grupo));
    if (!novo) {
        fprintf(stderr, "Erro de alocacao de memoria!\n");
        return NULL;
    }
    strcpy(novo->chave, chave);
    fefo_inicializar(&novo->fefo);

    unsigned int pos = hash_posicao(chave);
    novo->proximo = t->buckets[pos];        /* encadeamento: entra no início do bucket */
    t->buckets[pos] = novo;
    t->grupos++;
    return novo;
}

int hash_remover(TabelaHash* t, const char* chave) {
    if (!t || !chave_valida(chave)) return 0;

    /* p aponta para o ponteiro que aponta para o grupo (cabeça do bucket ou proximo do anterior) */
    Grupo** p = &t->buckets[hash_posicao(chave)];
    while (*p != NULL) {
        if (strcmp((*p)->chave, chave) == 0) {
            Grupo* remover = *p;
            *p = remover->proximo;          /* desencadeia o grupo */
            fefo_liberar(&remover->fefo);
            free(remover);
            t->grupos--;
            return 1;
        }
        p = &(*p)->proximo;
    }
    return 0;
}

void hash_liberar(TabelaHash* t) {
    if (!t) return;
    for (int i = 0; i < HASH_TAM; i++) {
        Grupo* g = t->buckets[i];
        while (g != NULL) {
            Grupo* proximo = g->proximo;
            fefo_liberar(&g->fefo);
            free(g);
            g = proximo;
        }
        t->buckets[i] = NULL;
    }
    t->grupos = 0;
}

double hash_fator_de_carga(const TabelaHash* t) {
    return t ? (double)t->grupos / HASH_TAM : 0.0;
}

size_t hash_maior_cadeia(const TabelaHash* t) {
    size_t maior = 0;
    if (!t) return 0;
    for (int i = 0; i < HASH_TAM; i++) {
        size_t n = 0;
        for (const Grupo* g = t->buckets[i]; g != NULL; g = g->proximo) n++;
        if (n > maior) maior = n;
    }
    return maior;
}

int hash_montar_chave(char* destino, size_t tamanho, int tipo, int hemocomponente) {
    if (!destino || tipo < 0 || tipo >= HASH_TIPOS
            || hemocomponente < 0 || hemocomponente >= HASH_HEMOCOMPONENTES) {
        return 0;
    }
    int n = snprintf(destino, tamanho, "%s|%s", NOMES_TIPO[tipo], NOMES_HEMOCOMPONENTE[hemocomponente]);
    return n > 0 && (size_t)n < tamanho;
}

int indice_registrar(TabelaHash* t, int tipo, int hemocomponente, int bolsa_id, int validade, int hoje) {
    char chave[HASH_CHAVE_MAX];
    if (!hash_montar_chave(chave, sizeof(chave), tipo, hemocomponente)) return 0;
    if (validade < hoje) return 0;                  /* vencida: nem cria o grupo */

    Grupo* g = hash_inserir(t, chave);
    if (!g) return 0;
    return fefo_inserir_ordenado(&g->fefo, bolsa_id, validade, hoje);
}

int indice_proxima(TabelaHash* t, int tipo, int hemocomponente, int hoje) {
    char chave[HASH_CHAVE_MAX];
    if (!hash_montar_chave(chave, sizeof(chave), tipo, hemocomponente)) return -1;

    Grupo* g = hash_buscar(t, chave);
    if (!g) return -1;

    int id = fefo_remover_primeira(&g->fefo, hoje);
    if (g->fefo.tamanho == 0) {
        hash_remover(t, chave);                     /* grupo vazio sai da tabela */
    }
    return id;
}

size_t indice_quantidade(const TabelaHash* t, int tipo, int hemocomponente) {
    char chave[HASH_CHAVE_MAX];
    if (!hash_montar_chave(chave, sizeof(chave), tipo, hemocomponente)) return 0;
    const Grupo* g = hash_buscar(t, chave);
    return g ? g->fefo.tamanho : 0;
}
