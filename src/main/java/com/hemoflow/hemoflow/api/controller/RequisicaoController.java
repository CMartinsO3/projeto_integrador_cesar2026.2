package com.hemoflow.hemoflow.api.controller;

import com.hemoflow.hemoflow.api.dto.RequisicaoDTO;
import com.hemoflow.hemoflow.domain.enums.StatusRequisicao;
import com.hemoflow.hemoflow.service.RequisicaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/requisicoes")
@Tag(name = "Requisições", description = "Gerenciamento de requisições hospitalares")
public class RequisicaoController {
    
    private final RequisicaoService requisicaoService;
    
    public RequisicaoController(RequisicaoService requisicaoService) {
        this.requisicaoService = requisicaoService;
    }
    
    @PostMapping
    @Operation(summary = "Criar requisição", description = "Cria uma nova requisição hospitalar")
    public ResponseEntity<RequisicaoDTO.Resposta> criar(@Valid @RequestBody RequisicaoDTO.Requisicao requisicao) {
        RequisicaoDTO.Resposta resposta = requisicaoService.criar(requisicao);
        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }
    
    @GetMapping
    @Operation(summary = "Listar requisições", description = "Lista requisições com filtro opcional por status")
    public ResponseEntity<List<RequisicaoDTO.Resposta>> listarTodas(
            @RequestParam(required = false) StatusRequisicao status) {
        List<RequisicaoDTO.Resposta> requisicoes = requisicaoService.listarTodas(status);
        return ResponseEntity.ok(requisicoes);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Buscar requisição", description = "Busca uma requisição específica por ID")
    public ResponseEntity<RequisicaoDTO.Resposta> buscarPorId(@PathVariable Long id) {
        RequisicaoDTO.Resposta requisicao = requisicaoService.buscarPorId(id);
        return ResponseEntity.ok(requisicao);
    }
    
    @PatchMapping("/{id}/status")
    @Operation(summary = "Atualizar status", description = "Atualiza o status de uma requisição")
    public ResponseEntity<RequisicaoDTO.Resposta> atualizarStatus(
            @PathVariable Long id,
            @Valid @RequestBody RequisicaoDTO.AtualizacaoStatus atualizacao) {
        RequisicaoDTO.Resposta resposta = requisicaoService.atualizarStatus(id, atualizacao);
        return ResponseEntity.ok(resposta);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Cancelar requisição", description = "Cancela uma requisição (soft delete)")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        requisicaoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
