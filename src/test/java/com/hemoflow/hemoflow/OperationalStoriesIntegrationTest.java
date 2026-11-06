package com.hemoflow.hemoflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.stream.StreamSupport;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.hemoflow.hemoflow.config.DemoMonitoramentoProperties;
import com.hemoflow.hemoflow.dominio.Hemocomponente;

@SpringBootTest
@AutoConfigureMockMvc
class OperationalStoriesIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DemoMonitoramentoProperties monitoramentoProperties;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void carregaConfiguracoesDidaticas() {
        assertThat(monitoramentoProperties.getLimiteRotaMinutos()).isEqualTo(120);
        assertThat(monitoramentoProperties.faixaTemperatura(Hemocomponente.HEMACIAS))
                .isEqualTo(new DemoMonitoramentoProperties.FaixaTemperatura(20, 30));
    }

    @Test
    void disponibilizaTelasOperacionais() throws Exception {
        mockMvc.perform(get("/requisicoes-hospital")).andExpect(status().isOk());
        mockMvc.perform(get("/motorista")).andExpect(status().isOk());
        mockMvc.perform(get("/monitoramento")).andExpect(status().isOk());
        mockMvc.perform(get("/rotas")).andExpect(status().isOk());
    }

    @Test
    void persisteAtualizacaoManualDeConectividadeNoPainel() throws Exception {
        MvcResult created = mockMvc.perform(post("/api/v1/nos-rede")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "codigo": "TEST-MONITOR",
                                  "nome": "Unidade de teste",
                                  "tipo": "HEMOCENTRO"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        long noId = objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(put("/api/v1/nos-rede/{id}/status-conectividade", noId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"OFFLINE\"}"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .jsonPath("$.statusConectividade").value("OFFLINE"));

        MvcResult panelResult = mockMvc.perform(get("/api/v1/monitoramento"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode devices = objectMapper.readTree(panelResult.getResponse().getContentAsString()).get("dispositivos");
        boolean foundOfflineDevice = StreamSupport.stream(devices.spliterator(), false)
                .anyMatch(device -> device.get("noRedeId").asLong() == noId
                        && device.get("status").asText().equals("OFFLINE"));
        assertThat(foundOfflineDevice).isTrue();
    }
}
