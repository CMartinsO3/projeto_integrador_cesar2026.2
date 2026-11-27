package com.hemoflow.hemoflow.estoque;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hemoflow.hemoflow.api.RecursoNaoEncontradoException;
import com.hemoflow.hemoflow.api.RegraNegocioException;
import com.hemoflow.hemoflow.dominio.Bolsa;
import com.hemoflow.hemoflow.dominio.Hospital;
import com.hemoflow.hemoflow.dominio.NoRede;
import com.hemoflow.hemoflow.dominio.Requisicao;
import com.hemoflow.hemoflow.dominio.StatusBolsa;
import com.hemoflow.hemoflow.dominio.StatusRequisicao;
import com.hemoflow.hemoflow.persistencia.BolsaRepository;
import com.hemoflow.hemoflow.persistencia.HospitalRepository;
import com.hemoflow.hemoflow.persistencia.NoRedeRepository;
import com.hemoflow.hemoflow.persistencia.RequisicaoRepository;

@Service
public class EstoqueService {

    private static final Logger log = LoggerFactory.getLogger(EstoqueService.class);

    private final BolsaRepository bolsaRepository;
    private final NoRedeRepository noRedeRepository;
    private final HospitalRepository hospitalRepository;
    private final RequisicaoRepository requisicaoRepository;
    private final IndiceEstoque indiceEstoque;
    private final ObservationRegistry observationRegistry;

    // ===== Métricas de negócio (Micrometer) =====
    private final Counter requisicoesCriadasCounter;
    private final Counter bolsasCadastradasCounter;
    private final Timer alocacaoTimer;

    public EstoqueService(
            BolsaRepository bolsaRepository,
            NoRedeRepository noRedeRepository,
            HospitalRepository hospitalRepository,
            RequisicaoRepository requisicaoRepository,
            IndiceEstoque indiceEstoque,
            MeterRegistry meterRegistry
    ) {
        this(bolsaRepository, noRedeRepository, hospitalRepository, requisicaoRepository,
                indiceEstoque, meterRegistry, ObservationRegistry.NOOP);
    }

    @Autowired
    public EstoqueService(
            BolsaRepository bolsaRepository,
            NoRedeRepository noRedeRepository,
            HospitalRepository hospitalRepository,
            RequisicaoRepository requisicaoRepository,
            IndiceEstoque indiceEstoque,
            MeterRegistry meterRegistry,
            ObservationRegistry observationRegistry
    ) {
        this.bolsaRepository = bolsaRepository;
        this.noRedeRepository = noRedeRepository;
        this.hospitalRepository = hospitalRepository;
        this.requisicaoRepository = requisicaoRepository;
        this.indiceEstoque = indiceEstoque;
        this.observationRegistry = observationRegistry != null ? observationRegistry : ObservationRegistry.NOOP;

        // Registra métricas de negócio
        this.requisicoesCriadasCounter = Counter.builder("hemoflow.requisicoes.criadas")
                .description("Total de requisições de sangue criadas")
                .tag("application", "hemoflow")
                .register(meterRegistry);
        this.bolsasCadastradasCounter = Counter.builder("hemoflow.bolsas.cadastradas")
                .description("Total de bolsas de sangue cadastradas no sistema")
                .tag("application", "hemoflow")
                .register(meterRegistry);
        this.alocacaoTimer = Timer.builder("hemoflow.alocacao.duracao")
                .description("Tempo de execução do algoritmo de alocação FEFO")
                .tag("application", "hemoflow")
                .register(meterRegistry);
    }

    // -------------------------------------------------------------------------
    // Bolsas
    // -------------------------------------------------------------------------

