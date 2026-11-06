package com.hemoflow.hemoflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.StreamSupport;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.hemoflow.hemoflow.config.DemoMonitoramentoProperties;
import com.hemoflow.hemoflow.dominio.Hemocomponente;
import com.hemoflow.hemoflow.dominio.ContaOperacional;
import com.hemoflow.hemoflow.dominio.Entrega;
import com.hemoflow.hemoflow.dominio.Bolsa;
import com.hemoflow.hemoflow.dominio.Requisicao;
import com.hemoflow.hemoflow.dominio.TipoSanguineo;
import com.hemoflow.hemoflow.dominio.PerfilOperacional;
import com.hemoflow.hemoflow.dominio.StatusConectividade;
import com.hemoflow.hemoflow.dominio.StatusBolsa;
import com.hemoflow.hemoflow.dominio.StatusRequisicao;
import com.hemoflow.hemoflow.persistencia.BolsaRepository;
import com.hemoflow.hemoflow.persistencia.ContaOperacionalRepository;
import com.hemoflow.hemoflow.persistencia.HospitalRepository;
import com.hemoflow.hemoflow.persistencia.NoRedeRepository;
import com.hemoflow.hemoflow.persistencia.RequisicaoRepository;
import com.hemoflow.hemoflow.rede.ConectividadeTimeoutService;

