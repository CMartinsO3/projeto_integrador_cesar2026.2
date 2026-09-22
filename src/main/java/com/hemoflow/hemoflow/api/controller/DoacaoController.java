package com.hemoflow.hemoflow.api.controller;

import com.hemoflow.hemoflow.api.dto.DoacaoDTO;
import com.hemoflow.hemoflow.service.DoacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/doacoes")
@Tag(name = "Doações", description = "Gerenciamento de doações de sangue")
public class DoacaoController {
    
    private final DoacaoService doacaoService;
    
    public DoacaoController(DoacaoService doacaoService) {
        this.doacaoService = doacaoService;
    }
    
    @PostMapping
    @Operation(summary = "Registrar doação", description = "Registra uma nova doação no sistema")
    public ResponseEntity<DoacaoDTO.Resposta> criar(@Valid @RequestBody DoacaoDTO.Requisicao requisicao) {
        DoacaoDTO.Resposta resposta = doacaoService.criar(requisicao);
        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }
    
    @GetMapping
    @Operation(summary = "Listar doações", description = "Lista todas as doações registradas")
    public ResponseEntity<List<DoacaoDTO.Resposta>> listarTodas() {
        List<DoacaoDTO.Resposta> doacoes = doacaoService.listarTodas();
        return ResponseEntity.ok(doacoes);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Buscar doação", description = "Busca uma doação específica por ID")
    public ResponseEntity<DoacaoDTO.Resposta> buscarPorId(@PathVariable Long id) {
        DoacaoDTO.Resposta doacao = doacaoService.buscarPorId(id);
        return ResponseEntity.ok(doacao);
    }
}
