package com.hemoflow.hemoflow.api.controller;

import com.hemoflow.hemoflow.api.dto.BolsaComponenteDTO;
import com.hemoflow.hemoflow.domain.enums.StatusBolsa;
import com.hemoflow.hemoflow.service.BolsaComponenteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/bolsas")
@Tag(name = "Bolsas/Componentes", description = "Gerenciamento de bolsas e componentes sanguíneos")
public class BolsaController {
    
    private final BolsaComponenteService bolsaComponenteService;
    
    public BolsaController(BolsaComponenteService bolsaComponenteService) {
        this.bolsaComponenteService = bolsaComponenteService;
    }
    
    @PostMapping
    @Operation(summary = "Criar bolsa", description = "Cria uma nova bolsa/componente a partir de uma doação")
    public ResponseEntity<BolsaComponenteDTO.Resposta> criar(@Valid @RequestBody BolsaComponenteDTO.Requisicao requisicao) {
        BolsaComponenteDTO.Resposta resposta = bolsaComponenteService.criar(requisicao);
        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }
    
    @GetMapping
    @Operation(summary = "Listar bolsas", description = "Lista bolsas com filtro opcional por status")
    public ResponseEntity<List<BolsaComponenteDTO.Resposta>> listarTodas(
            @RequestParam(required = false) StatusBolsa status) {
        List<BolsaComponenteDTO.Resposta> bolsas = bolsaComponenteService.listarTodas(status);
        return ResponseEntity.ok(bolsas);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Buscar bolsa", description = "Busca uma bolsa específica por ID")
    public ResponseEntity<BolsaComponenteDTO.Resposta> buscarPorId(@PathVariable Long id) {
        BolsaComponenteDTO.Resposta bolsa = bolsaComponenteService.buscarPorId(id);
        return ResponseEntity.ok(bolsa);
    }
    
    @PatchMapping("/{id}/status")
    @Operation(summary = "Atualizar status", description = "Atualiza o status de uma bolsa")
    public ResponseEntity<BolsaComponenteDTO.Resposta> atualizarStatus(
            @PathVariable Long id,
            @Valid @RequestBody BolsaComponenteDTO.AtualizacaoStatus atualizacao) {
        BolsaComponenteDTO.Resposta resposta = bolsaComponenteService.atualizarStatus(id, atualizacao);
        return ResponseEntity.ok(resposta);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Remover bolsa", description = "Remove uma bolsa do sistema")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        bolsaComponenteService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