@SpringBootTest
@AutoConfigureMockMvc
class OperationalStoriesIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DemoMonitoramentoProperties monitoramentoProperties;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BolsaRepository bolsaRepository;

    @Autowired
    private HospitalRepository hospitalRepository;

    @Autowired
    private ContaOperacionalRepository contaOperacionalRepository;

    @Autowired
    private RequisicaoRepository requisicaoRepository;

    @Autowired
    private NoRedeRepository noRedeRepository;

    @Autowired
    private ConectividadeTimeoutService conectividadeTimeoutService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void carregaConfiguracoesDidaticas() {
        assertThat(monitoramentoProperties.getLimiteRotaMinutos()).isEqualTo(120);
        assertThat(monitoramentoProperties.faixaTemperatura(Hemocomponente.HEMACIAS))
                .isEqualTo(new DemoMonitoramentoProperties.FaixaTemperatura(20, 30));
    }

    @Test
    void disponibilizaTelasOperacionais() throws Exception {
        mockMvc.perform(get("/requisicoes-hospital")).andExpect(status().isFound());
        mockMvc.perform(get("/motorista")).andExpect(status().isFound());
        mockMvc.perform(get("/monitoramento")).andExpect(status().isFound());
        mockMvc.perform(get("/rotas")).andExpect(status().isOk());
    }

    @Test
    void restringePaginaDeContasOperacionaisAoAdministrador() throws Exception {
        mockMvc.perform(get("/contas-operacionais"))
                .andExpect(status().isFound())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .header().string("Location", "/login-admin"));

        mockMvc.perform(get("/contas-operacionais").sessionAttr("role", "HOSPITAL"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/contas-operacionais").sessionAttr("role", "ADMIN"))
                .andExpect(status().isOk());
    }

    @Test
    void motoristaPodeCriarPropriaContaERecebeAcessoApenasComoMotorista() throws Exception {
        MvcResult cadastro = mockMvc.perform(post("/api/v1/auth/cadastro-operacional")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "usuario": "motorista-autocadastro",
                                  "senha": "motoristaSeguro123",
                                  "nome": "Motorista Autocadastro",
                                  "perfil": "MOTORISTA"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .jsonPath("$.redirect").value("/motorista"))
                .andReturn();

        ContaOperacional conta = contaOperacionalRepository.findByUsuario("motorista-autocadastro").orElseThrow();
        assertThat(conta.isAprovado()).isTrue();
        assertThat(passwordEncoder.matches("motoristaSeguro123", conta.getSenhaHash())).isTrue();
        MockHttpSession session = (MockHttpSession) cadastro.getRequest().getSession(false);
        assertThat(session).isNotNull();
        assertThat(session.getAttribute("role")).isEqualTo("MOTORISTA");

        mockMvc.perform(get("/api/v1/entregas").session(session))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .jsonPath("$").isEmpty());
    }

    @Test
    void cadastroHospitalAguardaAprovacaoAdministrativaAntesDoLogin() throws Exception {
        long hospitalId = hospitalRepository.findAll().get(0).getId();
        mockMvc.perform(post("/api/v1/auth/cadastro-operacional")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "usuario": "hospital-autocadastro",
                                  "senha": "hospitalSeguro123",
                                  "nome": "Hospital Autocadastro",
                                  "perfil": "HOSPITAL",
                                  "hospitalId": %d
                                }
                                """.formatted(hospitalId)))
                .andExpect(status().isAccepted())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .jsonPath("$.pendente").value(true));

        ContaOperacional conta = contaOperacionalRepository.findByUsuario("hospital-autocadastro").orElseThrow();
        assertThat(conta.isAprovado()).isFalse();
        mockMvc.perform(post("/api/v1/auth/login-operacional")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"usuario":"hospital-autocadastro","senha":"hospitalSeguro123","perfil":"HOSPITAL"}
                                """))
                .andExpect(status().isForbidden())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .jsonPath("$.erro").value("Cadastro aguardando aprovação da equipe clínica"));

        mockMvc.perform(post("/api/v1/auth/usuarios/" + conta.getId() + "/aprovar")
                        .sessionAttr("role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .jsonPath("$.aprovado").value(true));

        mockMvc.perform(post("/api/v1/auth/login-operacional")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"usuario":"hospital-autocadastro","senha":"hospitalSeguro123","perfil":"HOSPITAL"}
                                """))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .jsonPath("$.redirect").value("/requisicoes-hospital"));
    }

    @Test
    void alertaTemperaturaSomenteForaDosLimitesConfigurados() throws Exception {
        long bolsaId = bolsaRepository.findByStatus(StatusBolsa.EM_TRANSITO).get(0).getId();

        mockMvc.perform(post("/api/v1/monitoramento/leituras")
                        .sessionAttr("role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bolsaId\":" + bolsaId + ",\"temperaturaCelsius\":20.0}"))
                .andExpect(status().isCreated())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .jsonPath("$.alerta").value(false));

        mockMvc.perform(post("/api/v1/monitoramento/leituras")
                        .sessionAttr("role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bolsaId\":" + bolsaId + ",\"temperaturaCelsius\":30.0}"))
                .andExpect(status().isCreated())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .jsonPath("$.alerta").value(false));

        mockMvc.perform(post("/api/v1/monitoramento/leituras")
                        .sessionAttr("role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bolsaId\":" + bolsaId + ",\"temperaturaCelsius\":19.9}"))
                .andExpect(status().isCreated())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .jsonPath("$.alerta").value(true));

        mockMvc.perform(post("/api/v1/monitoramento/leituras")
                        .sessionAttr("role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bolsaId\":" + bolsaId + ",\"temperaturaCelsius\":30.1}"))
                .andExpect(status().isCreated())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .jsonPath("$.alerta").value(true));
    }

    @Test
    void contaHospitalPersistidaUsaSenhaHashEIsolaRequisicoes() throws Exception {
        var hospitais = hospitalRepository.findAll();
        long hospitalDaConta = requisicaoRepository.findAll().get(0).getHospital().getId();
        long hospitalDiferente = hospitais.stream()
                .mapToLong(hospital -> hospital.getId())
                .filter(id -> id != hospitalDaConta)
                .findFirst().orElseThrow();
        var hospitalDiferenteEntity = hospitalRepository.findById(hospitalDiferente).orElseThrow();
        requisicaoRepository.save(new Requisicao(
                hospitalDiferenteEntity,
                TipoSanguineo.O_POS,
                Hemocomponente.PLASMA,
                1,
                LocalDateTime.now().plusHours(5)
        ));
        String senha = "hospitalSeguro123";

        MvcResult contaResult = mockMvc.perform(post("/api/v1/auth/usuarios")
                        .sessionAttr("role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "usuario": "hospital-teste",
                                  "senha": "hospitalSeguro123",
                                  "nome": "Conta Hospital Teste",
                                  "perfil": "HOSPITAL",
                                  "hospitalId": %d
                                }
                                """.formatted(hospitalDaConta)))
                .andExpect(status().isOk())
                .andReturn();
        ContaOperacional conta = contaOperacionalRepository.findByUsuario("hospital-teste").orElseThrow();
        assertThat(conta.getSenhaHash()).isNotEqualTo(senha);
        assertThat(passwordEncoder.matches(senha, conta.getSenhaHash())).isTrue();

        mockMvc.perform(post("/api/v1/auth/login-operacional")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"usuario":"hospital-teste","senha":"hospitalSeguro123","perfil":"MOTORISTA"}
                                """))
                .andExpect(status().isUnauthorized());

        MvcResult login = mockMvc.perform(post("/api/v1/auth/login-operacional")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"usuario":"hospital-teste","senha":"hospitalSeguro123","perfil":"HOSPITAL"}
                                """))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .jsonPath("$.redirect").value("/requisicoes-hospital"))
                .andReturn();
        MockHttpSession session = (MockHttpSession) login.getRequest().getSession(false);

        String janelaEntrega = LocalDateTime.now().plusHours(4).withNano(0).toString();
        MvcResult created = mockMvc.perform(post("/api/v1/requisicoes")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "hospitalId": %d,
                                  "tipoSanguineo": "O_POS",
                                  "hemocomponente": "HEMACIAS",
                                  "quantidade": 1,
                                  "janelaEntrega": "%s"
                                }
                                """.formatted(hospitalDiferente, janelaEntrega)))
                .andExpect(status().isCreated())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .jsonPath("$.hospitalId").value(hospitalDaConta))
                .andReturn();
        long createdId = objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asLong();

        MvcResult listed = mockMvc.perform(get("/api/v1/requisicoes?hospitalId=" + hospitalDiferente)
                        .session(session))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode requests = objectMapper.readTree(listed.getResponse().getContentAsString());
        assertThat(java.util.stream.IntStream.range(0, requests.size())
                .mapToObj(requests::get)
                .anyMatch(request -> request.get("id").asLong() == createdId
                        && request.get("hospitalId").asLong() == hospitalDaConta)).isTrue();

        long requisicaoDeOutroHospital = requisicaoRepository.findAll().stream()
                .filter(requisicao -> !requisicao.getHospital().getId().equals(hospitalDaConta))
                .findFirst().orElseThrow().getId();
        mockMvc.perform(get("/api/v1/requisicoes/{id}", requisicaoDeOutroHospital).session(session))
                .andExpect(status().isForbidden());
    }

    @Test
    void motoristaConsultaSomenteEntregaAtribuida() throws Exception {
        Requisicao requisicao = new Requisicao(
                hospitalRepository.findAll().get(0),
                TipoSanguineo.O_NEG,
                Hemocomponente.HEMACIAS,
                1,
                LocalDateTime.now().plusHours(4)
        );
        requisicao.setStatus(StatusRequisicao.ALOCADA);
        Bolsa bolsa = bolsaRepository.findByStatusAndHemocomponente(
                        StatusBolsa.DISPONIVEL, Hemocomponente.HEMACIAS)
                .get(0);
        Entrega entrega = new Entrega(
                requisicao,
                bolsa,
                List.of(bolsa.getLocalizacaoAtual(), requisicao.getHospital().getLocalizacao()),
                5,
                0,
                false
        );
        requisicao.adicionarEntrega(entrega);
        requisicaoRepository.save(requisicao);
        MvcResult contaResult = mockMvc.perform(post("/api/v1/auth/usuarios")
                        .sessionAttr("role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "usuario": "motorista-teste",
                                  "senha": "motoristaSeguro123",
                                  "nome": "Motorista Teste",
                                  "perfil": "MOTORISTA"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();
        long motoristaId = objectMapper.readTree(contaResult.getResponse().getContentAsString())
                .get("id").asLong();

        mockMvc.perform(put("/api/v1/entregas/{id}/motorista", entrega.getId())
                        .sessionAttr("role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motoristaId\":" + motoristaId + "}"))
                .andExpect(status().isOk());
        MvcResult login = mockMvc.perform(post("/api/v1/auth/login-operacional")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"usuario":"motorista-teste","senha":"motoristaSeguro123","perfil":"MOTORISTA"}
                                """))
                .andExpect(status().isOk())
                .andReturn();
        MockHttpSession session = (MockHttpSession) login.getRequest().getSession(false);

        MvcResult result = mockMvc.perform(get("/api/v1/entregas").session(session))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode deliveries = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(deliveries.size()).isEqualTo(1);
        assertThat(deliveries.get(0).get("entregaId").asLong()).isEqualTo(entrega.getId());
    }

    @Test
    void heartbeatRenovaConectividadeETimeoutMarcaOffline() throws Exception {
        var no = noRedeRepository.findAll().get(0);
        no.atualizarStatusConectividade(StatusConectividade.ONLINE);
        ReflectionTestUtils.setField(no, "atualizadoStatusEm", LocalDateTime.now().minusSeconds(121));
        noRedeRepository.save(no);

        conectividadeTimeoutService.verificarTimeouts();
        assertThat(noRedeRepository.findById(no.getId()).orElseThrow().getStatusConectividade())
                .isEqualTo(StatusConectividade.OFFLINE);

        mockMvc.perform(post("/api/v1/nos-rede/{id}/heartbeat", no.getId())
                        .sessionAttr("role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .jsonPath("$.statusConectividade").value("ONLINE"));

        mockMvc.perform(put("/api/v1/nos-rede/{id}/status-conectividade", no.getId())
                        .sessionAttr("role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"OFFLINE\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/nos-rede/{id}/heartbeat", no.getId())
                        .sessionAttr("role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .jsonPath("$.statusConectividade").value("OFFLINE"));
    }

    @Test
    void persisteAtualizacaoManualDeConectividadeNoPainel() throws Exception {
        MvcResult created = mockMvc.perform(post("/api/v1/nos-rede")
                        .sessionAttr("role", "ADMIN")
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
                        .sessionAttr("role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"OFFLINE\"}"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .jsonPath("$.statusConectividade").value("OFFLINE"));

        MvcResult panelResult = mockMvc.perform(get("/api/v1/monitoramento").sessionAttr("role", "ADMIN"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode devices = objectMapper.readTree(panelResult.getResponse().getContentAsString()).get("dispositivos");
        boolean foundOfflineDevice = StreamSupport.stream(devices.spliterator(), false)
                .anyMatch(device -> device.get("noRedeId").asLong() == noId
                        && device.get("status").asText().equals("OFFLINE"));
        assertThat(foundOfflineDevice).isTrue();
    }
}
