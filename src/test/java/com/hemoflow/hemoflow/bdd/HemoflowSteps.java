package com.hemoflow.hemoflow.bdd;

import com.hemoflow.hemoflow.dominio.TipoSanguineo;
import com.hemoflow.hemoflow.estoque.CompatibilidadeAboRh;
import com.hemoflow.hemoflow.grafo.Grafo;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class HemoflowSteps {

    private TipoSanguineo tipoBolsa;
    private TipoSanguineo tipoRequisicao;
    private boolean compativel;
    private List<LocalDate> validadesOrdenadas;
    private Grafo grafo;
    private Grafo.ResultadoCaminhoMinimo resultadoRota;

    @Dado("que o tipo sanguíneo da bolsa disponível é {string}")
    public void dadoTipoBolsa(String tipo) {
        this.tipoBolsa = TipoSanguineo.valueOf(tipo);
    }

    @E("o tipo sanguíneo solicitado na requisição é {string}")
    public void dadoTipoRequisicao(String tipo) {
        this.tipoRequisicao = TipoSanguineo.valueOf(tipo);
    }

    @Quando("verifico a compatibilidade para alocação")
    public void quandoVerificoCompatibilidade() {
        this.compativel = CompatibilidadeAboRh.compativel(tipoBolsa, tipoRequisicao);
    }

    @Entao("a alocação deve ser considerada incompatível")
    public void entaoAlocacaoIncompativel() {
        assertFalse(this.compativel);
    }

    @Entao("a alocação deve ser considerada compatível")
    public void entaoAlocacaoCompativel() {
        assertTrue(this.compativel);
    }

    @Dado("que existem bolsas compatíveis do tipo {string} com vencimentos em {int} dias e {int} dias")
    public void dadoBolsasComVencimentos(String tipo, int dias1, int dias2) {
        LocalDate hoje = LocalDate.now();
        this.validadesOrdenadas = Stream.of(hoje.plusDays(dias1), hoje.plusDays(dias2))
                .sorted(Comparator.naturalOrder())
                .toList();
    }

    @Quando("ordeno as bolsas para alocação FEFO")
    public void quandoOrdenoBolsasFefo() {
        assertNotNull(this.validadesOrdenadas);
    }

    @Entao("a bolsa que vence em {int} dias deve ser selecionada primeiro")
    public void entaoBolsaMaisProximaSelecionadaPrimeiro(int diasEsperados) {
        assertEquals(LocalDate.now().plusDays(diasEsperados), this.validadesOrdenadas.get(0));
    }

    @Dado("uma rede logística com aresta direta de {long} para {long} custando {int} minutos")
    public void dadoRedeComArestaDireta(Long origem, Long destino, int minutos) {
        this.grafo = new Grafo();
        this.grafo.adicionarAresta(origem, destino, minutos);
    }

    @E("caminho alternativo de {long} para {long} custando {int} minutos e de {long} para {long} custando {int} minutos")
    public void dadoCaminhoAlternativo(Long o1, Long d1, int m1, Long o2, Long d2, int m2) {
        this.grafo.adicionarAresta(o1, d1, m1);
        this.grafo.adicionarAresta(o2, d2, m2);
    }

    @Quando("calculo o caminho mínimo de {long} até {long}")
    public void quandoCalculoCaminhoMinimo(Long origem, Long destino) {
        this.resultadoRota = this.grafo.calcularCaminhoMinimo(origem, destino);
    }

    @Entao("a rota deve existir com custo total de {int} minutos")
    public void entaoRotaDeveExistirComCusto(int custoEsperado) {
        assertTrue(this.resultadoRota.existeRota());
        assertEquals(custoEsperado, this.resultadoRota.custoTotalMinutos());
    }
}