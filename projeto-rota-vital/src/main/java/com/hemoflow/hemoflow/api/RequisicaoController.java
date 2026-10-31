package com.hemoflow.hemoflow.api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.hemoflow.hemoflow.api.dto.BolsaDTOs;
import com.hemoflow.hemoflow.api.dto.NoRedeDTOs;
import com.hemoflow.hemoflow.api.dto.RequisicaoDTOs;
import com.hemoflow.hemoflow.dominio.StatusRequisicao;
import com.hemoflow.hemoflow.estoque.EstoqueService;
import com.hemoflow.hemoflow.estoque.ProcessadorPedidoService;
import com.hemoflow.hemoflow.persistencia.RequisicaoRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/requisicoes")
public class RequisicaoController {

    private final EstoqueService estoqueService;
    private final ProcessadorPedidoService processadorPedidoService;
    private final RequisicaoRepository requisicaoRepository;

    public RequisicaoController(
            EstoqueService estoqueService,
            ProcessadorPedidoService processadorPedidoService,
            RequisicaoRepository requisicaoRepository
    ) {
        this.estoqueService = estoqueService;
        this.processadorPedidoService = processadorPedidoService;
        this.requisicaoRepository = requisicaoRepository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RequisicaoDTOs.Resposta criar(@Valid @RequestBody RequisicaoDTOs.Cadastro dto) {
        return RequisicaoDTOs.Resposta.de(estoqueService.criarRequisicao(dto));
    }

    @GetMapping
    public List<RequisicaoDTOs.Resposta> listar(@RequestParam(required = false) StatusRequisicao status) {
        var lista = status == null ? requisicaoRepository.findAll() : requisicaoRepository.findByStatus(status);
        return lista.stream().map(RequisicaoDTOs.Resposta::de).toList();
    }

    @GetMapping("/{id}")
    public RequisicaoDTOs.Resposta buscar(@PathVariable Long id) {
        return RequisicaoDTOs.Resposta.de(
                requisicaoRepository.findById(id)
                        .orElseThrow(() -> new RecursoNaoEncontradoException("Requisição não encontrada: " + id))
        );
    }

    @PostMapping("/{id}/alocar")
    public List<BolsaDTOs.Resposta> alocar(@PathVariable Long id) {
        return processadorPedidoService.processarPedido(id).bolsas().stream().map(BolsaDTOs.Resposta::de).toList();
    }

    @PostMapping("/{id}/processar")
    public ProcessamentoPedidoResposta processar(@PathVariable Long id) {
        var resultado = processadorPedidoService.processarPedido(id);
        List<EntregaPedidoResposta> entregas = resultado.entregas().stream()
                .map(entrega -> new EntregaPedidoResposta(
                        BolsaDTOs.Resposta.de(entrega.bolsa()),
                        entrega.rota().nos().stream().map(NoRedeDTOs.Resposta::de).toList(),
                        entrega.rota().tempoTotalMinutos(),
                        entrega.rota().distanciaTotalKm()))
                .toList();
        return new ProcessamentoPedidoResposta(resultado.requisicao().getId(),
                resultado.requisicao().getStatus(), entregas);
    }

    @PatchMapping("/{id}/status")
    public RequisicaoDTOs.Resposta atualizarStatus(
            @PathVariable Long id,
            @Valid @RequestBody RequisicaoDTOs.AtualizacaoStatus dto) {
        return RequisicaoDTOs.Resposta.de(estoqueService.transicionarStatusRequisicao(id, dto.status()));
    }

    public record ProcessamentoPedidoResposta(
            Long requisicaoId, StatusRequisicao status, List<EntregaPedidoResposta> entregas
    ) {
    }

    public record EntregaPedidoResposta(
            BolsaDTOs.Resposta bolsa,
            List<NoRedeDTOs.Resposta> rota,
            long tempoTotalMinutos,
            double distanciaTotalKm
    ) {
    }
}
