package com.hemoflow.hemoflow.estoque;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.hemoflow.hemoflow.dominio.TipoSanguineo;

class CompatibilidadeAboRhTest {

    @Test
    @DisplayName("O negativo é doador universal: compatível com todos os tipos receptores")
    void oNegativoDeveSerCompativelComTodos() {
        for (TipoSanguineo receptor : TipoSanguineo.values()) {
            assertTrue(CompatibilidadeAboRh.compativel(TipoSanguineo.O_NEG, receptor),
                    "O- deveria ser compatível com " + receptor);
        }
    }

    @Test
    @DisplayName("AB positivo só pode ser doado para receptores AB positivo")
    void abPositivoSoDeveSerCompativelComAbPositivo() {
        assertTrue(CompatibilidadeAboRh.compativel(TipoSanguineo.AB_POS, TipoSanguineo.AB_POS));

        for (TipoSanguineo receptor : TipoSanguineo.values()) {
            if (receptor != TipoSanguineo.AB_POS) {
                assertFalse(CompatibilidadeAboRh.compativel(TipoSanguineo.AB_POS, receptor),
                        "AB+ não deveria ser compatível com " + receptor);
            }
        }
    }

    @Test
    @DisplayName("Mesmo tipo sanguíneo é sempre compatível consigo mesmo")
    void mesmoTipoDeveSerSempreCompativelConsigoMesmo() {
        for (TipoSanguineo tipo : TipoSanguineo.values()) {
            assertTrue(CompatibilidadeAboRh.compativel(tipo, tipo),
                    tipo + " deveria ser compatível consigo mesmo");
        }
    }

    @Test
    @DisplayName("A positivo não pode ser doado para um receptor A negativo")
    void tipoPositivoNaoDeveSerCompativelComNegativoDeMesmoGrupo() {
        assertFalse(CompatibilidadeAboRh.compativel(TipoSanguineo.A_POS, TipoSanguineo.A_NEG));
        assertFalse(CompatibilidadeAboRh.compativel(TipoSanguineo.B_POS, TipoSanguineo.B_NEG));
    }

    @Test
    @DisplayName("A negativo é compatível com A positivo, mas o inverso não é verdadeiro")
    void compatibilidadeNaoDeveSerSimetrica() {
        assertTrue(CompatibilidadeAboRh.compativel(TipoSanguineo.A_NEG, TipoSanguineo.A_POS));
        assertFalse(CompatibilidadeAboRh.compativel(TipoSanguineo.A_POS, TipoSanguineo.A_NEG));
    }

    @Test
    @DisplayName("compativel() retorna false quando algum dos tipos é nulo")
    void compativelDeveRetornarFalseParaTiposNulos() {
        assertFalse(CompatibilidadeAboRh.compativel(null, TipoSanguineo.O_POS));
        assertFalse(CompatibilidadeAboRh.compativel(TipoSanguineo.O_POS, null));
        assertFalse(CompatibilidadeAboRh.compativel(null, null));
    }
}
