package com.hemoflow.hemoflow.rede;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
        NoRede origem = redeService.criarNo(new NoRedeDTOs.Cadastro(
                "TESTE-ORIGEM-2", "Origem 2", TipoNo.HEMOCENTRO, -8.0630, -34.8712));
        NoRede destino = redeService.criarNo(new NoRedeDTOs.Cadastro(
                "TESTE-DESTINO-2", "Destino 2", TipoNo.HOSPITAL, -8.0609, -34.9119));

        Ligacao ligacao = redeService.criarLigacao(origem.getId(), destino.getId(), 12);

        assertEquals(12, ligacao.getTempoMinutos());
        assertTrue(ligacao.getDistanciaKm() > 4 && ligacao.getDistanciaKm() < 5);
        assertTrue(redeService.listarLigacoes().stream().anyMatch(l -> l.getId().equals(ligacao.getId())));
    }

    @Test
    @DisplayName("Calcula rota pelo vizinho mais próximo e informa tempo e distância")
    void calcularRotaVizinhoMaisProximoDeveUsarGrafoPersistidoEInformarDistancia() {
        NoRede origem = redeService.criarNo(new NoRedeDTOs.Cadastro(
                "ROTA-ORIGEM", "Origem Rota", TipoNo.HEMOCENTRO, -8.0630, -34.8712));
        NoRede destino = redeService.criarNo(new NoRedeDTOs.Cadastro(
                "ROTA-DESTINO", "Destino Rota", TipoNo.HOSPITAL, -8.0609, -34.9119));
        redeService.criarLigacao(origem.getId(), destino.getId(), 17);

        var rota = redeService.calcularRotaVizinhoMaisProximo(origem.getId(), destino.getId());

        assertTrue(rota.encontrada());
        assertEquals(17, rota.tempoTotalMinutos());
        assertTrue(rota.distanciaTotalKm() > 4 && rota.distanciaTotalKm() < 5);
        assertEquals(2, rota.nos().size());
        assertEquals(origem.getId(), rota.nos().get(0).getId());
        assertEquals(destino.getId(), rota.nos().get(1).getId());
    }

    @Test
    @DisplayName("Não permite criar ligação sem coordenadas nos dois nós")
    void criarLigacaoSemCoordenadasDeveLancarExcecao() {
        NoRede origem = redeService.criarNo(new NoRedeDTOs.Cadastro("SEM-COORD-ORIGEM", "Origem", TipoNo.HEMOCENTRO));
        NoRede destino = redeService.criarNo(new NoRedeDTOs.Cadastro("SEM-COORD-DESTINO", "Destino", TipoNo.HOSPITAL));

        assertThrows(RegraNegocioException.class,
                () -> redeService.criarLigacao(origem.getId(), destino.getId(), 5));
    }

    @Test
    @DisplayName("Calcula distância geográfica em quilômetros")
    void calcularDistanciaGeograficaDeveRetornarQuilometros() {
        NoRede origem = new NoRede("ORIGEM-DISTANCIA", "Origem", TipoNo.HEMOCENTRO, 0.0, 0.0);
        NoRede destino = new NoRede("DESTINO-DISTANCIA", "Destino", TipoNo.HOSPITAL, 0.0, 1.0);

        assertEquals(111.195, com.hemoflow.hemoflow.grafo.DistanciaGeografica.calcularKm(origem, destino), 0.01);
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

    @Test
    @DisplayName("Dijkstra encontra o menor caminho ponderado no Grafo")
    void dijkstraDeveEncontrarMenorCaminhoPonderado() {
        com.hemoflow.hemoflow.grafo.Grafo grafo = new com.hemoflow.hemoflow.grafo.Grafo();
        // A -> B (peso 10)
        // A -> C (peso 2)
        // C -> B (peso 3)
        // Menor caminho A -> B deve ser A -> C -> B (custo 5), e não o direto de peso 10
        grafo.adicionarAresta("A", "B", 10, 10.0);
        grafo.adicionarAresta("A", "C", 2, 2.0);
        grafo.adicionarAresta("C", "B", 3, 3.0);

        var resultado = grafo.calcularDijkstra("A", "B");
        assertTrue(resultado.encontrado());
        assertEquals(5, resultado.tempoTotalMinutos());
        assertEquals(5.0, resultado.distanciaTotalKm(), 0.01);
        assertEquals(java.util.List.of("A", "C", "B"), resultado.rota());
    }

    @Test
    @DisplayName("calcularLatenciaECaminhosMinimos retorna lista de ligações e caminhos a partir do hemocentro")
    void calcularLatenciaECaminhosMinimosDeveRetornarRotas() {
        var dto = redeService.calcularLatenciaECaminhosMinimos();
        assertNotNull(dto);
        assertNotNull(dto.hemocentro());
        assertFalse(dto.ligacoes().isEmpty());
        assertFalse(dto.caminhosMinimos().isEmpty());
    }

    @Test
    @DisplayName("obterStatusRedeServidor retorna informações coerentes de ambiente e malha")
    void obterStatusRedeServidorDeveRetornarStatusCompleto() {
        var status = redeService.obterStatusRedeServidor();
        assertNotNull(status.uptime());
        assertTrue(status.uptimeMs() >= 0);
        assertNotNull(status.versaoJava());
        assertEquals(8080, status.porta());
        assertTrue(status.numeroNos() >= 1);
        assertTrue(status.numeroLigacoes() >= 1);
    }
}
