package com.hemoflow.hemoflow.estoque;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hemoflow.hemoflow.api.RecursoNaoEncontradoException;
import com.hemoflow.hemoflow.api.RegraNegocioException;
import com.hemoflow.hemoflow.api.dto.EntregaDTOs;
import com.hemoflow.hemoflow.api.dto.NoRedeDTOs;
import com.hemoflow.hemoflow.dominio.ContaOperacional;
import com.hemoflow.hemoflow.dominio.Entrega;
import com.hemoflow.hemoflow.dominio.NoRede;
import com.hemoflow.hemoflow.dominio.PerfilOperacional;
import com.hemoflow.hemoflow.dominio.StatusRequisicao;
import com.hemoflow.hemoflow.persistencia.ContaOperacionalRepository;
import com.hemoflow.hemoflow.persistencia.EntregaRepository;
import com.hemoflow.hemoflow.persistencia.LeituraTemperaturaRepository;
import com.hemoflow.hemoflow.persistencia.NoRedeRepository;
import com.hemoflow.hemoflow.persistencia.RequisicaoRepository;

@Service
public class EntregaService {

    private final EntregaRepository entregaRepository;
    private final RequisicaoRepository requisicaoRepository;
    private final NoRedeRepository noRedeRepository;
    private final LeituraTemperaturaRepository leituraRepository;
    private final EstoqueService estoqueService;
    private final ContaOperacionalRepository contaRepository;

    public EntregaService(
            EntregaRepository entregaRepository,
            RequisicaoRepository requisicaoRepository,
            NoRedeRepository noRedeRepository,
            LeituraTemperaturaRepository leituraRepository,
            EstoqueService estoqueService,
            ContaOperacionalRepository contaRepository
    ) {
        this.entregaRepository = entregaRepository;
        this.requisicaoRepository = requisicaoRepository;
        this.noRedeRepository = noRedeRepository;
        this.leituraRepository = leituraRepository;
        this.estoqueService = estoqueService;
        this.contaRepository = contaRepository;
    }

    @Transactional(readOnly = true)
    public List<EntregaDTOs.Resposta> listarEmAcompanhamento(Long motoristaId) {
        return requisicaoRepository.findByStatusInOrderByDataSolicitacaoDesc(
                        List.of(StatusRequisicao.ALOCADA, StatusRequisicao.EM_TRANSITO))
                .stream()
                .flatMap(requisicao -> requisicao.getEntregas().stream())
                .filter(entrega -> motoristaId == null
                        || entrega.getMotorista() != null
                        && entrega.getMotorista().getId().equals(motoristaId))
                .map(this::resposta)
                .toList();
    }

    @Transactional
    public void atualizarStatus(Long requisicaoId, StatusRequisicao novoStatus, Long motoristaId) {
        if (novoStatus != StatusRequisicao.EM_TRANSITO && novoStatus != StatusRequisicao.ENTREGUE) {
            throw new RegraNegocioException("Motorista só pode iniciar ou concluir uma entrega");
        }
        if (motoristaId != null) {
            validarAtribuicaoDaRequisicao(requisicaoId, motoristaId);
        }
        estoqueService.transicionarStatusRequisicao(requisicaoId, novoStatus);
    }

    @Transactional
    public EntregaDTOs.Resposta avancarProximoPonto(Long entregaId, Long motoristaId) {
        Entrega entrega = entregaRepository.findById(entregaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Entrega não encontrada: " + entregaId));
        if (motoristaId != null) {
            validarAtribuicao(entrega, motoristaId);
        }
        if (entrega.getRequisicao().getStatus() != StatusRequisicao.EM_TRANSITO) {
            throw new RegraNegocioException("Só é possível avançar uma entrega que esteja em trânsito");
        }
        try {
            entrega.avancarProximoNo();
        } catch (IllegalStateException exception) {
            throw new RegraNegocioException(exception.getMessage());
        }
        return resposta(entregaRepository.save(entrega));
    }

    @Transactional
    public Entrega atribuirMotorista(Long entregaId, Long motoristaId) {
        Entrega entrega = entregaRepository.findById(entregaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Entrega não encontrada: " + entregaId));
        ContaOperacional motorista = contaRepository.findById(motoristaId)
                .filter(conta -> conta.getPerfil() == PerfilOperacional.MOTORISTA)
                .orElseThrow(() -> new RegraNegocioException("Conta de motorista não encontrada: " + motoristaId));
        entrega.atribuirMotorista(motorista);
        return entregaRepository.save(entrega);
    }

    private void validarAtribuicaoDaRequisicao(Long requisicaoId, Long motoristaId) {
        var requisicao = requisicaoRepository.findById(requisicaoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Requisição não encontrada: " + requisicaoId));
        if (requisicao.getEntregas().isEmpty()
                || requisicao.getEntregas().stream().anyMatch(entrega ->
                        entrega.getMotorista() == null
                        || !entrega.getMotorista().getId().equals(motoristaId))) {
            throw new RegraNegocioException("A requisição não está atribuída a este motorista");
        }
    }

    private void validarAtribuicao(Entrega entrega, Long motoristaId) {
        if (entrega.getMotorista() == null || !entrega.getMotorista().getId().equals(motoristaId)) {
            throw new RegraNegocioException("A entrega não está atribuída a este motorista");
        }
    }

    private EntregaDTOs.Resposta resposta(Entrega entrega) {
        List<Long> idsRota = entrega.getRotaNoIds();
        Map<Long, NoRede> nosPorId = noRedeRepository.findAllById(idsRota).stream()
                .collect(Collectors.toMap(NoRede::getId, Function.identity()));
        List<NoRedeDTOs.Resposta> rota = idsRota.stream()
                .map(id -> {
                    NoRede no = nosPorId.get(id);
                    if (no == null) {
                        throw new RecursoNaoEncontradoException("Ponto da rota não encontrado: " + id);
                    }
                    return NoRedeDTOs.Resposta.de(no);
                })
                .toList();
        var leitura = leituraRepository.findTopByBolsaIdOrderByRegistradaEmDesc(entrega.getBolsa().getId());
        return new EntregaDTOs.Resposta(
                entrega.getId(),
                entrega.getRequisicao().getId(),
                entrega.getRequisicao().getStatus(),
                entrega.getBolsa().getId(),
                entrega.getBolsa().getLote(),
                entrega.getBolsa().getHemocomponente(),
                entrega.getBolsa().getTipoSanguineo(),
                entrega.getRequisicao().getHospital().getNome(),
                entrega.getRequisicao().getJanelaEntrega(),
                rota,
                entrega.getIndiceProximoNo(),
                entrega.getTempoEstimadoMinutos(),
                entrega.getPrevisaoChegada(),
                entrega.getDistanciaEstimadaKm(),
                entrega.isAlertaCadeiaFria(),
                leitura.map(item -> item.getTemperaturaCelsius()).orElse(null),
                leitura.map(item -> item.isAlerta()).orElse(false),
                leitura.map(item -> item.getRegistradaEm()).orElse(null)
        );
    }
}
