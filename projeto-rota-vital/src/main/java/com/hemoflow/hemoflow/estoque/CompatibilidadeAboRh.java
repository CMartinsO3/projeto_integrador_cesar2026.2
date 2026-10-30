package com.hemoflow.hemoflow.estoque;

import com.hemoflow.hemoflow.dominio.TipoSanguineo;

/**
 * Compatibilidade ABO/Rh por matriz 8x8 (Unidade 2).
 *
 * <p>Versao C/C++ equivalente: {@code c_structures/compat_abo_rh.cpp}.
 * A matriz e indexada por {@link TipoSanguineo#ordinal()}, que corresponde ao
 * valor numerico do enum {@code TipoSanguineo} da versao C (mesma ordem).
 * Por isso a ORDEM das constantes do enum nao pode mudar sem atualizar a
 * matriz (ha um teste que protege isso).</p>
 *
 * <p>Complexidade: {@link #compativel} faz duas verificacoes de {@code null} e
 * uma unica leitura de matriz: tempo O(1) e espaco O(1) (64 celulas fixas,
 * que nao crescem com o tamanho do estoque).</p>
 */
public final class CompatibilidadeAboRh {

    /**
     * {@code MATRIZ[doador][receptor]} e {@code true} quando a doacao e compativel.
     * Linhas = doador; colunas = receptor, na ordem do enum {@link TipoSanguineo}.
     */
    private static final boolean[][] MATRIZ = {
        //              A+     A-     B+     B-     AB+    AB-    O+     O-    <- receptor
        /* A+  */ { true , false, false, false, true , false, false, false },
        /* A-  */ { true , true , false, false, true , true , false, false },
        /* B+  */ { false, false, true , false, true , false, false, false },
        /* B-  */ { false, false, true , true , true , true , false, false },
        /* AB+ */ { false, false, false, false, true , false, false, false },
        /* AB- */ { false, false, false, false, true , true , false, false },
        /* O+  */ { true , false, true , false, true , false, true , false },
        /* O-  */ { true , true , true , true , true , true , true , true  }
    };

    static {
        if (MATRIZ.length != TipoSanguineo.values().length) {
            throw new IllegalStateException(
                    "A matriz ABO/Rh precisa ter uma linha por constante de TipoSanguineo");
        }
    }

    private CompatibilidadeAboRh() {
    }

    /**
     * Indica se uma bolsa do tipo {@code doador} pode ser dada a um paciente
     * do tipo {@code receptorSolicitado}. Retorna {@code false} se algum for nulo.
     */
    public static boolean compativel(TipoSanguineo doador, TipoSanguineo receptorSolicitado) {
        if (doador == null || receptorSolicitado == null) {
            return false;
        }
        return MATRIZ[doador.ordinal()][receptorSolicitado.ordinal()];
    }
}
