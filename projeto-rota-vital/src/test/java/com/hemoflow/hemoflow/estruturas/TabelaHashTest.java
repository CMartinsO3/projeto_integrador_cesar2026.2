package com.hemoflow.hemoflow.estruturas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TabelaHashTest {

    private static final String[] TIPOS = {"A_POS", "A_NEG", "B_POS", "B_NEG", "AB_POS", "AB_NEG", "O_POS", "O_NEG"};
    private static final String[] HEMOCOMPONENTES = {"HEMACIAS", "PLASMA", "PLAQUETAS", "CRIOPRECIPITADO"};

    @Test
    @DisplayName("Inserir e buscar devolvem o valor da chave")
    void inserirEBuscar() {
        TabelaHash<String> t = new TabelaHash<>();
        assertNull(t.inserir("A_POS|HEMACIAS", "grupo A+"));
        assertEquals("grupo A+", t.buscar("A_POS|HEMACIAS"));
        assertTrue(t.contem("A_POS|HEMACIAS"));
        assertEquals(1, t.tamanho());
    }

    @Test
    @DisplayName("Inserir chave repetida troca o valor e devolve o anterior, sem duplicar")
    void inserirChaveRepetidaSubstituiValor() {
        TabelaHash<Integer> t = new TabelaHash<>();
        t.inserir("O_NEG|PLASMA", 1);
        assertEquals(1, t.inserir("O_NEG|PLASMA", 2));
        assertEquals(2, t.buscar("O_NEG|PLASMA"));
        assertEquals(1, t.tamanho());
    }

    @Test
    @DisplayName("Chave inexistente: buscar devolve null, contem e remover dizem não")
    void chaveInexistente() {
        TabelaHash<String> t = new TabelaHash<>();
        t.inserir("A_POS|HEMACIAS", "x");
        assertNull(t.buscar("B_NEG|PLASMA"));
        assertFalse(t.contem("B_NEG|PLASMA"));
        assertFalse(t.remover("B_NEG|PLASMA"));
        assertEquals(1, t.tamanho());
    }

    @Test
    @DisplayName("Remover apaga só a chave pedida")
    void remover() {
        TabelaHash<String> t = new TabelaHash<>();
        t.inserir("A_POS|HEMACIAS", "a");
        t.inserir("O_NEG|PLASMA", "b");
        assertTrue(t.remover("A_POS|HEMACIAS"));
        assertNull(t.buscar("A_POS|HEMACIAS"));
        assertEquals("b", t.buscar("O_NEG|PLASMA"));
        assertFalse(t.remover("A_POS|HEMACIAS"));
        assertEquals(1, t.tamanho());
    }

    @Test
    @DisplayName("Colisão forçada: '!' (33) e 'd' (100) caem no mesmo bucket e convivem")
    void colisaoForcadaComTabelaPadrao() {
        TabelaHash<String> t = new TabelaHash<>();
        assertEquals(t.posicao("!"), t.posicao("d"));      // 100 - 33 = 67 = capacidade
        t.inserir("!", "primeira");
        t.inserir("d", "segunda");
        assertEquals("primeira", t.buscar("!"));
        assertEquals("segunda", t.buscar("d"));
        assertEquals(2, t.maiorCadeia());
        assertTrue(t.remover("!"));
        assertNull(t.buscar("!"));
        assertEquals("segunda", t.buscar("d"));
    }

    @Test
    @DisplayName("Todas as chaves no mesmo bucket: remover início, meio e fim da cadeia")
    void removerDeQualquerPosicaoDaCadeia() {
        TabelaHash<Integer> t = new TabelaHash<>(1);       // capacidade 1: tudo colide
        for (int i = 1; i <= 4; i++) {
            t.inserir("chave" + i, i);
        }
        assertEquals(4, t.maiorCadeia());

        assertTrue(t.remover("chave2"));                    // meio
        assertTrue(t.remover("chave4"));                    // cabeça (inserida por último)
        assertTrue(t.remover("chave1"));                    // fim
        assertEquals(1, t.tamanho());
        assertEquals(3, t.buscar("chave3"));
        assertNull(t.buscar("chave1"));
        assertNull(t.buscar("chave2"));
        assertNull(t.buscar("chave4"));
    }

    @Test
    @DisplayName("Posições calculadas pela função hash são as mesmas da versão em C")
    void posicoesIguaisAsDaVersaoEmC() {
        TabelaHash<String> t = new TabelaHash<>();
        assertEquals(67, t.capacidade());
        assertEquals(21, t.posicao("A_POS|HEMACIAS"));
        assertEquals(31, t.posicao("O_NEG|PLASMA"));
        assertEquals(65, t.posicao("AB_NEG|PLAQUETAS"));
    }

    @Test
    @DisplayName("As 32 chaves possíveis do estoque: nenhuma se perde e o fator de carga fica abaixo de 0,5")
    void todasAsChavesDoEstoque() {
        TabelaHash<String> t = new TabelaHash<>();
        List<String> chaves = new ArrayList<>();
        for (String tipo : TIPOS) {
            for (String hemo : HEMOCOMPONENTES) {
                chaves.add(tipo + "|" + hemo);
            }
        }
        assertEquals(32, chaves.size());
        for (String chave : chaves) {
            t.inserir(chave, "v:" + chave);
        }

        assertEquals(32, t.tamanho());
        for (String chave : chaves) {
            assertEquals("v:" + chave, t.buscar(chave));
        }
        assertTrue(t.fatorDeCarga() < 0.5, "fator de carga: " + t.fatorDeCarga());
        assertTrue(t.maiorCadeia() <= 3, "maior cadeia: " + t.maiorCadeia());
        assertEquals(32, t.chaves().size());

        for (String chave : chaves) {
            assertTrue(t.remover(chave));
        }
        assertTrue(t.estaVazia());
    }

    @Test
    @DisplayName("Operações aleatórias dão o mesmo resultado que um mapa de referência (tabela pequena, muitas colisões)")
    void operacoesAleatoriasContraMapaDeReferencia() {
        Random sorteio = new Random(7);
        TabelaHash<Integer> t = new TabelaHash<>(7);
        Map<String, Integer> referencia = new HashMap<>();

        for (int i = 0; i < 3000; i++) {
            String chave = "k" + sorteio.nextInt(60);
            int operacao = sorteio.nextInt(3);
            if (operacao == 0) {
                assertEquals(referencia.put(chave, i), t.inserir(chave, i));
            } else if (operacao == 1) {
                assertEquals(referencia.get(chave), t.buscar(chave));
            } else {
                assertEquals(referencia.remove(chave) != null, t.remover(chave));
            }
            assertEquals(referencia.size(), t.tamanho());
        }
        for (Map.Entry<String, Integer> e : referencia.entrySet()) {
            assertEquals(e.getValue(), t.buscar(e.getKey()));
        }
    }

    @Test
    @DisplayName("limpar() esvazia a tabela")
    void limpar() {
        TabelaHash<String> t = new TabelaHash<>();
        t.inserir("a", "1");
        t.inserir("b", "2");
        t.limpar();
        assertTrue(t.estaVazia());
        assertNull(t.buscar("a"));
        assertEquals(0, t.chaves().size());
    }

    @Test
    @DisplayName("Chave nula/vazia e capacidade inválida são rejeitadas")
    void argumentosInvalidos() {
        TabelaHash<String> t = new TabelaHash<>();
        assertThrows(IllegalArgumentException.class, () -> t.inserir(null, "x"));
        assertThrows(IllegalArgumentException.class, () -> t.buscar(""));
        assertThrows(IllegalArgumentException.class, () -> t.remover(null));
        assertThrows(IllegalArgumentException.class, () -> new TabelaHash<String>(0));
        assertNotNull(t);
    }
}
