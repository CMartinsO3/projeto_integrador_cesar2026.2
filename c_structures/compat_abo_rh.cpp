#include "compat_abo_rh.hpp"

/*
 * MATRIZ[doador][receptor] = 1 se a doacao e compativel.
 * Linhas = tipo do doador; colunas = tipo do receptor, na ordem do enum:
 *              A+  A-  B+  B-  AB+ AB- O+  O-
 * Tamanho fixo: 8 x 8 = 64 celulas, independente do tamanho do estoque.
 */
static const int MATRIZ[TS_TOTAL][TS_TOTAL] = {
    /* A+  */ { 1, 0, 0, 0, 1, 0, 0, 0 },
    /* A-  */ { 1, 1, 0, 0, 1, 1, 0, 0 },
    /* B+  */ { 0, 0, 1, 0, 1, 0, 0, 0 },
    /* B-  */ { 0, 0, 1, 1, 1, 1, 0, 0 },
    /* AB+ */ { 0, 0, 0, 0, 1, 0, 0, 0 },
    /* AB- */ { 0, 0, 0, 0, 1, 1, 0, 0 },
    /* O+  */ { 1, 0, 1, 0, 1, 0, 1, 0 },
    /* O-  */ { 1, 1, 1, 1, 1, 1, 1, 1 }
};

static const char* ROTULOS[TS_TOTAL] = {
    "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"
};

int compat_compativel(int doador, int receptor) {
    if (doador < 0 || doador >= TS_TOTAL || receptor < 0 || receptor >= TS_TOTAL) {
        return 0;                       /* indice invalido: nunca compativel */
    }
    return MATRIZ[doador][receptor];    /* uma unica leitura: O(1), sem laco */
}

const char* compat_rotulo(int tipo) {
    if (tipo < 0 || tipo >= TS_TOTAL) {
        return "?";
    }
    return ROTULOS[tipo];
}
