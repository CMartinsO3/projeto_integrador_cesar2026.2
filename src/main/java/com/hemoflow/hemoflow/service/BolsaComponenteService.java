package com.hemoflow.hemoflow.service;

import com.hemoflow.hemoflow.api.dto.BolsaComponenteDTO;
import com.hemoflow.hemoflow.domain.entity.BolsaComponente;
import com.hemoflow.hemoflow.domain.entity.Doacao;
import com.hemoflow.hemoflow.domain.enums.StatusBolsa;
import com.hemoflow.hemoflow.repository.BolsaComponenteRepository;
import com.hemoflow.hemoflow.repository.DoacaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BolsaComponenteService {
    
    private final BolsaComponenteRepository bolsaComponenteRepository;
    private final DoacaoRepository doacaoRepository;
    
    public BolsaComponenteService(BolsaComponenteRepository bolsaComponenteRepository,
                                   DoacaoRepository doacaoRepository) {
        this.bolsaComponenteRepository = bolsaComponenteRepository;
        this.doacaoRepository = doacaoRepository;
    }
    
    @Transactional
    public BolsaComponenteDTO.Resposta criar(BolsaComponenteDTO.Requisicao requisicao) {
        BolsaComponente bolsa = new BolsaComponente();
        
        if (requisicao.doacaoId() != null) {
            Doacao doacao = doacaoRepository.findById(requisicao.doacaoId())
                    .orElseThrow(() -> new RuntimeException("Doação não encontrada com id: " + requisicao.doacaoId()));
            bolsa.setDoacao(doacao);
        }
        
        bolsa.setTipoComponente(requisicao.tipoComponente());
        bolsa.setTipoAbo(requisicao.tipoAbo());
        bolsa.setFatorRh(requisicao.fatorRh());
        bolsa.setVolumeMl(requisicao.volumeMl());
        bolsa.setDataProducao(requisicao.dataProducao());
        bolsa.setLocalizacaoAtualId(requisicao.localizacaoAtualId());
        
        BolsaComponente salva = bolsaComponenteRepository.save(bolsa);
        return toResposta(salva);
    }
    
    @Transactional(readOnly = true)
    public List<BolsaComponenteDTO.Resposta> listarTodas(StatusBolsa status) {
        List<BolsaComponente> bolsas;
        if (status != null) {
            bolsas = bolsaComponenteRepository.findByStatus(status);
        } else {
            bolsas = bolsaComponenteRepository.findAll();
        }
        
        return bolsas.stream()
                .map(this::toResposta)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public BolsaComponenteDTO.Resposta buscarPorId(Long id) {
        BolsaComponente bolsa = bolsaComponenteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bolsa não encontrada com id: " + id));
        return toResposta(bolsa);
    }
    
    @Transactional
    public BolsaComponenteDTO.Resposta atualizarStatus(Long id, BolsaComponenteDTO.AtualizacaoStatus atualizacao) {
        BolsaComponente bolsa = bolsaComponenteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bolsa não encontrada com id: " + id));
        
        // Validação de transição de status
        if (bolsa.isVencida() && atualizacao.status() != StatusBolsa.DESCARTADA) {
            throw new RuntimeException("Bolsa vencida só pode ser movida para DESCARTADA");
        }
        
        bolsa.setStatus(atualizacao.status());
        BolsaComponente atualizada = bolsaComponenteRepository.save(bolsa);
        return toResposta(atualizada);
    }
    
    @Transactional
    public void deletar(Long id) {
        BolsaComponente bolsa = bolsaComponenteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bolsa não encontrada com id: " + id));
        bolsaComponenteRepository.delete(bolsa);
    }
    
    private BolsaComponenteDTO.Resposta toResposta(BolsaComponente bolsa) {
        return new BolsaComponenteDTO.Resposta(
                bolsa.getId(),
                bolsa.getCodigoRastreio(),
                bolsa.getDoacao() != null ? bolsa.getDoacao().getId() : null,
                bolsa.getTipoComponente(),
                bolsa.getTipoAbo(),
                bolsa.getFatorRh(),
                bolsa.getVolumeMl(),
                bolsa.getDataProducao(),
                bolsa.getDataValidade(),
                bolsa.getStatus(),
                bolsa.getLocalizacaoAtualId(),
                bolsa.getNomeLocalizacao(),
                bolsa.isVencida(),
                bolsa.diasParaVencimento()
        );
    }
}
