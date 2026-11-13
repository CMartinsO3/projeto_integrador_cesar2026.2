package com.hemoflow.hemoflow.rede;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class RedePainelIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MetricasRedeService metricasRedeService;

    @Test
    @DisplayName("GET /api/v1/rede/status retorna informações de ambiente, malha e métricas de rota")
    void statusRedeDeveRetornarInformacoesDoServidorEMalha() throws Exception {
        mockMvc.perform(get("/api/v1/rede/status"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.uptime", notNullValue()))
                .andExpect(jsonPath("$.versaoJava", notNullValue()))
                .andExpect(jsonPath("$.porta", is(8080)))
                .andExpect(jsonPath("$.perfilAtivo", notNullValue()))
                .andExpect(jsonPath("$.numeroNos", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.numeroLigacoes", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.estatisticasRotas", notNullValue()));
    }

    @Test
    @DisplayName("GET /api/v1/rede/latencia-entre-nos retorna ligações da malha e caminhos mínimos de Dijkstra")
    void latenciaEntreNosDeveRetornarLigacoesECaminhosMinimosDijkstra() throws Exception {
        mockMvc.perform(get("/api/v1/rede/latencia-entre-nos"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.hemocentro", notNullValue()))
                .andExpect(jsonPath("$.hemocentro.codigo", is("HEMOPE")))
                .andExpect(jsonPath("$.ligacoes", notNullValue()))
                .andExpect(jsonPath("$.caminhosMinimos", notNullValue()))
                .andExpect(jsonPath("$.caminhosMinimos[*].hospitalCodigo", hasItem("HR")))
                .andExpect(jsonPath("$.caminhosMinimos[0].tempoTotalMinutos", greaterThanOrEqualTo(1)));
    }

    @Test
    @DisplayName("GET /api/v1/rotas/caminho-minimo calcula o caminho ótimo por Dijkstra entre dois nós")
    void rotasCaminhoMinimoDeveCalcularDijkstraEntreDoisNos() throws Exception {
        mockMvc.perform(get("/api/v1/rotas/caminho-minimo?origemId=1&destinoId=4"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.encontrada", is(true)))
                .andExpect(jsonPath("$.rota", notNullValue()))
                .andExpect(jsonPath("$.tempoTotalMinutos", greaterThanOrEqualTo(1)));
    }

    @Test
    @DisplayName("FiltroMetricasRede registra métricas em memória com rota normalizada, p95 e status HTTP")
    void filtroMetricasRedeDeveRegistrarRequisicoesComRotaNormalizada() throws Exception {
        // Dispara requisições autenticadas para testar normalização de rota
        mockMvc.perform(get("/api/v1/nos-rede/1").sessionAttr("role", "ADMIN"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/nos-rede/1").sessionAttr("role", "ADMIN"))
                .andExpect(status().isOk());

        var estatisticas = metricasRedeService.obterEstatisticas();
        assertFalse(estatisticas.isEmpty(), "Métricas de rota devem conter registros");

        var optStat = estatisticas.stream()
                .filter(s -> s.rota().contains("/nos-rede/{id}"))
                .findFirst();

        assertTrue(optStat.isPresent(), "Deve existir estatística com rota normalizada /api/v1/nos-rede/{id}");
        var stat = optStat.get();
        assertEqualsOrGreater(2L, stat.totalRequisicoes());
        assertTrue(stat.tempoMaximoMs() >= 0);
        assertTrue(stat.p95Ms() >= 0);
        assertTrue(stat.contagemPorStatus().containsKey(200));
        assertTrue(stat.contagemPorStatus().get(200) >= 2);
    }

    @Test
    @DisplayName("Actuator expõe apenas health, info e metrics")
    void actuatorDeveExporApenasHealthInfoMetrics() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("UP")));

        mockMvc.perform(get("/actuator/info"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/actuator/metrics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.names", notNullValue()));

        mockMvc.perform(get("/actuator"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links.health", notNullValue()))
                .andExpect(jsonPath("$._links.info", notNullValue()))
                .andExpect(jsonPath("$._links.metrics", notNullValue()))
                .andExpect(jsonPath("$._links.beans").doesNotExist())
                .andExpect(jsonPath("$._links.env").doesNotExist());
    }

    private void assertEqualsOrGreater(long expectedMin, long actual) {
        assertTrue(actual >= expectedMin, () -> "Esperado pelo menos " + expectedMin + ", mas obteve " + actual);
    }
}
