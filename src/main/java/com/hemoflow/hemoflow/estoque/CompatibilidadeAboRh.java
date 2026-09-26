package com.hemoflow.hemoflow.estoque;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import com.hemoflow.hemoflow.dominio.TipoSanguineo;

public final class CompatibilidadeAboRh {

    private static final Map<TipoSanguineo, Set<TipoSanguineo>> DOADOR_PARA_RECEPTORES =
            new EnumMap<>(TipoSanguineo.class);

    static {
        DOADOR_PARA_RECEPTORES.put(TipoSanguineo.O_NEG, EnumSet.allOf(TipoSanguineo.class));

        DOADOR_PARA_RECEPTORES.put(TipoSanguineo.O_POS, EnumSet.of(
                TipoSanguineo.O_POS, TipoSanguineo.A_POS, TipoSanguineo.B_POS, TipoSanguineo.AB_POS));

        DOADOR_PARA_RECEPTORES.put(TipoSanguineo.A_NEG, EnumSet.of(
                TipoSanguineo.A_NEG, TipoSanguineo.A_POS, TipoSanguineo.AB_NEG, TipoSanguineo.AB_POS));

        DOADOR_PARA_RECEPTORES.put(TipoSanguineo.A_POS, EnumSet.of(
                TipoSanguineo.A_POS, TipoSanguineo.AB_POS));

        DOADOR_PARA_RECEPTORES.put(TipoSanguineo.B_NEG, EnumSet.of(
                TipoSanguineo.B_NEG, TipoSanguineo.B_POS, TipoSanguineo.AB_NEG, TipoSanguineo.AB_POS));

        DOADOR_PARA_RECEPTORES.put(TipoSanguineo.B_POS, EnumSet.of(
                TipoSanguineo.B_POS, TipoSanguineo.AB_POS));

        DOADOR_PARA_RECEPTORES.put(TipoSanguineo.AB_NEG, EnumSet.of(
                TipoSanguineo.AB_NEG, TipoSanguineo.AB_POS));

        DOADOR_PARA_RECEPTORES.put(TipoSanguineo.AB_POS, EnumSet.of(TipoSanguineo.AB_POS));
    }

    private CompatibilidadeAboRh() {
    }

    public static boolean compativel(TipoSanguineo doador, TipoSanguineo receptorSolicitado) {
        if (doador == null || receptorSolicitado == null) {
            return false;
        }
        return DOADOR_PARA_RECEPTORES.getOrDefault(doador, Set.of()).contains(receptorSolicitado);
    }
}