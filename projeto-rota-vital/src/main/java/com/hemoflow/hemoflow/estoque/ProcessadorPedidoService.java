package com.hemoflow.hemoflow.estoque;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hemoflow.hemoflow.api.RecursoNaoEncontradoException;
import com.hemoflow.hemoflow.api.RegraNegocioException;
import com.hemoflow.hemoflow.dominio.Bolsa;
import com.hemoflow.hemoflow.dominio.Hemocomponente;
import com.hemoflow.hemoflow.dominio.Requisicao;
import com.hemoflow.hemoflow.dominio.StatusBolsa;
import com.hemoflow.hemoflow.dominio.StatusRequisicao;
import com.hemoflow.hemoflow.persistencia.BolsaRepository;
import com.hemoflow.hemoflow.persistencia.RequisicaoRepository;
import com.hemoflow.hemoflow.rede.RedeService;

@Service
public class ProcessadorPedidoService {

    private final RequisicaoRepository requisicaoRepository;
    private final BolsaRepository bolsaRepository;
    private final IndiceEstoque indiceEstoque;
    private final RedeService redeService;

    public ProcessadorPedidoService(
            RequisicaoRepository requisicaoRepository,
            BolsaRepository bolsaRepository,
            IndiceEstoque indiceEstoque,
            RedeService redeService
    ) {
        this.requisicaoRepository = requisicaoRepository;
        this.bolsaRepository = bolsaRepository;
        this.indiceEstoque = indiceEstoque;
        this.redeService = redeService;
    }

    /**
     * Processa o pedido em uma única transação: consulta o índice hash,
     * aplica compatibilidade e FEFO, calcula as rotas elegíveis e aloca.
     */
    @Transactional(noRollbackFor = RegraNegocioException.class)
    public ResultadoProcessamento processarPedido(Long requisicaoId) {
        Requisicao requisicao = requisicaoRepository.findById(requisicaoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Requisição não encontrada: " + requisicaoId));
        if (requisicao.getStatus() != StatusRequisicao.PENDENTE
                && requisicao.getStatus() != StatusRequisicao.AGUARDANDO_ESTOQUE) {
            throw new RegraNegocioException(
                    "Requisição em status " + requisicao.getStatus() + " não pode ser processada");
        }

        indiceEstoque.descartarVencidas();
        List<Bolsa> candidatasPorCompatibilidade = buscarNoHash(requisicao);
        List<Entrega> entregas = new ArrayList<>();

        for (Bolsa bolsa : candidatasPorCompatibilidade) {
            if (bolsa.getLocalizacaoAtual() == null) {
                continue;
            }
            RedeService.RotaCalculada rota = redeService.calcularRotaVizinhoMaisProximo(
                    bolsa.getLocalizacaoAtual().getId(),
                    requisicao.getHospital().getLocalizacao().getId());
            if (rota.encontrada()) {
                entregas.add(new Entrega(bolsa, rota));
                if (entregas.size() == requisicao.getQuantidade()) {
                    break;
                }
            }
        }

        if (entregas.size() < requisicao.getQuantidade()) {
            requisicao.setStatus(StatusRequisicao.AGUARDANDO_ESTOQUE);
            requisicaoRepository.save(requisicao);
            throw new RegraNegocioException(
                    "Estoque compatível e roteável insuficiente: necessários " + requisicao.getQuantidade()
                            + ", disponíveis " + entregas.size());
        }

        List<Bolsa> alocadas = entregas.stream().map(Entrega::bolsa).toList();
        alocadas.forEach(bolsa -> bolsa.setStatus(StatusBolsa.ALOCADA));
        bolsaRepository.saveAll(alocadas);
        alocadas.forEach(indiceEstoque::retirarAposCommit);
        requisicao.setStatus(StatusRequisicao.ALOCADA);
        requisicaoRepository.save(requisicao);

        return new ResultadoProcessamento(requisicao, List.copyOf(entregas));
    }

    private List<Bolsa> buscarNoHash(Requisicao requisicao) {
        if (requisicao.getHemocomponente() == Hemocomponente.HEMACIAS) {
            return indiceEstoque.candidatasCompativeis(
                    requisicao.getTipoSanguineo(), requisicao.getHemocomponente());
        }
        return indiceEstoque.candidatasCompativeisExatas(
                requisicao.getTipoSanguineo(), requisicao.getHemocomponente());
    }

    public record Entrega(Bolsa bolsa, RedeService.RotaCalculada rota) {
    }

    public record ResultadoProcessamento(Requisicao requisicao, List<Entrega> entregas) {
        public List<Bolsa> bolsas() {
            return entregas.stream().map(Entrega::bolsa).toList();
        }
    }
}
