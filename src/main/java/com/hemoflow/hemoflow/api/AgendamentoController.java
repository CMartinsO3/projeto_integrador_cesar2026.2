package com.hemoflow.hemoflow.api;

import java.time.LocalDate;
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
import org.springframework.web.bind.annotation.RestController;

import com.hemoflow.hemoflow.dominio.Agendamento;
import com.hemoflow.hemoflow.dominio.StatusAgendamento;
import com.hemoflow.hemoflow.dominio.StatusAgendamento;
import com.hemoflow.hemoflow.estoque.AgendamentoService;
import com.hemoflow.hemoflow.persistencia.AgendamentoRepository;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping("/api/v1/agendamentos")
public class AgendamentoController {

    private final AgendamentoRepository agendamentoRepository;
    private final AgendamentoService agendamentoService;

    public AgendamentoController(
            AgendamentoRepository agendamentoRepository,
            AgendamentoService agendamentoService) {
        this.agendamentoRepository = agendamentoRepository;
        this.agendamentoService = agendamentoService;
    }

    @GetMapping("/hospital/{hospitalId}/disponibilidade")
    public List<AgendamentoService.HorarioDisponivel> disponibilidade(
            @PathVariable Long hospitalId,
            @RequestParam LocalDate data) {
        return agendamentoService.consultarDisponibilidade(hospitalId, data);
    }

    @PostMapping
    public ResponseEntity<?> criar(
            @Valid @RequestBody NovoAgendamento pedido,
            HttpSession session) {
        Long doadorId = obterDoadorAutenticado(session);
        if (doadorId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("erro", "Entre na sua conta de doador para agendar"));
        }

        Agendamento agendamento = agendamentoService.criar(
                doadorId, pedido.hospitalId(), pedido.dataHora());
        return ResponseEntity.status(HttpStatus.CREATED).body(toMap(agendamento));
    }

    @GetMapping("/doador/{doadorId}")
    public ResponseEntity<?> porDoador(@PathVariable Long doadorId, HttpSession session) {
        Long doadorAutenticado = obterDoadorAutenticado(session);
        if (doadorAutenticado == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("erro", "Entre na sua conta de doador"));
        }
        if (!doadorAutenticado.equals(doadorId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("erro", "Você só pode consultar seus próprios agendamentos"));
        }
        return ResponseEntity.ok(agendamentoRepository.findByDoadorId(doadorId).stream()
                .map(this::toMap)
                .toList());
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> atualizarStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            HttpSession session) {
        if (!"ADMIN".equals(session.getAttribute("role"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("erro", "Somente a equipe clínica pode atualizar o status"));
        }
        try {
            StatusAgendamento status = StatusAgendamento.valueOf(body.get("status"));
            return agendamentoRepository.findById(id)
                    .map(agendamento -> {
                        agendamento.setStatus(status);
                        return ResponseEntity.ok(toMap(agendamentoRepository.save(agendamento)));
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException | NullPointerException ex) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Status de agendamento inválido"));
        }
    }

    private Long obterDoadorAutenticado(HttpSession session) {
        if (!"DOADOR".equals(session.getAttribute("role"))) {
            return null;
        }
        Object id = session.getAttribute("doadorId");
        return id instanceof Number numero ? numero.longValue() : null;
    }

    private Map<String, Object> toMap(Agendamento agendamento) {
        return Map.of(
                "id", agendamento.getId(),
                "doador", agendamento.getDoador().getNome(),
                "hospital", agendamento.getHospital().getNome(),
                "dataHora", agendamento.getDataHora().toString(),
                "status", agendamento.getStatus().name());
    }

    public record NovoAgendamento(
            @NotNull Long hospitalId,
            @NotNull java.time.LocalDateTime dataHora) {
    }
}
