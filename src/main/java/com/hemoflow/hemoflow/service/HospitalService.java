package com.hemoflow.hemoflow.service;

import com.hemoflow.hemoflow.api.dto.HospitalDTO;
import com.hemoflow.hemoflow.domain.entity.Hospital;
import com.hemoflow.hemoflow.repository.HospitalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HospitalService {
    
    private final HospitalRepository hospitalRepository;
    
    public HospitalService(HospitalRepository hospitalRepository) {
        this.hospitalRepository = hospitalRepository;
    }
    
    @Transactional
    public HospitalDTO.Resposta criar(HospitalDTO.Requisicao requisicao) {
        Hospital hospital = new Hospital();
        hospital.setNome(requisicao.nome());
        hospital.setLocalizacao(requisicao.localizacao());
        
        Hospital salvo = hospitalRepository.save(hospital);
        return toResposta(salvo);
    }
    
    @Transactional(readOnly = true)
    public List<HospitalDTO.Resposta> listarTodos() {
        return hospitalRepository.findAll()
                .stream()
                .map(this::toResposta)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public HospitalDTO.Resposta buscarPorId(Long id) {
        Hospital hospital = hospitalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hospital não encontrado com id: " + id));
        return toResposta(hospital);
    }
    
    @Transactional
    public HospitalDTO.Resposta atualizar(Long id, HospitalDTO.Requisicao requisicao) {
        Hospital hospital = hospitalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hospital não encontrado com id: " + id));
        
        hospital.setNome(requisicao.nome());
        hospital.setLocalizacao(requisicao.localizacao());
        
        Hospital atualizado = hospitalRepository.save(hospital);
        return toResposta(atualizado);
    }
    
    @Transactional
    public void deletar(Long id) {
        Hospital hospital = hospitalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hospital não encontrado com id: " + id));
        
        // Soft delete
        hospital.setAtivo(false);
        hospitalRepository.save(hospital);
    }
    
    private HospitalDTO.Resposta toResposta(Hospital hospital) {
        return new HospitalDTO.Resposta(
                hospital.getId(),
                hospital.getNome(),
                hospital.getLocalizacao(),
                hospital.getAtivo(),
                hospital.getDataCadastro().toString()
        );
    }
}
