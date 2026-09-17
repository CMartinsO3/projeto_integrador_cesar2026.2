package com.hemoflow.hemoflow.estoque;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

    private final BolsaRepository bolsaRepository;
    private final NoRedeRepository noRedeRepository;
    private final HospitalRepository hospitalRepository;
    private final RequisicaoRepository requisicaoRepository;

    public EstoqueService(
            BolsaRepository bolsaRepository,
            NoRedeRepository noRedeRepository,
            HospitalRepository hospitalRepository,
            RequisicaoRepository requisicaoRepository
    ) {
        this.bolsaRepository = bolsaRepository;
        this.noRedeRepository = noRedeRepository;
        this.hospitalRepository = hospitalRepository;
        this.requisicaoRepository = requisicaoRepository;
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
        return bolsaRepository.save(bolsa);
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
        return bolsaRepository.save(bolsa);
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

        // U1: filtra apenas por hemocomponente e status DISPONIVEL
        // Compatibilidade ABO/Rh será aplicada na Unidade 2 (CompatibilidadeAboRh)
        List<Bolsa> candidatas = bolsaRepository.findByStatusAndHemocomponente(
                        StatusBolsa.DISPONIVEL,
                        requisicao.getHemocomponente()
                ).stream()
                .filter(b -> !b.isVencida())
                .toList();

        if (candidatas.size() < requisicao.getQuantidade()) {
            requisicao.setStatus(StatusRequisicao.AGUARDANDO_ESTOQUE);
            requisicaoRepository.save(requisicao);
            throw new RegraNegocioException(
                    "Estoque insuficiente: necessários " + requisicao.getQuantidade()
                            + ", disponíveis " + candidatas.size()
            );
        }

        // U1: seleção simples (as primeiras N bolsas da lista)
        List<Bolsa> alocadas = new ArrayList<>();
        for (int i = 0; i < requisicao.getQuantidade(); i++) {
            Bolsa bolsa = candidatas.get(i);
            bolsa.setStatus(StatusBolsa.ALOCADA);
            alocadas.add(bolsa);
        }

        bolsaRepository.saveAll(alocadas);
        requisicao.setStatus(StatusRequisicao.ALOCADA);
        requisicaoRepository.save(requisicao);
        return alocadas;
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
}
