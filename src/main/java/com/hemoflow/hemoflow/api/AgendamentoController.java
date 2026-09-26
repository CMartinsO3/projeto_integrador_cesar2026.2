package com.hemoflow.hemoflow.api;

import com.hemoflow.hemoflow.dominio.*;
import com.hemoflow.hemoflow.persistencia.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/agendamentos")
public class AgendamentoController {
    private final AgendamentoRepository repo;
    private final DoadorRepository doadorRepo;
    private final HospitalRepository hospitalRepo;

    public AgendamentoController(AgendamentoRepository repo, DoadorRepository doadorRepo, HospitalRepository hospitalRepo) {
        this.repo = repo; this.doadorRepo = doadorRepo; this.hospitalRepo = hospitalRepo;
    }

    @PostMapping
    public ResponseEntity<?> criar(@RequestBody Map<String, Object> body) {
        Long doadorId = Long.valueOf(body.get("doadorId").toString());
        Long hospitalId = Long.valueOf(body.get("hospitalId").toString());
        Doador doador = doadorRepo.findById(doadorId).orElseThrow();
        Hospital hospital = hospitalRepo.findById(hospitalId).orElseThrow();
        Agendamento a = new Agendamento(doador, hospital, LocalDateTime.parse(body.get("dataHora").toString()));
        return ResponseEntity.ok(toMap(repo.save(a)));
    }

    @GetMapping("/doador/{doadorId}")
    public List<Map<String, Object>> porDoador(@PathVariable Long doadorId) {
        return repo.findByDoadorId(doadorId).stream().map(this::toMap).toList();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> atualizarStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return repo.findById(id).map(a -> {
            a.setStatus(StatusAgendamento.valueOf(body.get("status")));
            return ResponseEntity.ok(toMap(repo.save(a)));
        }).orElse(ResponseEntity.notFound().build());
    }

    private Map<String, Object> toMap(Agendamento a) {
        return Map.of("id", a.getId(), "doador", a.getDoador().getNome(),
            "hospital", a.getHospital().getNome(), "dataHora", a.getDataHora().toString(),
            "status", a.getStatus().name());
    }
}