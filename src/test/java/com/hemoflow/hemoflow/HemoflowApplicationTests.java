package com.hemoflow.hemoflow;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class HemoflowApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void contextLoads() {
	}

	@Test
	void paginaDeRotasExibeConsultaEResumoDoPercurso() throws Exception {
		mockMvc.perform(get("/rotas"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Consultar rota")))
				.andExpect(content().string(containsString("/api/v1/topologia")))
				.andExpect(content().string(containsString("/api/v1/rotas/vizinho-mais-proximo")));
	}

}
