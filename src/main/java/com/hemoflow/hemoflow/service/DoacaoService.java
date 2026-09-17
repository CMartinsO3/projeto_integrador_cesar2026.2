package com.hemoflow.hemoflow.service;

import com.hemoflow.hemoflow.api.dto.DoacaoDTO;
import com.hemoflow.hemoflow.domain.entity.Doacao;
import com.hemoflow.hemoflow.repository.DoacaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DoacaoService {
    
    private final DoacaoRepository doacaoRepository;
    
    public DoacaoService(DoacaoRepository doacaoRepository) {
        this.doacaoRepository = doacaoRepository;
    }
    
    @Transactional
    public DoacaoDTO.Resposta criar(DoacaoDTO.Requisicao requisicao) {
        Doacao doacao = new Doacao();
        doacao.setTipoAbo(requisicao.tipoAbo());
        doacao.setFatorRh(requisicao.fatorRh());
        doacao.setDataColeta(requisicao.dataColeta());
        doacao.setVolumeColetadoMl(requisicao.volumeColetadoMl());
        doacao.setCentroColetaId(requisicao.centroColetaId());
        doacao.setCentroColetaNome(requisicao.centroColetaNome());
        
        Doacao salva = doacaoRepository.save(doacao);
        return toResposta(salva);
    }
    
    @Transactional(readOnly = true)
    public List<DoacaoDTO.Resposta> listarTodas() {
        return doacaoRepository.findAll()
                .stream()
                .map(this::toResposta)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public DoacaoDTO.Resposta buscarPorId(Long id) {
        Doacao doacao = doacaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Doação não encontrada com id: " + id));
        return toResposta(doacao);
    }
    
    private DoacaoDTO.Resposta toResposta(Doacao doacao) {
        return new DoacaoDTO.Resposta(
                doacao.getId(),
                doacao.getTipoAbo(),
                doacao.getFatorRh(),
                doacao.getDataColeta(),
                doacao.getVolumeColetadoMl(),
                doacao.getCentroColetaId(),
                doacao.getCentroColetaNome()
        );
    }
}
