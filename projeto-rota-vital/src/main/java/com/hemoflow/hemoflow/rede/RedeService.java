package com.hemoflow.hemoflow.rede;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hemoflow.hemoflow.api.RecursoNaoEncontradoException;
import com.hemoflow.hemoflow.api.RegraNegocioException;
import com.hemoflow.hemoflow.api.dto.NoRedeDTOs;
import com.hemoflow.hemoflow.grafo.Grafo;
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

    /**
     * Monta o grafo a partir dos nós e ligações persistidos e executa o
     * algoritmo do vizinho mais próximo, escolhendo a ligação de menor tempo
     * entre os vizinhos ainda não visitados. As ligações são direcionadas.
     */
    @Transactional(readOnly = true)
    public RotaCalculada calcularRotaVizinhoMaisProximo(Long origemId, Long destinoId) {
        NoRede origem = buscarNo(origemId);
        NoRede destino = buscarNo(destinoId);
        Grafo grafo = new Grafo();
        List<NoRede> nos = noRedeRepository.findAll();
        Map<String, NoRede> nosPorId = new HashMap<>();
        for (NoRede no : nos) {
            String idNo = String.valueOf(no.getId());
            grafo.adicionarNo(idNo);
            nosPorId.put(idNo, no);
        }
        List<Ligacao> ligacoes = ligacaoRepository.findAll();
        Map<LigacaoIds, Double> distanciasPorLigacao = new HashMap<>();
        for (Ligacao ligacao : ligacoes) {
            Long idOrigem = ligacao.getOrigem().getId();
            Long idDestino = ligacao.getDestino().getId();
            grafo.adicionarAresta(
                    String.valueOf(idOrigem),
                    String.valueOf(idDestino),
                    ligacao.getTempoMinutos(),
                    ligacao.getDistanciaKm());
            distanciasPorLigacao.putIfAbsent(
                    new LigacaoIds(idOrigem, idDestino), ligacao.getDistanciaKm());
        }
        var resultado = grafo.calcularVizinhoMaisProximo(String.valueOf(origemId), String.valueOf(destinoId));
        List<NoRede> caminho = resultado.rota().stream()
                .map(nosPorId::get)
                .toList();
        double distanciaTotalKm = 0;
        for (int i = 1; i < caminho.size(); i++) {
            Long idOrigem = caminho.get(i - 1).getId();
            Long idDestino = caminho.get(i).getId();
            distanciaTotalKm += distanciasPorLigacao.get(new LigacaoIds(idOrigem, idDestino));
        }
        return new RotaCalculada(caminho, resultado.custoTotal(), distanciaTotalKm, resultado.encontrado());
    }

    private record LigacaoIds(Long origemId, Long destinoId) {
    }

    public record RotaCalculada(
            List<NoRede> nos, long tempoTotalMinutos, double distanciaTotalKm, boolean encontrada
    ) {
        public RotaCalculada(List<NoRede> nos, long tempoTotalMinutos, boolean encontrada) {
            this(nos, tempoTotalMinutos, 0, encontrada);
        }
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
        validarCoordenadas(dto.latitude(), dto.longitude());
        NoRede no = new NoRede(dto.codigo(), dto.nome(), dto.tipo(), dto.latitude(), dto.longitude());
        no = noRedeRepository.save(no);
        if (dto.tipo() == TipoNo.HOSPITAL) {
            Hospital hospital = new Hospital(dto.nome(), no);
            hospital.setLatitude(dto.latitude());
            hospital.setLongitude(dto.longitude());
            hospitalRepository.save(hospital);
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
        if (dto.latitude() != null || dto.longitude() != null) {
            validarCoordenadas(dto.latitude(), dto.longitude());
            salvarCoordenadas(no, dto.latitude(), dto.longitude());
        }
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
        if (origem.getLatitude() == null || origem.getLongitude() == null
                || destino.getLatitude() == null || destino.getLongitude() == null) {
            throw new RegraNegocioException("Os nós da ligação precisam ter latitude e longitude cadastradas");
        }
        return ligacaoRepository.save(new Ligacao(origem, destino, tempoMinutos));
    }

    @Transactional
    public NoRede atualizarCoordenadasNo(Long noId, Double latitude, Double longitude) {
        validarCoordenadas(latitude, longitude);
        NoRede no = buscarNo(noId);
        return salvarCoordenadas(no, latitude, longitude);
    }

    private NoRede salvarCoordenadas(NoRede no, Double latitude, Double longitude) {
        no.setLatitude(latitude);
        no.setLongitude(longitude);
        noRedeRepository.save(no);
        hospitalRepository.findByLocalizacao(no).ifPresent(hospital -> {
            hospital.setLatitude(latitude);
            hospital.setLongitude(longitude);
            hospitalRepository.save(hospital);
        });
        return no;
    }

    private void validarCoordenadas(Double latitude, Double longitude) {
        if ((latitude == null) != (longitude == null)) {
            throw new RegraNegocioException("Latitude e longitude devem ser informadas juntas");
        }
        if (latitude != null && (!Double.isFinite(latitude) || latitude < -90 || latitude > 90)) {
            throw new RegraNegocioException("Latitude deve estar entre -90 e 90");
        }
        if (longitude != null && (!Double.isFinite(longitude) || longitude < -180 || longitude > 180)) {
            throw new RegraNegocioException("Longitude deve estar entre -180 e 180");
        }
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

    @Transactional
    public Hospital criarHospital(NoRedeDTOs.Cadastro dto) {
        // Delega a criação do nó de rede + hospital à lógica existente
        NoRede no = criarNo(dto);
        return hospitalRepository.findByLocalizacao(no)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Hospital não criado para o nó: " + no.getCodigo()));
    }

    @Transactional
    public Hospital atualizarHospital(Long id, com.hemoflow.hemoflow.api.dto.HospitalDTOs.Atualizacao dto) {
        Hospital hospital = buscarHospital(id);
        hospital.setNome(dto.nome());
        hospital.setAtivo(dto.ativo());
        return hospitalRepository.save(hospital);
    }

    @Transactional
    public void inativarHospital(Long id) {
        Hospital hospital = buscarHospital(id);
        hospital.setAtivo(false);
        hospitalRepository.save(hospital);
    }

    @Transactional
    public Hospital salvarHospital(Hospital hospital) {
        return hospitalRepository.save(hospital);
    }
}
