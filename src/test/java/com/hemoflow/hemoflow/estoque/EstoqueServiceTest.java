package com.hemoflow.hemoflow.estoque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
import com.hemoflow.hemoflow.dominio.StatusRequisicao;
import com.hemoflow.hemoflow.dominio.TipoNo;
import com.hemoflow.hemoflow.dominio.TipoSanguineo;
import com.hemoflow.hemoflow.persistencia.BolsaRepository;
import com.hemoflow.hemoflow.persistencia.HospitalRepository;
import com.hemoflow.hemoflow.persistencia.NoRedeRepository;
import com.hemoflow.hemoflow.persistencia.RequisicaoRepository;

/**
 * Testes da lógica de alocação de {@link EstoqueService}: compatibilidade
 * ABO/Rh e priorização por validade (FEFO).
 *
 * <p>Todos os cenários usam {@link Hemocomponente#CRIOPRECIPITADO} combinado
 * com tipos sanguíneos que não colidem com os dados sintéticos semeados por
 * {@code DadosIniciais}, para que cada teste fique isolado da massa inicial
 * do banco (o único crioprecipitado semeado é B+, incompatível com os
 * cenários abaixo).</p>
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

    @Test
    @DisplayName("Entre bolsas compatíveis, aloca sempre a de validade mais próxima (FEFO)")
    void alocarDevePriorizarBolsaComValidadeMaisProxima() {
        NoRede local = criarNo("TESTE-LOCAL-FEFO");
        LocalDate hoje = LocalDate.now();

        Bolsa venceEm30 = bolsaRepository.save(new Bolsa(
                TipoSanguineo.O_NEG, Hemocomponente.CRIOPRECIPITADO, hoje.minusDays(1), hoje.plusDays(30), "L-30", local));
        Bolsa venceEm5 = bolsaRepository.save(new Bolsa(
                TipoSanguineo.O_NEG, Hemocomponente.CRIOPRECIPITADO, hoje.minusDays(1), hoje.plusDays(5), "L-5", local));
        Bolsa venceEm15 = bolsaRepository.save(new Bolsa(
                TipoSanguineo.O_NEG, Hemocomponente.CRIOPRECIPITADO, hoje.minusDays(1), hoje.plusDays(15), "L-15", local));

        Hospital hospital = criarHospital("TESTE-HOSP-FEFO");
        Requisicao requisicao = criarRequisicao(hospital, TipoSanguineo.O_NEG, Hemocomponente.CRIOPRECIPITADO, 1);

        List<Bolsa> alocadas = estoqueService.alocar(requisicao.getId());

        assertEquals(1, alocadas.size());
        assertEquals(venceEm5.getId(), alocadas.get(0).getId(),
                "Deveria alocar a bolsa que vence primeiro, ignorando as que vencem em 15 e 30 dias");
    }

    @Test
    @DisplayName("Não aloca bolsa incompatível, mesmo que ela vença antes da compatível")
    void alocarNaoDeveSelecionarBolsaIncompativel() {
        NoRede local = criarNo("TESTE-LOCAL-ABO");
        LocalDate hoje = LocalDate.now();

        // AB+ só é compatível com receptor AB+ — não deve ser oferecida a uma
        // requisição O+, mesmo vencendo antes da bolsa realmente compatível.
        bolsaRepository.save(new Bolsa(
                TipoSanguineo.AB_POS, Hemocomponente.CRIOPRECIPITADO, hoje.minusDays(1), hoje.plusDays(1), "L-AB", local));
        Bolsa compativel = bolsaRepository.save(new Bolsa(
                TipoSanguineo.O_NEG, Hemocomponente.CRIOPRECIPITADO, hoje.minusDays(1), hoje.plusDays(20), "L-O", local));

        Hospital hospital = criarHospital("TESTE-HOSP-ABO");
        Requisicao requisicao = criarRequisicao(hospital, TipoSanguineo.O_POS, Hemocomponente.CRIOPRECIPITADO, 1);

        List<Bolsa> alocadas = estoqueService.alocar(requisicao.getId());

        assertEquals(1, alocadas.size());
        assertEquals(compativel.getId(), alocadas.get(0).getId(),
                "A bolsa AB+ é incompatível com uma requisição O+ e não deveria ser alocada");
    }

    @Test
    @DisplayName("Marca requisição como AGUARDANDO_ESTOQUE quando não há bolsas suficientes")
    void alocarDeveMarcarAguardandoEstoqueQuandoNaoHaBolsasSuficientes() {
        Hospital hospital = criarHospital("TESTE-HOSP-VAZIO");
        Requisicao requisicao = criarRequisicao(hospital, TipoSanguineo.AB_NEG, Hemocomponente.CRIOPRECIPITADO, 1);

        assertThrows(RegraNegocioException.class, () -> estoqueService.alocar(requisicao.getId()));

        Requisicao atualizada = requisicaoRepository.findById(requisicao.getId()).orElseThrow();
        assertEquals(StatusRequisicao.AGUARDANDO_ESTOQUE, atualizada.getStatus());
    }

    @Test
    @DisplayName("Ignora bolsa vencida mesmo quando ela seria compatível")
    void alocarDeveIgnorarBolsaVencida() {
        NoRede local = criarNo("TESTE-LOCAL-VENC");
        LocalDate hoje = LocalDate.now();

        bolsaRepository.save(new Bolsa(
                TipoSanguineo.O_NEG, Hemocomponente.CRIOPRECIPITADO,
                hoje.minusDays(100), hoje.minusDays(1), "L-VENC", local));

        Hospital hospital = criarHospital("TESTE-HOSP-VENC");
        Requisicao requisicao = criarRequisicao(hospital, TipoSanguineo.O_NEG, Hemocomponente.CRIOPRECIPITADO, 1);

        assertThrows(RegraNegocioException.class, () -> estoqueService.alocar(requisicao.getId()));
    }
}
