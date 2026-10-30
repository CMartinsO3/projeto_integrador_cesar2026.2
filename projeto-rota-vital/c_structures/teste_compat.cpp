#include <stdio.h>
#include "compat_abo_rh.hpp"

/*
 * Teste da matriz ABO/Rh. O "oraculo" e a regra clinica, escrita de forma
 * independente da matriz: cada tipo tem antigenos (A=1, B=2, Rh=4) e a doacao e
 * compativel quando o doador NAO tem nenhum antigeno que o receptor nao tenha.
 */
static int antigenos(int tipo) {
    switch (tipo) {
        case TS_A_POS:  return 1 | 4;
        case TS_A_NEG:  return 1;
        case TS_B_POS:  return 2 | 4;
        case TS_B_NEG:  return 2;
        case TS_AB_POS: return 1 | 2 | 4;
        case TS_AB_NEG: return 1 | 2;
        case TS_O_POS:  return 4;
        default:        return 0;       /* O- */
    }
}

static int pela_regra(int doador, int receptor) {
    return (antigenos(doador) & ~antigenos(receptor)) == 0;
}

int main() {
    int falhas = 0, compativeis = 0;

    printf("Matriz de compatibilidade (linha = doador, coluna = receptor)\n");
    printf("      ");
    for (int r = 0; r < TS_TOTAL; r++) printf("%-4s", compat_rotulo(r));
    printf("\n");

    for (int d = 0; d < TS_TOTAL; d++) {
        printf("%-5s ", compat_rotulo(d));
        for (int r = 0; r < TS_TOTAL; r++) {
            int m = compat_compativel(d, r);
            printf("%-4s", m ? "sim" : ".");
            if (m != pela_regra(d, r)) {
                printf("\n  FALHA: %s -> %s deveria ser %d\n", compat_rotulo(d), compat_rotulo(r), pela_regra(d, r));
                falhas++;
            }
            compativeis += m;
        }
        printf("\n");
    }

    for (int r = 0; r < TS_TOTAL; r++) {
        if (!compat_compativel(TS_O_NEG, r)) { printf("FALHA: O- deveria doar para todos\n"); falhas++; }
        if (r != TS_AB_POS && compat_compativel(TS_AB_POS, r)) { printf("FALHA: AB+ so doa para AB+\n"); falhas++; }
    }
    if (compat_compativel(TS_A_POS, TS_B_NEG)) { printf("FALHA: A+ nao doa para B-\n"); falhas++; }
    if (compat_compativel(TS_A_POS, TS_A_NEG)) { printf("FALHA: A+ nao doa para A-\n"); falhas++; }
    if (!compat_compativel(TS_A_NEG, TS_A_POS)) { printf("FALHA: A- doa para A+\n"); falhas++; }

    if (compat_compativel(-1, 0) || compat_compativel(0, 8) || compat_compativel(99, 99)) {
        printf("FALHA: indice invalido deve ser incompativel\n"); falhas++;
    }

    printf("\nPares compativeis: %d (esperado: 27)\n", compativeis);
    if (compativeis != 27) falhas++;
    printf("Falhas: %d\n", falhas);
    return falhas == 0 ? 0 : 1;
}
