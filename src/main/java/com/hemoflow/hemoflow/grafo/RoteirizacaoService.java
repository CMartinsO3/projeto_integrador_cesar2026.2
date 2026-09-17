package com.hemoflow.hemoflow.grafo;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hemoflow.hemoflow.api.RecursoNaoEncontradoException;
import com.hemoflow.hemoflow.api.RegraNegocioException;
import com.hemoflow.hemoflow.dominio.Hemocomponente;
import com.hemoflow.hemoflow.dominio.Ligacao;
import com.hemoflow.hemoflow.dominio.NoRede;
import com.hemoflow.hemoflow.persistencia.LigacaoRepository;
import com.hemoflow.hemoflow.persistencia.NoRedeRepository;

@Service
public class RoteirizacaoService {

    private final NoRedeRepository noRedeRepository;
    private final LigacaoRepository ligacaoRepository;

    public RoteirizacaoService(NoRedeRepository noRedeRepository, LigacaoRepository ligacaoRepository) {
        this.noRedeRepository = noRedeRepository;
        this.ligacaoRepository = ligacaoRepository;
    }

    @Transactional(readOnly = true)
    public Grafo montarGrafo() {
        Grafo grafo = new Grafo();
        for (NoRede no : noRedeRepository.findAll()) {
            grafo.adicionarNo(no.getCodigo());
        }
        List<Ligacao> ligacoes = ligacaoRepository.findAll();
        for (Ligacao ligacao : ligacoes) {
            grafo.adicionarAresta(
                    ligacao.getOrigem().getCodigo(),
                    ligacao.getDestino().getCodigo(),
                    ligacao.getTempoMinutos(),
                    true
            );
        }
        return grafo;
    }

    @Transactional(readOnly = true)
    public ResultadoRota caminhoMinimo(Long origemId, Long destinoId, boolean exigirCadeiaFria, Hemocomponente hemocomponente) {
        NoRede origem = noRedeRepository.findById(origemId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Nó de origem não encontrado: " + origemId));
        NoRede destino = noRedeRepository.findById(destinoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Nó de destino não encontrado: " + destinoId));

        ResultadoCaminho resultado = montarGrafo().caminhoMinimo(origem.getCodigo(), destino.getCodigo());
        int limite = hemocomponente != null
                ? hemocomponente.getLimiteCadeiaFriaMinutos()
                : Hemocomponente.HEMACIAS.getLimiteCadeiaFriaMinutos();

        if (!resultado.existeRota()) {
            throw new RecursoNaoEncontradoException("Não há rota entre " + origem.getCodigo() + " e " + destino.getCodigo());
        }

        boolean dentro = resultado.custoTotalMinutos() <= limite;
        if (exigirCadeiaFria && !dentro) {
            throw new RegraNegocioException(
                    "Rota de " + resultado.custoTotalMinutos() + " min ultrapassa o limite da cadeia fria de " + limite + " min"
            );
        }

        List<Long> caminhoIds = resultado.caminho().stream()
                .map(codigo -> noRedeRepository.findByCodigo(codigo)
                        .orElseThrow(() -> new RecursoNaoEncontradoException("Nó não encontrado: " + codigo))
                        .getId())
                .toList();

        String alerta = dentro ? null : "Tempo estimado acima do limite da cadeia fria";
        return new ResultadoRota(
                caminhoIds,
                resultado.caminho(),
                resultado.custoTotalMinutos(),
                true,
                dentro,
                limite,
                alerta
        );
    }

    public record ResultadoRota(
            List<Long> caminho,
            List<String> caminhoCodigos,
            int custoTotalMinutos,
            boolean existeRota,
            boolean dentroDaCadeiaFria,
            int limiteCadeiaFriaMinutos,
            String alerta
    ) {
    }
}