    @Transactional
    public Bolsa criarBolsa(com.hemoflow.hemoflow.api.dto.BolsaDTOs.RequisicaoCadastro dto) {
        validarDatas(dto.dataColeta(), dto.dataValidade());
        NoRede local = noRedeRepository.findById(dto.localizacaoAtualId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Localização não encontrada: " + dto.localizacaoAtualId()));
        Bolsa bolsa = new Bolsa(
                dto.tipoSanguineo(),
                dto.hemocomponente(),
                dto.dataColeta(),
                dto.dataValidade(),
                dto.lote(),
                local
        );
        Bolsa salva = bolsaRepository.save(bolsa);
        indiceEstoque.registrarAposCommit(salva);
        bolsasCadastradasCounter.increment(); // métrica de negócio
        log.info("Bolsa cadastrada: id={}, tipo={}, componente={}, lote={}",
                salva.getId(), salva.getTipoSanguineo(), salva.getHemocomponente(), salva.getLote());
        return salva;
    }

    @Transactional(readOnly = true)
    public List<Bolsa> listar(StatusBolsa status) {
        if (status == null) {
            return bolsaRepository.findAll();
        }
        return bolsaRepository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public Bolsa buscar(Long id) {
        return bolsaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Bolsa não encontrada: " + id));
    }

    @Transactional
    public Bolsa transicionarStatusBolsa(Long id, StatusBolsa novoStatus) {
        Bolsa bolsa = buscar(id);
        validarTransicaoBolsa(bolsa.getStatus(), novoStatus);
        bolsa.setStatus(novoStatus);
        Bolsa salva = bolsaRepository.save(bolsa);
        // toda transição parte de DISPONIVEL ou de um status posterior: a bolsa sai do índice
        indiceEstoque.retirarAposCommit(salva);
        return salva;
    }

    // -------------------------------------------------------------------------
    // Requisições
    // -------------------------------------------------------------------------

    @Transactional
    public Requisicao criarRequisicao(com.hemoflow.hemoflow.api.dto.RequisicaoDTOs.Cadastro dto) {
        return Observation.createNotStarted("hemoflow.requisicao.criar", observationRegistry)
                .observe(() -> {
                    Hospital hospital = hospitalRepository.findById(dto.hospitalId())
                            .orElseThrow(() -> new RecursoNaoEncontradoException("Hospital não encontrado: " + dto.hospitalId()));
                    if (!hospital.isAtivo()) {
                        throw new RegraNegocioException("Hospital inativo não pode abrir requisição");
                    }
                    if (dto.quantidade() < 1) {
                        throw new RegraNegocioException("Quantidade deve ser pelo menos 1");
                    }
                    Requisicao requisicao = new Requisicao(
                            hospital,
                            dto.tipoSanguineo(),
                            dto.hemocomponente(),
                            dto.quantidade(),
                            dto.janelaEntrega()
                    );
                    Requisicao salva = requisicaoRepository.save(requisicao);
                    requisicoesCriadasCounter.increment(); // métrica de negócio
                    log.info("Requisicao criada: id={}, hospitalId={}, tipo={}, componente={}, qtd={}",
                            salva.getId(), hospital.getId(), salva.getTipoSanguineo(),
                            salva.getHemocomponente(), salva.getQuantidade());
                    return salva;
                });
    }

    /**
     * Aloca bolsas disponíveis para uma requisição pendente.
     *
     * <p>U1: seleção por ordem de cadastro (sem FEFO nem compatibilidade ABO/Rh).
     * Essas regras serão incorporadas na Unidade 2.</p>
     */
    @Transactional(noRollbackFor = RegraNegocioException.class)
    public List<Bolsa> alocar(Long requisicaoId) {
        Requisicao requisicao = requisicaoRepository.findById(requisicaoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Requisição não encontrada: " + requisicaoId));
        if (requisicao.getStatus() != StatusRequisicao.PENDENTE
                && requisicao.getStatus() != StatusRequisicao.AGUARDANDO_ESTOQUE) {
            throw new RegraNegocioException("Requisição em status " + requisicao.getStatus() + " não pode ser alocada");
        }

        List<Bolsa> candidatas = Observation.createNotStarted("hemoflow.estoque.buscar-candidatas", observationRegistry)
                .observe(() -> {
                    // Bolsas que venceram enquanto esperavam saem do índice
                    indiceEstoque.descartarVencidas();

                    // U1: filtra apenas por hemocomponente e status DISPONIVEL
                    return bolsaRepository.findByStatusAndHemocomponente(
                                    StatusBolsa.DISPONIVEL,
                                    requisicao.getHemocomponente()
                            ).stream()
                            .filter(b -> !b.isVencida())
                            .toList();
                });

        if (candidatas.size() < requisicao.getQuantidade()) {
            requisicao.setStatus(StatusRequisicao.AGUARDANDO_ESTOQUE);
            requisicaoRepository.save(requisicao);
            log.warn("Estoque insuficiente para requisicao {}: necessarios={}, disponiveis={}",
                    requisicaoId, requisicao.getQuantidade(), candidatas.size());
            throw new RegraNegocioException(
                    "Estoque insuficiente: necessários " + requisicao.getQuantidade()
                            + ", disponíveis " + candidatas.size()
            );
        }

        // U1: seleção simples (as primeiras N bolsas da lista)
        // Timer de negócio + span dedicado envolvem a fase de seleção e persistência
        return Observation.createNotStarted("hemoflow.estoque.alocar-bolsas", observationRegistry)
                .observe(() -> alocacaoTimer.record(() -> {
                    List<Bolsa> alocadas = new ArrayList<>();
                    for (int i = 0; i < requisicao.getQuantidade(); i++) {
                        Bolsa bolsa = candidatas.get(i);
                        bolsa.setStatus(StatusBolsa.ALOCADA);
                        alocadas.add(bolsa);
                    }
                    bolsaRepository.saveAll(alocadas);
                    alocadas.forEach(indiceEstoque::retirarAposCommit);
                    requisicao.setStatus(StatusRequisicao.ALOCADA);
                    requisicaoRepository.save(requisicao);
                    log.info("Alocacao concluida: requisicaoId={}, bolsasAlocadas={}", requisicaoId, alocadas.size());
                    return alocadas;
                }));
    }

    @Transactional
    public Requisicao transicionarStatusRequisicao(Long id, StatusRequisicao novoStatus) {
        Requisicao requisicao = requisicaoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Requisição não encontrada: " + id));
        validarTransicaoRequisicao(requisicao.getStatus(), novoStatus);
        requisicao.setStatus(novoStatus);
        return requisicaoRepository.save(requisicao);
    }

    // -------------------------------------------------------------------------
    // Máquinas de estado
    // -------------------------------------------------------------------------

    private void validarTransicaoBolsa(StatusBolsa atual, StatusBolsa novo) {
        boolean valida = switch (atual) {
            case DISPONIVEL  -> novo == StatusBolsa.ALOCADA    || novo == StatusBolsa.DESCARTADA;
            case ALOCADA     -> novo == StatusBolsa.EM_TRANSITO || novo == StatusBolsa.DESCARTADA;
            case EM_TRANSITO -> novo == StatusBolsa.ENTREGUE   || novo == StatusBolsa.DESCARTADA;
            case ENTREGUE, DESCARTADA -> false;
        };
        if (!valida) {
            throw new RegraNegocioException(
                    "Transição inválida para bolsa: " + atual + " → " + novo
                            + ". Permitidas a partir de " + atual + ": "
                            + transicoesPossiveisBolsa(atual)
            );
        }
    }

    private void validarTransicaoRequisicao(StatusRequisicao atual, StatusRequisicao novo) {
        boolean valida = switch (atual) {
            case PENDENTE           -> novo == StatusRequisicao.AGUARDANDO_ESTOQUE
                                       || novo == StatusRequisicao.CANCELADA;
            case AGUARDANDO_ESTOQUE -> novo == StatusRequisicao.CANCELADA;
            case ALOCADA            -> novo == StatusRequisicao.EM_TRANSITO
                                       || novo == StatusRequisicao.CANCELADA;
            case EM_TRANSITO        -> novo == StatusRequisicao.ENTREGUE;
            case ENTREGUE, CANCELADA -> false;
        };
        if (!valida) {
            throw new RegraNegocioException(
                    "Transição inválida para requisição: " + atual + " → " + novo
                            + ". Permitidas a partir de " + atual + ": "
                            + transicoesPossiveisRequisicao(atual)
            );
        }
    }

    private String transicoesPossiveisBolsa(StatusBolsa atual) {
        return switch (atual) {
            case DISPONIVEL  -> "[ALOCADA, DESCARTADA]";
            case ALOCADA     -> "[EM_TRANSITO, DESCARTADA]";
            case EM_TRANSITO -> "[ENTREGUE, DESCARTADA]";
            case ENTREGUE, DESCARTADA -> "nenhuma (status terminal)";
        };
    }

    private String transicoesPossiveisRequisicao(StatusRequisicao atual) {
        return switch (atual) {
            case PENDENTE           -> "[AGUARDANDO_ESTOQUE, CANCELADA]";
            case AGUARDANDO_ESTOQUE -> "[CANCELADA]";
            case ALOCADA            -> "[EM_TRANSITO, CANCELADA]";
            case EM_TRANSITO        -> "[ENTREGUE]";
            case ENTREGUE, CANCELADA -> "nenhuma (status terminal)";
        };
    }

    // -------------------------------------------------------------------------
    // Validações
    // -------------------------------------------------------------------------

    private void validarDatas(LocalDate coleta, LocalDate validade) {
        if (validade.isBefore(coleta)) {
            throw new RegraNegocioException("Validade não pode ser anterior à data de coleta");
        }
    }

    // -------------------------------------------------------------------------
    // Entrada / Saida simplificadas (painel web)
    // -------------------------------------------------------------------------

    @Transactional
    public Map<String, Object> registrarEntrada(com.hemoflow.hemoflow.dominio.TipoSanguineo tipo,
            com.hemoflow.hemoflow.dominio.Hemocomponente hemo,
            LocalDate dataColeta, LocalDate dataValidade, String lote, Long noRedeId) {
        NoRede no = noRedeRepository.findById(noRedeId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Nó de rede não encontrado: " + noRedeId));
        Bolsa b = new Bolsa(tipo, hemo, dataColeta, dataValidade, lote, no);
        bolsaRepository.save(b);
        indiceEstoque.registrarAposCommit(b);
        return Map.of("id", b.getId(), "tipoSanguineo", tipo.name(), "status", "DISPONIVEL", "lote", lote);
    }

    @Transactional
    public Map<String, Object> registrarSaida(Long bolsaId, String motivo) {
        Bolsa b = bolsaRepository.findById(bolsaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Bolsa não encontrada: " + bolsaId));
        if (b.getStatus() == StatusBolsa.ENTREGUE || b.getStatus() == StatusBolsa.DESCARTADA) {
            throw new RegraNegocioException("Bolsa já se encontra em status terminal: " + b.getStatus());
        }
        StatusBolsa novoStatus = "DESCARTE".equals(motivo) ? StatusBolsa.DESCARTADA : StatusBolsa.ENTREGUE;
        b.setStatus(novoStatus);
        bolsaRepository.save(b);
        indiceEstoque.retirarAposCommit(b);
        return Map.of("id", b.getId(), "status", novoStatus.name(), "motivo", motivo);
    }

    public Map<String, Object> gerarEstoqueMock(Long hospitalId) {
        Random rnd = new Random(hospitalId);
        Map<String, Integer> estoque = new LinkedHashMap<>();
        for (com.hemoflow.hemoflow.dominio.TipoSanguineo ts : com.hemoflow.hemoflow.dominio.TipoSanguineo.values()) {
            estoque.put(ts.name(), rnd.nextInt(20));
        }
        return Map.of("hospitalId", hospitalId, "estoque", estoque);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> alertasVencimento(int diasLimite) {
        LocalDate limite = LocalDate.now().plusDays(diasLimite);
        return bolsaRepository.findProximasDoVencimento(limite).stream().map(b -> {
            long diasRestantes = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), b.getDataValidade());
            java.util.LinkedHashMap<String, Object> m = new java.util.LinkedHashMap<>();
            m.put("id", b.getId());
            m.put("lote", b.getLote());
            m.put("tipoSanguineo", b.getTipoSanguineo().name());
            m.put("hemocomponente", b.getHemocomponente().name());
            m.put("dataValidade", b.getDataValidade().toString());
            m.put("diasRestantes", diasRestantes);
            m.put("urgencia", diasRestantes <= 1 ? "CRITICO" : diasRestantes <= 3 ? "ALTO" : "MEDIO");
            return (Map<String, Object>) m;
        }).toList();
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> bolsasEmTransito() {
        return bolsaRepository.findByStatusOrderByDataValidadeAsc(StatusBolsa.EM_TRANSITO).stream().map(b -> {
            java.util.LinkedHashMap<String, Object> m = new java.util.LinkedHashMap<>();
            m.put("id", b.getId());
            m.put("lote", b.getLote());
            m.put("tipoSanguineo", b.getTipoSanguineo().name());
            m.put("hemocomponente", b.getHemocomponente().name());
            m.put("dataValidade", b.getDataValidade().toString());
            m.put("local", b.getLocalizacaoAtual().getNome());
            return (Map<String, Object>) m;
        }).toList();
    }
}

