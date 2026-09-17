package com.hemoflow.hemoflow.rede;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hemoflow.hemoflow.api.RecursoNaoEncontradoException;
import com.hemoflow.hemoflow.api.RegraNegocioException;
import com.hemoflow.hemoflow.api.dto.NoRedeDTOs;
import com.hemoflow.hemoflow.dominio.Hospital;
import com.hemoflow.hemoflow.dominio.Ligacao;
import com.hemoflow.hemoflow.dominio.NoRede;
import com.hemoflow.hemoflow.dominio.TipoNo;
import com.hemoflow.hemoflow.persistencia.HospitalRepository;
import com.hemoflow.hemoflow.persistencia.LigacaoRepository;
import com.hemoflow.hemoflow.persistencia.NoRedeRepository;

@Service
public class RedeService {

    private final NoRedeRepository noRedeRepository;
    private final LigacaoRepository ligacaoRepository;
    private final HospitalRepository hospitalRepository;

    public RedeService(
            NoRedeRepository noRedeRepository,
            LigacaoRepository ligacaoRepository,
            HospitalRepository hospitalRepository
    ) {
        this.noRedeRepository = noRedeRepository;
        this.ligacaoRepository = ligacaoRepository;
        this.hospitalRepository = hospitalRepository;
    }

    @Transactional(readOnly = true)
    public List<NoRede> listarNos() {
        return noRedeRepository.findAll();
    }

    @Transactional(readOnly = true)
    public NoRede buscarNo(Long id) {
        return noRedeRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Nó não encontrado: " + id));
    }

    @Transactional
    public NoRede criarNo(NoRedeDTOs.Cadastro dto) {
        if (noRedeRepository.existsByCodigo(dto.codigo())) {
            throw new RegraNegocioException("Já existe nó com código " + dto.codigo());
        }
        NoRede no = new NoRede(dto.codigo(), dto.nome(), dto.tipo());
        no = noRedeRepository.save(no);
        if (dto.tipo() == TipoNo.HOSPITAL) {
            hospitalRepository.save(new Hospital(dto.nome(), no));
        }
        return no;
    }

    @Transactional
    public NoRede atualizarNo(Long id, NoRedeDTOs.Cadastro dto) {
        NoRede no = buscarNo(id);
        noRedeRepository.findByCodigo(dto.codigo())
                .filter(existente -> !existente.getId().equals(id))
                .ifPresent(existente -> {
                    throw new RegraNegocioException("Já existe nó com código " + dto.codigo());
                });
        no.setCodigo(dto.codigo());
        no.setNome(dto.nome());
        no.setTipo(dto.tipo());
        return noRedeRepository.save(no);
    }

    @Transactional
    public void removerNo(Long id) {
        NoRede no = buscarNo(id);
        noRedeRepository.delete(no);
    }

    @Transactional(readOnly = true)
    public List<Ligacao> listarLigacoes() {
        return ligacaoRepository.findAll();
    }

    @Transactional
    public Ligacao criarLigacao(Long origemId, Long destinoId, int tempoMinutos) {
        if (tempoMinutos <= 0) {
            throw new RegraNegocioException("Tempo da ligação deve ser positivo");
        }
        NoRede origem = buscarNo(origemId);
        NoRede destino = buscarNo(destinoId);
        return ligacaoRepository.save(new Ligacao(origem, destino, tempoMinutos));
    }

    @Transactional(readOnly = true)
    public List<Hospital> listarHospitais() {
        return hospitalRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Hospital buscarHospital(Long id) {
        return hospitalRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Hospital não encontrado: " + id));
    }
}
