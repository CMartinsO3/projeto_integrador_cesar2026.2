package com.hemoflow.hemoflow.rede;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.hemoflow.hemoflow.api.RegraNegocioException;
import com.hemoflow.hemoflow.api.dto.NoRedeDTOs;
import com.hemoflow.hemoflow.dominio.Hospital;
import com.hemoflow.hemoflow.dominio.Ligacao;
import com.hemoflow.hemoflow.dominio.NoRede;
import com.hemoflow.hemoflow.dominio.TipoNo;

@SpringBootTest
@Transactional
class RedeServiceTest {

    @Autowired
    private RedeService redeService;

    @Test
    @DisplayName("Criar nó do tipo HOSPITAL também cria o Hospital associado")
    void criarNoDoTipoHospitalDeveTambemCriarHospitalAssociado() {
        NoRede no = redeService.criarNo(new NoRedeDTOs.Cadastro("TESTE-HOSP-NOVO", "Hospital Teste", TipoNo.HOSPITAL));

        assertNotNull(no.getId());
        Hospital hospital = redeService.listarHospitais().stream()
                .filter(h -> h.getLocalizacao().getId().equals(no.getId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Hospital não foi criado junto ao nó"));
        assertEquals("Hospital Teste", hospital.getNome());
    }

    @Test
    @DisplayName("Não permite dois nós de rede com o mesmo código")
    void criarNoComCodigoDuplicadoDeveLancarExcecao() {
        redeService.criarNo(new NoRedeDTOs.Cadastro("TESTE-DUP", "Nó 1", TipoNo.HEMOCENTRO));

        assertThrows(RegraNegocioException.class, () ->
                redeService.criarNo(new NoRedeDTOs.Cadastro("TESTE-DUP", "Nó 2", TipoNo.HEMOCENTRO)));
    }

    @Test
    @DisplayName("Não permite criar ligação com tempo de viagem zero ou negativo")
    void criarLigacaoComTempoNaoPositivoDeveLancarExcecao() {
        NoRede origem = redeService.criarNo(new NoRedeDTOs.Cadastro("TESTE-ORIGEM", "Origem", TipoNo.HEMOCENTRO));
        NoRede destino = redeService.criarNo(new NoRedeDTOs.Cadastro("TESTE-DESTINO", "Destino", TipoNo.HOSPITAL));

        assertThrows(RegraNegocioException.class, () ->
                redeService.criarLigacao(origem.getId(), destino.getId(), 0));
    }

    @Test
    @DisplayName("Ligação válida é persistida e aparece na listagem")
    void criarLigacaoValidaDeveSerListada() {
        NoRede origem = redeService.criarNo(new NoRedeDTOs.Cadastro("TESTE-ORIGEM-2", "Origem 2", TipoNo.HEMOCENTRO));
        NoRede destino = redeService.criarNo(new NoRedeDTOs.Cadastro("TESTE-DESTINO-2", "Destino 2", TipoNo.HOSPITAL));

        Ligacao ligacao = redeService.criarLigacao(origem.getId(), destino.getId(), 12);

        assertEquals(12, ligacao.getTempoMinutos());
        assertTrue(redeService.listarLigacoes().stream().anyMatch(l -> l.getId().equals(ligacao.getId())));
    }

    @Test
    @DisplayName("Inativar hospital reflete em isAtivo()")
    void inativarHospitalDeveMarcarComoInativo() {
        NoRede no = redeService.criarNo(new NoRedeDTOs.Cadastro("TESTE-HOSP-INATIVO", "Hospital a inativar", TipoNo.HOSPITAL));
        Hospital hospital = redeService.listarHospitais().stream()
                .filter(h -> h.getLocalizacao().getId().equals(no.getId()))
                .findFirst()
                .orElseThrow();

        redeService.inativarHospital(hospital.getId());

        Hospital atualizado = redeService.buscarHospital(hospital.getId());
        assertTrue(!atualizado.isAtivo());
    }
}
