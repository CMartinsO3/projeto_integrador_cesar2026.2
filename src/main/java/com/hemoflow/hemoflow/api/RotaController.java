package com.hemoflow.hemoflow.api;

import com.hemoflow.hemoflow.dominio.Ligacao;
import com.hemoflow.hemoflow.dominio.NoRede;
import com.hemoflow.hemoflow.grafo.Grafo;
import com.hemoflow.hemoflow.persistencia.LigacaoRepository;
import com.hemoflow.hemoflow.persistencia.NoRedeRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rotas")
public class RotaController {

    private static final int LIMITE_CADEIA_FRIA_MINUTOS = 120;

    private final NoRedeRepository noRedeRepository;
    private final LigacaoRepository ligacaoRepository;

    public RotaController(NoRedeRepository noRedeRepository, LigacaoRepository ligacaoRepository) {
        this.noRedeRepository = noRedeRepository;
        this.ligacaoRepository = ligacaoRepository;
    }

    @GetMapping("/caminho-minimo")
    public ResponseEntity<CaminhoMinimoResponse> calcularCaminhoMinimo(
            @RequestParam Long origemId,
            @RequestParam Long destinoId,
            @RequestParam(defaultValue = "false") boolean exigirCadeiaFria) {

        NoRede origem = noRedeRepository.findById(origemId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Nó de origem não encontrado: " + origemId));
        NoRede destino = noRedeRepository.findById(destinoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Nó de destino não encontrado: " + destinoId));

        Grafo grafo = new Grafo();
        noRedeRepository.findAll().forEach(no -> grafo.adicionarVertice(no.getId()));
        for (Ligacao lig : ligacaoRepository.findAll()) {
            grafo.adicionarAresta(
                    lig.getOrigem().getId(),
                    lig.getDestino().getId(),
                    lig.getTempoMinutos());
        }

        Grafo.ResultadoCaminhoMinimo resultado = grafo.calcularCaminhoMinimo(origem.getId(), destino.getId());

        boolean alertaCadeiaFria = exigirCadeiaFria
                && resultado.existeRota()
                && resultado.custoTotalMinutos() > LIMITE_CADEIA_FRIA_MINUTOS;

        return ResponseEntity.ok(new CaminhoMinimoResponse(
                resultado.caminho(),
                resultado.custoTotalMinutos(),
                resultado.existeRota(),
                alertaCadeiaFria
        ));
    }

    public record CaminhoMinimoResponse(
            List<Long> caminho,
            int custoTotalMinutos,
            boolean existeRota,
            boolean alertaCadeiaFria
    ) {}
}