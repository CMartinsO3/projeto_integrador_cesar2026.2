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

    @Transactional(readOnly = true)
    public Bolsa proximaFefo() {
        List<Bolsa> disponiveis = bolsaRepository.findByStatus(StatusBolsa.DISPONIVEL).stream()
                .filter(b -> !b.isVencida())
                .toList();
        FilaFEFO fila = new FilaFEFO(disponiveis);
        Bolsa proxima = fila.proxima();
        if (proxima == null) {
            throw new RecursoNaoEncontradoException("Não há bolsas disponíveis para FEFO");
        }
        return proxima;
    }

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

    @Transactional(noRollbackFor = RegraNegocioException.class)
    public List<Bolsa> alocar(Long requisicaoId) {
        Requisicao requisicao = requisicaoRepository.findById(requisicaoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Requisição não encontrada: " + requisicaoId));
        if (requisicao.getStatus() != StatusRequisicao.PENDENTE
                && requisicao.getStatus() != StatusRequisicao.AGUARDANDO_ESTOQUE) {
            throw new RegraNegocioException("Requisição em status " + requisicao.getStatus() + " não pode ser alocada");
        }

        List<Bolsa> candidatas = bolsaRepository.findByStatusAndHemocomponente(
                        StatusBolsa.DISPONIVEL,
                        requisicao.getHemocomponente()
                ).stream()
                .filter(b -> !b.isVencida())
                .filter(b -> CompatibilidadeAboRh.compativel(b.getTipoSanguineo(), requisicao.getTipoSanguineo()))
                .toList();

        if (candidatas.size() < requisicao.getQuantidade()) {
            requisicao.setStatus(StatusRequisicao.AGUARDANDO_ESTOQUE);
            requisicaoRepository.save(requisicao);
            throw new RegraNegocioException(
                    "Estoque insuficiente: necessários " + requisicao.getQuantidade()
                            + ", compatíveis disponíveis " + candidatas.size()
            );
        }

        FilaFEFO fila = new FilaFEFO(candidatas);
        List<Bolsa> alocadas = new ArrayList<>();
        while (alocadas.size() < requisicao.getQuantidade()) {
            Bolsa bolsa = fila.proxima();
            bolsa.setStatus(StatusBolsa.ALOCADA);
            alocadas.add(bolsa);
        }

        bolsaRepository.saveAll(alocadas);
        requisicao.setStatus(StatusRequisicao.ALOCADA);
        requisicaoRepository.save(requisicao);
        return alocadas;
    }

    private void validarDatas(LocalDate coleta, LocalDate validade) {
        if (validade.isBefore(coleta)) {
            throw new RegraNegocioException("Validade não pode ser anterior à data de coleta");
        }
    }
}
