#ifndef COMPAT_ABO_RH_H
#define COMPAT_ABO_RH_H

/*
 * Compatibilidade ABO/Rh por matriz 8x8 (Unidade 2).
 *
 * A ORDEM dos valores deste enum e o indice da matriz e e IGUAL a ordem do
 * enum TipoSanguineo em Java (dominio/TipoSanguineo.java), usada la via ordinal().
 */
typedef enum {
    TS_A_POS  = 0,
    TS_A_NEG  = 1,
    TS_B_POS  = 2,
    TS_B_NEG  = 3,
    TS_AB_POS = 4,
    TS_AB_NEG = 5,
    TS_O_POS  = 6,
    TS_O_NEG  = 7
} TipoSanguineo;

#define TS_TOTAL 8

/* Retorna 1 se uma bolsa do tipo 'doador' pode ser dada a um paciente do tipo
 * 'receptor', e 0 caso contrario (ou se algum indice for invalido). O(1). */
int compat_compativel(int doador, int receptor);

/* Rotulo legivel do tipo (ex.: "A+"), ou "?" se o indice for invalido. */
const char* compat_rotulo(int tipo);

#endif /* COMPAT_ABO_RH_H */
