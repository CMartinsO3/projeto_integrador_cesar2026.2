package com.hemoflow.hemoflow.service;

import com.hemoflow.hemoflow.api.dto.RequisicaoDTO;
import com.hemoflow.hemoflow.domain.entity.Hospital;
import com.hemoflow.hemoflow.domain.entity.Requisicao;
import com.hemoflow.hemoflow.domain.enums.StatusRequisicao;
import com.hemoflow.hemoflow.repository.HospitalRepository;
import com.hemoflow.hemoflow.repository.RequisicaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RequisicaoService {
    
    private final RequisicaoRepository requisicaoRepository;
    private final HospitalRepository hospitalRepository;
    
    public RequisicaoService(RequisicaoRepository requisicaoRepository,
                              HospitalRepository hospitalRepository) {
        this.requisicaoRepository = requisicaoRepository;
        this.hospitalRepository = hospitalRepository;
    }
    
    @Transactional
    public RequisicaoDTO.Resposta criar(RequisicaoDTO.Requisicao requisicao) {
        Hospital hospital = hospitalRepository.findById(requisicao.hospitalId())
                .orElseThrow(() -> new RuntimeException("Hospital não encontrado com id: " + requisicao.hospitalId()));
        
        if (!hospital.getAtivo()) {
            throw new RuntimeException("Hospital inativo não pode criar requisições");
        }
        
        Requisicao novaRequisicao = new Requisicao();
        novaRequisicao.setHospital(hospital);
        novaRequisicao.setTipoAbo(requisicao.tipoAbo());
        novaRequisicao.setFatorRh(requisicao.fatorRh());
        novaRequisicao.setTipoComponente(requisicao.tipoComponente());
        novaRequisicao.setQuantidade(requisicao.quantidade());
        novaRequisicao.setUrgencia(requisicao.urgencia());
        novaRequisicao.setPrazoLimite(requisicao.prazoLimite());
        
        Requisicao salva = requisicaoRepository.save(novaRequisicao);
        return toResposta(salva);
    }
    
    @Transactional(readOnly = true)
    public List<RequisicaoDTO.Resposta> listarTodas(StatusRequisicao status) {
        List<Requisicao> requisicoes;
        if (status != null) {
            requisicoes = requisicaoRepository.findByStatus(status);
        } else {
            requisicoes = requisicaoRepository.findAll();
        }
        
        return requisicoes.stream()
                .map(this::toResposta)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public RequisicaoDTO.Resposta buscarPorId(Long id) {
        Requisicao requisicao = requisicaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Requisição não encontrada com id: " + id));
        return toResposta(requisicao);
    }
    
    @Transactional
    public RequisicaoDTO.Resposta atualizarStatus(Long id, RequisicaoDTO.AtualizacaoStatus atualizacao) {
        Requisicao requisicao = requisicaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Requisição não encontrada com id: " + id));
        
        // Validação de transições de status
        if (requisicao.getStatus() == StatusRequisicao.ATENDIDA || 
            requisicao.getStatus() == StatusRequisicao.CANCELADA) {
            throw new RuntimeException("Requisição em status final não pode ser alterada");
        }
        
        requisicao.setStatus(atualizacao.status());
        Requisicao atualizada = requisicaoRepository.save(requisicao);
        return toResposta(atualizada);
    }
    
    @Transactional
    public void deletar(Long id) {
        Requisicao requisicao = requisicaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Requisição não encontrada com id: " + id));
        
        // Soft delete: marca como cancelada
        requisicao.setStatus(StatusRequisicao.CANCELADA);
        requisicaoRepository.save(requisicao);
    }
    
    private RequisicaoDTO.Resposta toResposta(Requisicao requisicao) {
        return new RequisicaoDTO.Resposta(
                requisicao.getId(),
                requisicao.getHospital().getId(),
                requisicao.getHospital().getNome(),
                requisicao.getTipoAbo(),
                requisicao.getFatorRh(),
                requisicao.getTipoComponente(),
                requisicao.getQuantidade(),
                requisicao.getUrgencia(),
                requisicao.getStatus(),
                requisicao.getDataSolicitacao(),
                requisicao.getPrazoLimite()
        );
    }
}
