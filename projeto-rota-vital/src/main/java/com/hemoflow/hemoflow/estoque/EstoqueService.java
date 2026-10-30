package com.hemoflow.hemoflow.estoque;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

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
import com.hemoflow.hemoflow.rede.RedeService;

@Service
public class EstoqueService {

    private final BolsaRepository bolsaRepository;
    private final NoRedeRepository noRedeRepository;
    private final HospitalRepository hospitalRepository;
    private final RequisicaoRepository requisicaoRepository;
    private final IndiceEstoque indiceEstoque;
    private final ProcessadorPedidoService processadorPedidoService;

    @Autowired
    public EstoqueService(
            BolsaRepository bolsaRepository,
            NoRedeRepository noRedeRepository,
            HospitalRepository hospitalRepository,
            RequisicaoRepository requisicaoRepository,
            IndiceEstoque indiceEstoque,
            ProcessadorPedidoService processadorPedidoService
    ) {
        this.bolsaRepository = bolsaRepository;
        this.noRedeRepository = noRedeRepository;
        this.hospitalRepository = hospitalRepository;
        this.requisicaoRepository = requisicaoRepository;
        this.indiceEstoque = indiceEstoque;
        this.processadorPedidoService = processadorPedidoService;
    }

    public EstoqueService(
            BolsaRepository bolsaRepository,
            NoRedeRepository noRedeRepository,
            HospitalRepository hospitalRepository,
            RequisicaoRepository requisicaoRepository,
            IndiceEstoque indiceEstoque,
            RedeService redeService
    ) {
        this(bolsaRepository, noRedeRepository, hospitalRepository, requisicaoRepository, indiceEstoque,
                new ProcessadorPedidoService(requisicaoRepository, bolsaRepository, indiceEstoque, redeService));
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
        return requisicaoRepository.save(requisicao);
    }

    /**
     * Aloca bolsas compatíveis, priorizando FEFO e verificando se existe rota
     * na rede cadastrada entre a localização da bolsa e o hospital solicitante.
     */
    @Transactional(noRollbackFor = RegraNegocioException.class)
    public List<Bolsa> alocar(Long requisicaoId) {
        return processadorPedidoService.processarPedido(requisicaoId).bolsas();
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
