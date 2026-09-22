package com.hemoflow.hemoflow.service;

import com.hemoflow.hemoflow.domain.entity.BolsaComponente;
import com.hemoflow.hemoflow.domain.enums.FatorRh;
import com.hemoflow.hemoflow.domain.enums.TipoABO;
import com.hemoflow.hemoflow.domain.enums.TipoComponente;
import com.hemoflow.hemoflow.domain.model.ResultadoProcessamento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes para validar equivalência entre processamento sequencial e paralelo.
 */
class ProcessamentoBolsasServiceTest {
    
    private ProcessamentoBolsasService processamentoBolsasService;
    private GeradorDadosBenchmarkService geradorDadosBenchmarkService;
    
    @BeforeEach
    void setUp() {
        processamentoBolsasService = new ProcessamentoBolsasService();
        geradorDadosBenchmarkService = new GeradorDadosBenchmarkService();
    }
    
    @Test
    @DisplayName("Versão sequencial e paralela com 2 threads devem produzir mesmo resultado")
    void testEquivalenciaSequencialParalelo2Threads() throws ExecutionException, InterruptedException {
        // Arrange
        int quantidade = 10000;
        List<BolsaComponente> bolsas = geradorDadosBenchmarkService.gerarBolsas(quantidade);
        
        TipoABO tipoReceptor = TipoABO.A;
        FatorRh fatorRhReceptor = FatorRh.POSITIVO;
        TipoComponente tipoComponente = TipoComponente.CONCENTRADO_HEMACIAS;
        
        // Act
        ResultadoProcessamento resultadoSequencial = processamentoBolsasService.processarSequencial(
                bolsas, tipoReceptor, fatorRhReceptor, tipoComponente);
        
        ResultadoProcessamento resultadoParalelo = processamentoBolsasService.processarParalelo(
                bolsas, tipoReceptor, fatorRhReceptor, tipoComponente, 2);
        
        // Assert
        assertEquals(resultadoSequencial.getTotalProcessadas(), resultadoParalelo.getTotalProcessadas(),
                "Total de bolsas processadas deve ser igual");
        
        assertEquals(resultadoSequencial.getTotalCompativeis(), resultadoParalelo.getTotalCompativeis(),
                "Total de bolsas compatíveis deve ser igual");
        
        assertEquals(resultadoSequencial.getBolsasVencidas(), resultadoParalelo.getBolsasVencidas(),
                "Número de bolsas vencidas deve ser igual");
        
        assertEquals(resultadoSequencial.getBolsasProximasVencimento(), resultadoParalelo.getBolsasProximasVencimento(),
                "Número de bolsas próximas ao vencimento deve ser igual");
        
        // Validar ordenação (lista deve estar na mesma ordem)
        List<BolsaComponente> listaSeq = resultadoSequencial.getBolsasPriorizadas();
        List<BolsaComponente> listaPar = resultadoParalelo.getBolsasPriorizadas();
        
        assertEquals(listaSeq.size(), listaPar.size(), "Listas devem ter mesmo tamanho");
        
        for (int i = 0; i < listaSeq.size(); i++) {
            assertEquals(listaSeq.get(i).getCodigoRastreio(), listaPar.get(i).getCodigoRastreio(),
                    "Bolsas devem estar na mesma ordem (FEFO)");
        }
    }
    
    @Test
    @DisplayName("Versão paralela com 4 threads deve produzir mesmo resultado que sequencial")
    void testEquivalenciaSequencialParalelo4Threads() throws ExecutionException, InterruptedException {
        // Arrange
        int quantidade = 50000;
        List<BolsaComponente> bolsas = geradorDadosBenchmarkService.gerarBolsas(quantidade);
        
        TipoABO tipoReceptor = TipoABO.O;
        FatorRh fatorRhReceptor = FatorRh.NEGATIVO;
        TipoComponente tipoComponente = TipoComponente.PLASMA_FRESCO;
        
        // Act
        ResultadoProcessamento resultadoSequencial = processamentoBolsasService.processarSequencial(
                bolsas, tipoReceptor, fatorRhReceptor, tipoComponente);
        
        ResultadoProcessamento resultadoParalelo = processamentoBolsasService.processarParalelo(
                bolsas, tipoReceptor, fatorRhReceptor, tipoComponente, 4);
        
        // Assert
        validarEquivalencia(resultadoSequencial, resultadoParalelo);
    }
    
    @Test
    @DisplayName("Versão paralela com 8 threads deve produzir mesmo resultado que sequencial")
    void testEquivalenciaSequencialParalelo8Threads() throws ExecutionException, InterruptedException {
        // Arrange
        int quantidade = 100000;
        List<BolsaComponente> bolsas = geradorDadosBenchmarkService.gerarBolsas(quantidade);
        
        TipoABO tipoReceptor = TipoABO.AB;
        FatorRh fatorRhReceptor = FatorRh.POSITIVO;
        TipoComponente tipoComponente = TipoComponente.CONCENTRADO_PLAQUETAS;
        
        // Act
        ResultadoProcessamento resultadoSequencial = processamentoBolsasService.processarSequencial(
                bolsas, tipoReceptor, fatorRhReceptor, tipoComponente);
        
        ResultadoProcessamento resultadoParalelo = processamentoBolsasService.processarParalelo(
                bolsas, tipoReceptor, fatorRhReceptor, tipoComponente, 8);
        
        // Assert
        validarEquivalencia(resultadoSequencial, resultadoParalelo);
    }
    
