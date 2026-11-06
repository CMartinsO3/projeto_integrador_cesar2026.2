package com.hemoflow.hemoflow.api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hemoflow.hemoflow.api.dto.EntregaDTOs;
import com.hemoflow.hemoflow.dominio.Entrega;
import com.hemoflow.hemoflow.dominio.StatusRequisicao;
import com.hemoflow.hemoflow.estoque.EntregaService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.ResponseStatus;

@RestController
@RequestMapping("/api/v1/entregas")
public class EntregaController {

    private final EntregaService entregaService;

    public EntregaController(EntregaService entregaService) {
        this.entregaService = entregaService;
    }

    @GetMapping
    public List<EntregaDTOs.Resposta> listar(HttpSession session) {
        return entregaService.listarEmAcompanhamento(operacionalId(session));
    }

    @PatchMapping("/requisicoes/{requisicaoId}/status")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void atualizarStatus(
            @PathVariable Long requisicaoId,
            @Valid @RequestBody AtualizacaoStatus dto,
            HttpSession session
    ) {
        entregaService.atualizarStatus(requisicaoId, dto.status(), operacionalId(session));
    }

    @PatchMapping("/{entregaId}/proximo-ponto")
    public EntregaDTOs.Resposta avancarProximoPonto(@PathVariable Long entregaId, HttpSession session) {
        return entregaService.avancarProximoPonto(entregaId, operacionalId(session));
    }

    @PutMapping("/{entregaId}/motorista")
    public AtribuicaoResposta atribuirMotorista(
            @PathVariable Long entregaId,
            @Valid @RequestBody AtribuicaoMotorista dto
    ) {
        Entrega entrega = entregaService.atribuirMotorista(entregaId, dto.motoristaId());
        return new AtribuicaoResposta(
                entrega.getId(),
                entrega.getMotorista().getId(),
                entrega.getMotorista().getNome()
        );
    }

    private Long operacionalId(HttpSession session) {
        Object id = session.getAttribute("operacionalId");
        return id instanceof Long value ? value : null;
    }

    public record AtualizacaoStatus(@NotNull StatusRequisicao status) {
    }

    public record AtribuicaoMotorista(@NotNull Long motoristaId) {
    }

    public record AtribuicaoResposta(Long entregaId, Long motoristaId, String motoristaNome) {
    }
}
