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
import org.springframework.test.context.transaction.AfterTransaction;
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
import com.hemoflow.hemoflow.rede.RedeService;

/**
 * Testes da lógica de alocação de {@link EstoqueService}: controle de status,
 * validade, índice hash, compatibilidade, priorização FEFO e disponibilidade
 * de rota. Testes focados adicionais dessas regras ficam nas classes de teste
 * de estoque, índice e rede.
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
    @Autowired
    private RedeService redeService;
    @Autowired
    private IndiceEstoque indiceEstoque;
    @Autowired
    private ProcessadorPedidoService processadorPedidoService;

    @AfterTransaction
    void reconstruirIndiceAposRollback() {
        indiceEstoque.reconstruir(bolsaRepository.findAll().stream()
                .filter(bolsa -> bolsa.getStatus() == StatusBolsa.DISPONIVEL)
                .toList());
    }

    private NoRede criarNo(String codigo) {
        return noRedeRepository.save(new NoRede(codigo, "Nó " + codigo, TipoNo.HEMOCENTRO, -8.0630, -34.8712));
    }

    private Hospital criarHospital(String codigo) {
        NoRede no = noRedeRepository.save(
                new NoRede(codigo, "Hospital " + codigo, TipoNo.HOSPITAL, -8.0609, -34.9119));
        Hospital hospital = new Hospital("Hospital " + codigo, no);
        hospital.setLatitude(-8.0609);
        hospital.setLongitude(-34.9119);
        return hospitalRepository.save(hospital);
    }

    private Requisicao criarRequisicao(Hospital hospital, TipoSanguineo tipo, Hemocomponente componente, int quantidade) {
        return requisicaoRepository.save(
                new Requisicao(hospital, tipo, componente, quantidade, LocalDateTime.now().plusHours(6)));
    }

    private long contarDisponiveisCompativeis(TipoSanguineo tipo, Hemocomponente componente) {
        return bolsaRepository.findByStatusAndHemocomponente(StatusBolsa.DISPONIVEL, componente).stream()
                .filter(b -> b.getTipoSanguineo() == tipo)
                .filter(b -> !b.isVencida())
                .count();
    }

    @Test
    @DisplayName("Aloca uma bolsa elegível e registra o status ALOCADA")
    void alocarDeveMarcarBolsaSelecionadaComoAlocada() {
        NoRede local = criarNo("TESTE-LOCAL-ORDEM");
        LocalDate hoje = LocalDate.now();

        Bolsa primeira = bolsaRepository.save(new Bolsa(
                TipoSanguineo.O_NEG, Hemocomponente.CRIOPRECIPITADO, hoje.minusDays(1), hoje.plusDays(30), "L-1", local));
        Bolsa segunda = bolsaRepository.save(new Bolsa(
                TipoSanguineo.O_NEG, Hemocomponente.CRIOPRECIPITADO, hoje.minusDays(1), hoje.plusDays(5), "L-2", local));
        indiceEstoque.registrar(primeira);
        indiceEstoque.registrar(segunda);

        Hospital hospital = criarHospital("TESTE-HOSP-ORDEM");
        redeService.criarLigacao(local.getId(), hospital.getLocalizacao().getId(), 5);
        Requisicao requisicao = criarRequisicao(hospital, TipoSanguineo.O_NEG, Hemocomponente.CRIOPRECIPITADO, 1);

        List<Bolsa> alocadas = estoqueService.alocar(requisicao.getId());

        assertEquals(1, alocadas.size());
        assertEquals(StatusBolsa.ALOCADA, alocadas.get(0).getStatus(),
                "A bolsa escolhida deve ficar marcada como ALOCADA");
    }

    @Test
    @DisplayName("Processador executa hash, FEFO, rota e alocação no mesmo fluxo")
    void processarPedidoDeveRetornarBolsaERespectivaRota() {
        NoRede local = criarNo("PROCESSADOR-LOCAL");
        Hospital hospital = criarHospital("PROCESSADOR-HOSPITAL");
        redeService.criarLigacao(local.getId(), hospital.getLocalizacao().getId(), 11);

        LocalDate hoje = LocalDate.now();
        Bolsa bolsa = bolsaRepository.save(new Bolsa(
                TipoSanguineo.O_NEG,
                Hemocomponente.CRIOPRECIPITADO,
                hoje.minusDays(1),
                hoje.plusDays(1),
                "PROCESSADOR-FEFO",
                local));
        indiceEstoque.registrar(bolsa);
        Requisicao requisicao = criarRequisicao(
                hospital, TipoSanguineo.O_NEG, Hemocomponente.CRIOPRECIPITADO, 1);

        var resultado = processadorPedidoService.processarPedido(requisicao.getId());

        assertEquals(StatusRequisicao.ALOCADA, resultado.requisicao().getStatus());
        assertEquals(1, resultado.entregas().size());
        assertEquals(bolsa.getId(), resultado.entregas().get(0).bolsa().getId());
        assertTrue(resultado.entregas().get(0).rota().encontrada());
        assertEquals(11, resultado.entregas().get(0).rota().tempoTotalMinutos());
        assertEquals(StatusBolsa.ALOCADA, resultado.entregas().get(0).bolsa().getStatus());
    }

    @Test
    @DisplayName("Processa hemácias aplicando hash, ABO/Rh, FEFO e rota em conjunto")
    void processarHemaciasDeveFiltrarCompatibilidadePriorizarFefoECalcularRota() {
        NoRede local = criarNo("PROCESSADOR-HEMACIAS-LOCAL");
        Hospital hospital = criarHospital("PROCESSADOR-HEMACIAS-HOSPITAL");
        redeService.criarLigacao(local.getId(), hospital.getLocalizacao().getId(), 7);

        LocalDate hoje = LocalDate.now();
        Bolsa compativelMaisTardia = bolsaRepository.save(new Bolsa(
                TipoSanguineo.A_POS,
                Hemocomponente.HEMACIAS,
                hoje.minusDays(30),
                hoje.plusDays(8),
                "PROCESSADOR-HEMACIAS-FEFO-2",
                local));
        Bolsa incompativelComValidadeMaisCurta = bolsaRepository.save(new Bolsa(
                TipoSanguineo.B_NEG,
                Hemocomponente.HEMACIAS,
                hoje.minusDays(30),
                hoje,
                "PROCESSADOR-HEMACIAS-INCOMPATIVEL",
                local));
        Bolsa compativelComMenorValidade = bolsaRepository.save(new Bolsa(
                TipoSanguineo.O_NEG,
                Hemocomponente.HEMACIAS,
                hoje.minusDays(30),
                hoje.plusDays(2),
                "PROCESSADOR-HEMACIAS-FEFO-1",
                local));
        indiceEstoque.registrar(compativelMaisTardia);
        indiceEstoque.registrar(incompativelComValidadeMaisCurta);
        indiceEstoque.registrar(compativelComMenorValidade);
        Requisicao requisicao = criarRequisicao(
                hospital, TipoSanguineo.A_POS, Hemocomponente.HEMACIAS, 1);

        var resultado = processadorPedidoService.processarPedido(requisicao.getId());

        assertEquals(1, resultado.entregas().size());
        assertEquals(compativelComMenorValidade.getId(), resultado.entregas().get(0).bolsa().getId(),
                "deve escolher a bolsa compatível com menor validade, mesmo em outro grupo hash");
        assertTrue(CompatibilidadeAboRh.compativel(
                resultado.entregas().get(0).bolsa().getTipoSanguineo(), TipoSanguineo.A_POS));
        assertEquals(StatusBolsa.DISPONIVEL, incompativelComValidadeMaisCurta.getStatus(),
                "a bolsa de validade mais curta deve ser rejeitada pela matriz ABO/Rh");
        assertEquals(StatusBolsa.DISPONIVEL, compativelMaisTardia.getStatus(),
                "a bolsa compatível com validade posterior não deve ser escolhida");
        assertEquals(StatusBolsa.ALOCADA, compativelComMenorValidade.getStatus());
        assertTrue(resultado.entregas().get(0).rota().encontrada());
        assertEquals(7, resultado.entregas().get(0).rota().tempoTotalMinutos());
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