package com.hemoflow.hemoflow.estoque;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.hemoflow.hemoflow.dominio.TipoSanguineo;

class CompatibilidadeAboRhTest {

    @Test
    void oNegativoDoaParaTodos() {
        for (TipoSanguineo receptor : TipoSanguineo.values()) {
            assertTrue(CompatibilidadeAboRh.compativel(TipoSanguineo.O_NEG, receptor));
        }
    }

    @Test
    void aPositivoNaoDoaParaONegativo() {
        assertFalse(CompatibilidadeAboRh.compativel(TipoSanguineo.A_POS, TipoSanguineo.O_NEG));
    }
}
