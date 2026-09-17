package com.hemoflow.hemoflow.estoque;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import com.hemoflow.hemoflow.dominio.TipoSanguineo;

/**
 * Matriz didática ABO/Rh: quem pode doar para o receptor.
 */
public final class CompatibilidadeAboRh {

    private static final Map<TipoSanguineo, Set<TipoSanguineo>> DOADORES_POR_RECEPTOR = Map.of(
            TipoSanguineo.O_NEG, EnumSet.of(TipoSanguineo.O_NEG),
            TipoSanguineo.O_POS, EnumSet.of(TipoSanguineo.O_NEG, TipoSanguineo.O_POS),
            TipoSanguineo.A_NEG, EnumSet.of(TipoSanguineo.O_NEG, TipoSanguineo.A_NEG),
            TipoSanguineo.A_POS, EnumSet.of(TipoSanguineo.O_NEG, TipoSanguineo.O_POS, TipoSanguineo.A_NEG, TipoSanguineo.A_POS),
            TipoSanguineo.B_NEG, EnumSet.of(TipoSanguineo.O_NEG, TipoSanguineo.B_NEG),
            TipoSanguineo.B_POS, EnumSet.of(TipoSanguineo.O_NEG, TipoSanguineo.O_POS, TipoSanguineo.B_NEG, TipoSanguineo.B_POS),
            TipoSanguineo.AB_NEG, EnumSet.of(TipoSanguineo.O_NEG, TipoSanguineo.A_NEG, TipoSanguineo.B_NEG, TipoSanguineo.AB_NEG),
            TipoSanguineo.AB_POS, EnumSet.allOf(TipoSanguineo.class)
    );

    private CompatibilidadeAboRh() {
    }

    public static boolean compativel(TipoSanguineo tipoBolsa, TipoSanguineo tipoReceptor) {
        return DOADORES_POR_RECEPTOR.get(tipoReceptor).contains(tipoBolsa);
    }
}
