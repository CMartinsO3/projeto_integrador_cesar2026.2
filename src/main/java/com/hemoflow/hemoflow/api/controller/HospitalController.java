package com.hemoflow.hemoflow.api.controller;

import com.hemoflow.hemoflow.api.dto.HospitalDTO;
import com.hemoflow.hemoflow.service.HospitalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/hospitais")
@Tag(name = "Hospitais", description = "Gerenciamento de hospitais")
public class HospitalController {
    
    private final HospitalService hospitalService;
    
    public HospitalController(HospitalService hospitalService) {
        this.hospitalService = hospitalService;
    }
    
    @PostMapping
    @Operation(summary = "Cadastrar hospital", description = "Cria um novo hospital no sistema")
    public ResponseEntity<HospitalDTO.Resposta> criar(@Valid @RequestBody HospitalDTO.Requisicao requisicao) {
        HospitalDTO.Resposta resposta = hospitalService.criar(requisicao);
        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }
    
    @GetMapping
    @Operation(summary = "Listar hospitais", description = "Lista todos os hospitais cadastrados")
    public ResponseEntity<List<HospitalDTO.Resposta>> listarTodos() {
        List<HospitalDTO.Resposta> hospitais = hospitalService.listarTodos();
        return ResponseEntity.ok(hospitais);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Buscar hospital", description = "Busca um hospital específico por ID")
    public ResponseEntity<HospitalDTO.Resposta> buscarPorId(@PathVariable Long id) {
        HospitalDTO.Resposta hospital = hospitalService.buscarPorId(id);
        return ResponseEntity.ok(hospital);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Atualizar hospital", description = "Atualiza os dados de um hospital")
    public ResponseEntity<HospitalDTO.Resposta> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody HospitalDTO.Requisicao requisicao) {
        HospitalDTO.Resposta resposta = hospitalService.atualizar(id, requisicao);
        return ResponseEntity.ok(resposta);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Inativar hospital", description = "Inativa um hospital (soft delete)")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        hospitalService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
