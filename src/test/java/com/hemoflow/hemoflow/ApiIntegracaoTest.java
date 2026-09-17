package com.hemoflow.hemoflow;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class ApiIntegracaoTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void topologiaECaminhoMinimoUsamGrafoPersistido() throws Exception {
        MvcResult resultado = mockMvc.perform(get("/api/v1/topologia"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nos").isArray())
                .andExpect(jsonPath("$.ligacoes").isArray())
                .andReturn();

        JsonNode raiz = objectMapper.readTree(resultado.getResponse().getContentAsString());
        long origemId = idPorCodigo(raiz, "HC");
        long destinoId = idPorCodigo(raiz, "H3");

        mockMvc.perform(get("/api/v1/rotas/caminho-minimo")
                        .param("origemId", String.valueOf(origemId))
                        .param("destinoId", String.valueOf(destinoId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.existeRota").value(true))
                .andExpect(jsonPath("$.custoTotalMinutos").value(35))
                .andExpect(jsonPath("$.dentroDaCadeiaFria").value(true));
    }

    @Test
    void proximaBolsaUsaFefo() throws Exception {
        mockMvc.perform(get("/api/v1/estoque/proxima-bolsa"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lote").value("L-003"));
    }

    @Test
    void rejeitaBolsaComValidadeAnteriorAColeta() throws Exception {
        mockMvc.perform(get("/api/v1/nos-rede"))
                .andExpect(status().isOk());

        String corpo = """
                {
                  "tipoSanguineo": "O_POS",
                  "hemocomponente": "HEMACIAS",
                  "dataColeta": "2026-09-10",
                  "dataValidade": "2026-09-01",
                  "lote": "INV",
                  "localizacaoAtualId": 1
                }
                """;

        mockMvc.perform(post("/api/v1/bolsas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422));
    }

    private long idPorCodigo(JsonNode raiz, String codigo) {
        for (JsonNode no : raiz.get("nos")) {
            if (codigo.equals(no.get("codigo").asText())) {
                return no.get("id").asLong();
            }
        }
        throw new IllegalStateException("Nó não encontrado: " + codigo);
    }
}
