package com.hemoflow.hemoflow.estoque;

import com.hemoflow.hemoflow.api.RegraNegocioException;
import com.hemoflow.hemoflow.dominio.*;
import com.hemoflow.hemoflow.persistencia.BolsaRepository;
import com.hemoflow.hemoflow.persistencia.HospitalRepository;
import com.hemoflow.hemoflow.persistencia.NoRedeRepository;
import com.hemoflow.hemoflow.persistencia.RequisicaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class EstoquePersistenceIntegrationTest {

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

    @BeforeEach
    void limparBanco() {
        requisicaoRepository.deleteAll();
        bolsaRepository.deleteAll();
        hospitalRepository.deleteAll();
    }

    @Test
    void devePersistirStatusAguardandoEstoqueQuandoNaoHouverBolsasSuficientesNoBanco() {
        NoRede hospitalNo = noRedeRepository.save(
                new NoRede("INT-HOSP", "Hospital Integracao", TipoNo.HOSPITAL));
        Hospital hospital = hospitalRepository.save(new Hospital("Hospital Integracao", hospitalNo));

        NoRede estoqueNo = new NoRede("INT-ESTOQUE", "Estoque Integracao", TipoNo.HEMOCENTRO);
        com.hemoflow.hemoflow.dominio.Bolsa bolsaDisponivel = new com.hemoflow.hemoflow.dominio.Bolsa(
                TipoSanguineo.AB_POS,
                Hemocomponente.HEMACIAS,
                LocalDate.now().minusDays(2),
                LocalDate.now().plusDays(15),
                "BOLSA-INT-AB",
                noRedeRepository.save(estoqueNo));
        bolsaRepository.save(bolsaDisponivel);

        Requisicao req = requisicaoRepository.save(new Requisicao(
                hospital,
                TipoSanguineo.O_NEG,
                Hemocomponente.HEMACIAS,
                999_999,
                LocalDateTime.now().plusHours(6)));

        Long reqId = req.getId();
        assertThrows(RegraNegocioException.class, () -> estoqueService.alocar(reqId));

        Requisicao salvaNoBanco = requisicaoRepository.findById(reqId).orElseThrow();
        assertEquals(StatusRequisicao.AGUARDANDO_ESTOQUE, salvaNoBanco.getStatus());
    }
}