package com.hemoflow.hemoflow.api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hemoflow.hemoflow.api.dto.EntregaDTOs;
import com.hemoflow.hemoflow.dominio.StatusRequisicao;
import com.hemoflow.hemoflow.estoque.EntregaService;

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
    public List<EntregaDTOs.Resposta> listar() {
        return entregaService.listarEmAcompanhamento();
    }

    @PatchMapping("/requisicoes/{requisicaoId}/status")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void atualizarStatus(
            @PathVariable Long requisicaoId,
            @Valid @RequestBody AtualizacaoStatus dto
    ) {
        entregaService.atualizarStatus(requisicaoId, dto.status());
    }

    @PatchMapping("/{entregaId}/proximo-ponto")
    public EntregaDTOs.Resposta avancarProximoPonto(@PathVariable Long entregaId) {
        return entregaService.avancarProximoPonto(entregaId);
    }

    public record AtualizacaoStatus(@NotNull StatusRequisicao status) {
    }
}
