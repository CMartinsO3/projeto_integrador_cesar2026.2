package com.hemoflow.hemoflow.api;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import com.hemoflow.hemoflow.dominio.Hemocomponente;
import com.hemoflow.hemoflow.dominio.StatusBolsa;
import com.hemoflow.hemoflow.dominio.TipoSanguineo;
import com.hemoflow.hemoflow.estoque.EstoqueService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
public class BolsaController {

    private final EstoqueService estoqueService;

    public BolsaController(EstoqueService estoqueService) {
        this.estoqueService = estoqueService;
    }

    @PostMapping("/bolsas")
    @ResponseStatus(HttpStatus.CREATED)
    public BolsaDTOs.Resposta criar(@Valid @RequestBody BolsaDTOs.RequisicaoCadastro dto) {
        return BolsaDTOs.Resposta.de(estoqueService.criarBolsa(dto));
    }

    @GetMapping("/bolsas")
    public List<BolsaDTOs.Resposta> listar(@RequestParam(required = false) StatusBolsa status) {
        return estoqueService.listar(status).stream().map(BolsaDTOs.Resposta::de).toList();
    }

    @GetMapping("/bolsas/{id}")
    public BolsaDTOs.Resposta buscar(@PathVariable Long id) {
        return BolsaDTOs.Resposta.de(estoqueService.buscar(id));
    }

    @PatchMapping({"/bolsas/{id}/status", "/{id}/status"})
    public BolsaDTOs.Resposta atualizarStatus(
            @PathVariable Long id,
            @Valid @RequestBody BolsaDTOs.AtualizacaoStatus dto) {
        return BolsaDTOs.Resposta.de(estoqueService.transicionarStatusBolsa(id, dto.status()));
    }

    @PostMapping("/bolsas/entrada")
    public ResponseEntity<?> registrarEntrada(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(estoqueService.registrarEntrada(
            TipoSanguineo.valueOf(body.get("tipoSanguineo")),
            Hemocomponente.valueOf(body.get("hemocomponente")),
            java.time.LocalDate.parse(body.get("dataColeta")),
            java.time.LocalDate.parse(body.get("dataValidade")),
            body.get("lote"),
            Long.valueOf(body.get("noRedeId"))
        ));
    }

    @PostMapping("/bolsas/{id}/saida")
    public ResponseEntity<?> registrarSaida(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String motivo = body.getOrDefault("motivo", "TRANSFUSAO");
        return ResponseEntity.ok(estoqueService.registrarSaida(id, motivo));
    }

    @GetMapping("/bolsas/hospital/{hospitalId}/estoque-mock")
    public ResponseEntity<?> estoqueMock(@PathVariable Long hospitalId) {
        return ResponseEntity.ok(estoqueService.gerarEstoqueMock(hospitalId));
    }

    @GetMapping("/bolsas/alertas-vencimento")
    public ResponseEntity<?> alertasVencimento(@RequestParam(defaultValue = "5") int dias) {
        return ResponseEntity.ok(estoqueService.alertasVencimento(dias));
    }

    @GetMapping("/bolsas/em-transito")
    public ResponseEntity<?> emTransito() {
        return ResponseEntity.ok(estoqueService.bolsasEmTransito());
    }
}

