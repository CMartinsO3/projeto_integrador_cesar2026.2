package com.hemoflow.hemoflow.estoque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.hemoflow.hemoflow.api.RegraNegocioException;
import com.hemoflow.hemoflow.dominio.Bolsa;
import com.hemoflow.hemoflow.dominio.Hemocomponente;
import com.hemoflow.hemoflow.dominio.Hospital;
import com.hemoflow.hemoflow.dominio.NoRede;
import com.hemoflow.hemoflow.dominio.Requisicao;
import com.hemoflow.hemoflow.dominio.StatusBolsa;
import com.hemoflow.hemoflow.dominio.StatusRequisicao;
import com.hemoflow.hemoflow.dominio.TipoNo;
import com.hemoflow.hemoflow.dominio.TipoSanguineo;
import com.hemoflow.hemoflow.persistencia.BolsaRepository;
import com.hemoflow.hemoflow.persistencia.HospitalRepository;
import com.hemoflow.hemoflow.persistencia.NoRedeRepository;
import com.hemoflow.hemoflow.persistencia.RequisicaoRepository;

/**
 * Testes da lógica de alocação de {@link EstoqueService} na Unidade 1:
 * seleção simples por ordem de cadastro, controle de status e checagem de
 * validade. Compatibilidade ABO/Rh e priorização por validade (FEFO) ficam
 * reservadas para a Unidade 2 (ver {@link CompatibilidadeAboRh} e
 * {@link FilaFEFO}, já implementadas mas ainda não conectadas a
 * {@code alocar()}).
 *
 * <p>Nenhum cenário assume o conteúdo exato semeado por {@code DadosIniciais}:
 * cada teste ou usa uma quantidade solicitada inatingível para forçar
 * "estoque insuficiente", ou primeiro consulta o estoque já existente para
 * descobrir quantas bolsas compatíveis já estão disponíveis, somando a isso
 * o que o próprio teste cadastra. Assim os testes continuam válidos mesmo que
 * a massa inicial do banco mude no futuro.</p>
 */
@SpringBootTest
@Transactional
class EstoqueServiceTest {

    @Autowired
    private EstoqueService estoqueService;
    @Autowired
    private BolsaRepository bolsaRepository;
    @Autowired
    private HospitalRepository hospitalRepository;
    @Autowired
    private NoRedeRepository noRedeRepository;
    @Autowired
    private RequisicaoRepository requisicaoRepository;

    private NoRede criarNo(String codigo) {
        return noRedeRepository.save(new NoRede(codigo, "Nó " + codigo, TipoNo.HEMOCENTRO));
    }

    private Hospital criarHospital(String codigo) {
        NoRede no = noRedeRepository.save(new NoRede(codigo, "Hospital " + codigo, TipoNo.HOSPITAL));
        return hospitalRepository.save(new Hospital("Hospital " + codigo, no));
    }

    private Requisicao criarRequisicao(Hospital hospital, TipoSanguineo tipo, Hemocomponente componente, int quantidade) {
        return requisicaoRepository.save(
                new Requisicao(hospital, tipo, componente, quantidade, LocalDateTime.now().plusHours(6)));
    }

    private long contarDisponiveisCompativeis(TipoSanguineo tipo, Hemocomponente componente) {
        return bolsaRepository.findByStatusAndHemocomponente(StatusBolsa.DISPONIVEL, componente).stream()
                .filter(b -> !b.isVencida())
                .count();
    }

    @Test
    @DisplayName("Aloca bolsas disponíveis e compatíveis com o hemocomponente pedido (U1, sem FEFO/ABO-Rh)")
    void alocarDeveSelecionarBolsasDisponiveisPorOrdemDeCadastro() {
        NoRede local = criarNo("TESTE-LOCAL-ORDEM");
        LocalDate hoje = LocalDate.now();

        bolsaRepository.save(new Bolsa(
                TipoSanguineo.O_NEG, Hemocomponente.CRIOPRECIPITADO, hoje.minusDays(1), hoje.plusDays(30), "L-1", local));
        bolsaRepository.save(new Bolsa(
                TipoSanguineo.O_NEG, Hemocomponente.CRIOPRECIPITADO, hoje.minusDays(1), hoje.plusDays(5), "L-2", local));

        Hospital hospital = criarHospital("TESTE-HOSP-ORDEM");
        Requisicao requisicao = criarRequisicao(hospital, TipoSanguineo.O_NEG, Hemocomponente.CRIOPRECIPITADO, 1);

        List<Bolsa> alocadas = estoqueService.alocar(requisicao.getId());

        assertEquals(1, alocadas.size());
        assertEquals(StatusBolsa.ALOCADA, alocadas.get(0).getStatus(),
                "U1: a bolsa escolhida deve ficar marcada como ALOCADA (seleção simples, sem FEFO nem checagem de ABO/Rh)");
    }

    @Test
    @DisplayName("Marca requisição como AGUARDANDO_ESTOQUE quando não há bolsas suficientes")
    void alocarDeveMarcarAguardandoEstoqueQuandoNaoHaBolsasSuficientes() {
        Hospital hospital = criarHospital("TESTE-HOSP-VAZIO");
        // Quantidade deliberadamente inatingível, independente do que DadosIniciais tenha semeado.
        Requisicao requisicao = criarRequisicao(hospital, TipoSanguineo.AB_NEG, Hemocomponente.CRIOPRECIPITADO, 999_999);

        assertThrows(RegraNegocioException.class, () -> estoqueService.alocar(requisicao.getId()));

        Requisicao atualizada = requisicaoRepository.findById(requisicao.getId()).orElseThrow();
        assertEquals(StatusRequisicao.AGUARDANDO_ESTOQUE, atualizada.getStatus());
    }

    @Test
    @DisplayName("Ignora bolsa vencida mesmo quando ela seria compatível")
    void alocarDeveIgnorarBolsaVencida() {
        NoRede local = criarNo("TESTE-LOCAL-VENC");
        LocalDate hoje = LocalDate.now();

        // Descobre quantas bolsas já disponíveis e compatíveis existem (sejam do DadosIniciais, sejam de outro teste).
        long disponiveisAntes = contarDisponiveisCompativeis(TipoSanguineo.O_NEG, Hemocomponente.CRIOPRECIPITADO);

        bolsaRepository.save(new Bolsa(
                TipoSanguineo.O_NEG, Hemocomponente.CRIOPRECIPITADO,
                hoje.minusDays(100), hoje.minusDays(1), "L-VENC", local));

        Hospital hospital = criarHospital("TESTE-HOSP-VENC");
        // Pede uma a mais do que o estoque válido atual: só seria atendida se a bolsa vencida fosse contada (erro).
        Requisicao requisicao = criarRequisicao(
                hospital, TipoSanguineo.O_NEG, Hemocomponente.CRIOPRECIPITADO, (int) disponiveisAntes + 1);

        assertThrows(RegraNegocioException.class, () -> estoqueService.alocar(requisicao.getId()));

        Requisicao atualizada = requisicaoRepository.findById(requisicao.getId()).orElseThrow();
        assertTrue(atualizada.getStatus() == StatusRequisicao.AGUARDANDO_ESTOQUE,
                "A bolsa vencida não deve contar como disponível na alocação");
    }
}