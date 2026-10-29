#ifndef TABELA_HASH_H
#define TABELA_HASH_H

#include <stddef.h>
#include "fefo.hpp"

/*
 * Índice de estoque por tabela hash com encadeamento (Unidade 2).
 *
 * Chave: tipo sanguíneo + hemocomponente, no formato "A_POS|HEMACIAS".
 * Cada posição ("bucket") guarda uma lista encadeada de Grupos; cada Grupo
 * guarda a lista FEFO (fefo.hpp) das bolsas daquela chave. Duas chaves que
 * caem na mesma posição (colisão) ficam encadeadas no mesmo bucket.
 *
 * Tamanho: existem 8 tipos x 4 hemocomponentes = 32 chaves possíveis. Uma
 * tabela de 67 posições (número primo) deixa o fator de carga em 32/67 ~ 0,48
 * no pior caso, então as cadeias ficam curtas.
 */
#define HASH_TAM 67

/* Mesma ordem do enum TipoSanguineo (compat_abo_rh.hpp) e do enum Java. */
#define HASH_TIPOS 8
/* Mesma ordem do enum Hemocomponente do Java. */
#define HASH_HEMOCOMPONENTES 4

#define HASH_CHAVE_MAX 40

typedef struct Grupo {
    char chave[HASH_CHAVE_MAX];
    ListaFefo fefo;
    struct Grupo* proximo;      /* próximo grupo do mesmo bucket (colisão) */
} Grupo;

typedef struct {
    Grupo* buckets[HASH_TAM];
    size_t grupos;              /* quantidade de chaves guardadas */
} TabelaHash;

/* Função hash: h = h*31 + caractere (aritmética de 32 bits sem sinal). */
unsigned int hash_funcao(const char* chave);

/* Posição da chave na tabela: hash_funcao(chave) % HASH_TAM. */
unsigned int hash_posicao(const char* chave);

void hash_inicializar(TabelaHash* t);

/* Devolve o grupo da chave, criando-o (com lista FEFO vazia) se ainda não existir.
 * O(1) em média; O(n) no pior caso (todas as chaves no mesmo bucket).
 * Retorna NULL se a chave for inválida ou faltar memória. */
Grupo* hash_inserir(TabelaHash* t, const char* chave);

/* Devolve o grupo da chave, ou NULL se não existir. Mesma complexidade. */
Grupo* hash_buscar(const TabelaHash* t, const char* chave);

/* Remove o grupo da chave (e libera a lista FEFO dele). Retorna 1 se removeu, 0 se não existia. */
int hash_remover(TabelaHash* t, const char* chave);

/* Libera toda a tabela. */
void hash_liberar(TabelaHash* t);

/* Grupos / HASH_TAM. */
double hash_fator_de_carga(const TabelaHash* t);

/* Tamanho da maior cadeia (pior bucket). */
size_t hash_maior_cadeia(const TabelaHash* t);

/* Monta a chave "TIPO|HEMOCOMPONENTE" (ex.: "A_POS|HEMACIAS"). Retorna 1 se os índices são válidos. */
int hash_montar_chave(char* destino, size_t tamanho, int tipo, int hemocomponente);

/* ---- operações de estoque sobre o índice ---- */

/* Registra a bolsa no grupo da sua chave (cria o grupo se preciso).
 * Retorna 1 se entrou; 0 se a bolsa está vencida ou os índices são inválidos. */
int indice_registrar(TabelaHash* t, int tipo, int hemocomponente, int bolsa_id, int validade, int hoje);

/* Retira a bolsa válida que vence primeiro do grupo; se o grupo ficar vazio, remove-o da tabela.
 * Retorna o id da bolsa, ou -1 se não houver. */
int indice_proxima(TabelaHash* t, int tipo, int hemocomponente, int hoje);

/* Quantidade de bolsas guardadas no grupo (0 se não existir). */
size_t indice_quantidade(const TabelaHash* t, int tipo, int hemocomponente);

#endif /* TABELA_HASH_H */
