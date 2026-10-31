package com.hemoflow.hemoflow.estoque;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    @DisplayName("AB positivo é receptor universal: recebe de todos os tipos doadores")
    void abPositivoDeveReceberDeTodos() {
        for (TipoSanguineo doador : TipoSanguineo.values()) {
            assertTrue(CompatibilidadeAboRh.compativel(doador, TipoSanguineo.AB_POS),
                    doador + " deveria poder doar para AB+");
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
    @DisplayName("Incompatibilidades entre grupos ABO diferentes são rejeitadas")
    void gruposAboIncompativeisDevemSerRejeitados() {
        assertFalse(CompatibilidadeAboRh.compativel(TipoSanguineo.A_POS, TipoSanguineo.B_NEG));
        assertFalse(CompatibilidadeAboRh.compativel(TipoSanguineo.A_NEG, TipoSanguineo.B_POS));
        assertFalse(CompatibilidadeAboRh.compativel(TipoSanguineo.B_POS, TipoSanguineo.A_POS));
        assertFalse(CompatibilidadeAboRh.compativel(TipoSanguineo.AB_NEG, TipoSanguineo.O_NEG));
        assertFalse(CompatibilidadeAboRh.compativel(TipoSanguineo.AB_POS, TipoSanguineo.O_POS));
        assertFalse(CompatibilidadeAboRh.compativel(TipoSanguineo.A_POS, TipoSanguineo.O_POS));
    }

    @Test
    @DisplayName("Receptor Rh negativo nunca recebe de doador Rh positivo")
    void receptorRhNegativoNaoRecebeDeRhPositivo() {
        for (TipoSanguineo doador : TipoSanguineo.values()) {
            for (TipoSanguineo receptor : TipoSanguineo.values()) {
                if (rhPositivo(doador) && !rhPositivo(receptor)) {
                    assertFalse(CompatibilidadeAboRh.compativel(doador, receptor),
                            doador + " (Rh+) não deveria doar para " + receptor + " (Rh-)");
                }
            }
        }
    }

    @Test
    @DisplayName("Tabela 8x8 inteira confere com a regra de antígenos (64 combinações)")
    void tabelaCompletaDeveConferirComARegraDeAntigenos() {
        for (TipoSanguineo doador : TipoSanguineo.values()) {
            for (TipoSanguineo receptor : TipoSanguineo.values()) {
                assertEquals(pelaRegra(doador, receptor),
                        CompatibilidadeAboRh.compativel(doador, receptor),
                        doador + " -> " + receptor);
            }
        }
    }

    @Test
    @DisplayName("Existem exatamente 27 combinações doador-receptor compatíveis")
    void deveHaver27CombinacoesCompativeis() {
        int compativeis = 0;
        for (TipoSanguineo doador : TipoSanguineo.values()) {
            for (TipoSanguineo receptor : TipoSanguineo.values()) {
                if (CompatibilidadeAboRh.compativel(doador, receptor)) {
                    compativeis++;
                }
            }
        }
        assertEquals(27, compativeis);
    }

    @Test
    @DisplayName("A ordem do enum TipoSanguineo coincide com os índices da matriz (e da versão C)")
    void ordemDoEnumDeveCorresponderAMatriz() {
        TipoSanguineo[] esperado = {
            TipoSanguineo.A_POS, TipoSanguineo.A_NEG, TipoSanguineo.B_POS, TipoSanguineo.B_NEG,
            TipoSanguineo.AB_POS, TipoSanguineo.AB_NEG, TipoSanguineo.O_POS, TipoSanguineo.O_NEG
        };
        assertEquals(esperado.length, TipoSanguineo.values().length);
        for (int i = 0; i < esperado.length; i++) {
            assertEquals(esperado[i], TipoSanguineo.values()[i], "posição " + i);
        }
    }

    @Test
    @DisplayName("compativel() retorna false quando algum dos tipos é nulo")
    void compativelDeveRetornarFalseParaTiposNulos() {
        assertFalse(CompatibilidadeAboRh.compativel(null, TipoSanguineo.O_POS));
        assertFalse(CompatibilidadeAboRh.compativel(TipoSanguineo.O_POS, null));
        assertFalse(CompatibilidadeAboRh.compativel(null, null));
    }

    // ---- oráculo independente da matriz: regra clínica dos antígenos ----

    private static boolean rhPositivo(TipoSanguineo tipo) {
        return tipo.name().endsWith("_POS");
    }

    private static int antigenos(TipoSanguineo tipo) {
        String grupo = tipo.name().substring(0, tipo.name().indexOf('_'));
        int bits = 0;
        if (grupo.contains("A")) {
            bits |= 1;      // antígeno A
        }
        if (grupo.contains("B")) {
            bits |= 2;      // antígeno B
        }
        if (rhPositivo(tipo)) {
            bits |= 4;      // antígeno Rh
        }
        return bits;
    }

    /** Compatível quando o doador não tem nenhum antígeno que o receptor não tenha. */
    private static boolean pelaRegra(TipoSanguineo doador, TipoSanguineo receptor) {
        return (antigenos(doador) & ~antigenos(receptor)) == 0;
    }
}