    @Test
    @DisplayName("Processamento sequencial deve funcionar com lista vazia")
    void testProcessamentoSequencialListaVazia() {
        // Arrange
        List<BolsaComponente> bolsasVazias = List.of();
        
        // Act
        ResultadoProcessamento resultado = processamentoBolsasService.processarSequencial(
                bolsasVazias, TipoABO.A, FatorRh.POSITIVO, TipoComponente.CONCENTRADO_HEMACIAS);
        
        // Assert
        assertEquals(0, resultado.getTotalProcessadas());
        assertEquals(0, resultado.getTotalCompativeis());
        assertTrue(resultado.getBolsasPriorizadas().isEmpty());
    }
    
    @Test
    @DisplayName("Processamento paralelo deve funcionar com lista vazia")
    void testProcessamentoParaleloListaVazia() throws ExecutionException, InterruptedException {
        // Arrange
        List<BolsaComponente> bolsasVazias = List.of();
        
        // Act
        ResultadoProcessamento resultado = processamentoBolsasService.processarParalelo(
                bolsasVazias, TipoABO.A, FatorRh.POSITIVO, TipoComponente.CONCENTRADO_HEMACIAS, 4);
        
        // Assert
        assertEquals(0, resultado.getTotalProcessadas());
        assertEquals(0, resultado.getTotalCompativeis());
        assertTrue(resultado.getBolsasPriorizadas().isEmpty());
    }
    
    @Test
    @DisplayName("Gerador de dados deve produzir quantidade correta de bolsas")
    void testGeradorDadosQuantidadeCorreta() {
        // Arrange & Act
        List<BolsaComponente> bolsas = geradorDadosBenchmarkService.gerarBolsas(1000);
        
        // Assert
        assertEquals(1000, bolsas.size());
        
        // Verificar que todas as bolsas têm dados válidos
        for (BolsaComponente bolsa : bolsas) {
            assertNotNull(bolsa.getTipoAbo());
            assertNotNull(bolsa.getFatorRh());
            assertNotNull(bolsa.getTipoComponente());
            assertNotNull(bolsa.getDataProducao());
            assertNotNull(bolsa.getDataValidade());
            assertTrue(bolsa.getVolumeMl() > 0);
        }
    }
    
    @Test
    @DisplayName("Gerador de dados deve ser determinístico")
    void testGeradorDadosDeterministico() {
        // Act
        List<BolsaComponente> bolsas1 = geradorDadosBenchmarkService.gerarBolsas(100);
        List<BolsaComponente> bolsas2 = geradorDadosBenchmarkService.gerarBolsas(100);
        
        // Assert
        assertEquals(bolsas1.size(), bolsas2.size());
        
        for (int i = 0; i < bolsas1.size(); i++) {
            assertEquals(bolsas1.get(i).getTipoAbo(), bolsas2.get(i).getTipoAbo(),
                    "Tipos ABO devem ser iguais na mesma posição");
            assertEquals(bolsas1.get(i).getFatorRh(), bolsas2.get(i).getFatorRh(),
                    "Fatores Rh devem ser iguais na mesma posição");
            assertEquals(bolsas1.get(i).getTipoComponente(), bolsas2.get(i).getTipoComponente(),
                    "Tipos de componente devem ser iguais na mesma posição");
        }
    }
    
    /**
     * Método auxiliar para validar equivalência entre resultados.
     */
    private void validarEquivalencia(ResultadoProcessamento seq, ResultadoProcessamento par) {
        assertEquals(seq.getTotalProcessadas(), par.getTotalProcessadas(),
                "Total processado deve ser igual");
        assertEquals(seq.getTotalCompativeis(), par.getTotalCompativeis(),
                "Total compatíveis deve ser igual");
        assertEquals(seq.getBolsasVencidas(), par.getBolsasVencidas(),
                "Bolsas vencidas deve ser igual");
        assertEquals(seq.getBolsasProximasVencimento(), par.getBolsasProximasVencimento(),
                "Bolsas próximas vencimento deve ser igual");
        
        // Validar ordenação
        List<BolsaComponente> listaSeq = seq.getBolsasPriorizadas();
        List<BolsaComponente> listaPar = par.getBolsasPriorizadas();
        
        assertEquals(listaSeq.size(), listaPar.size(), "Tamanho das listas deve ser igual");
        
        for (int i = 0; i < Math.min(listaSeq.size(), 100); i++) { // Validar primeiros 100
            assertEquals(listaSeq.get(i).getDataValidade(), listaPar.get(i).getDataValidade(),
                    "Datas de validade devem estar na mesma ordem");
        }
    }
}
