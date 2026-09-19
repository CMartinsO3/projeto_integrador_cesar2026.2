package com.hemoflow.hemoflow.api;

import com.hemoflow.hemoflow.dominio.*;
import com.hemoflow.hemoflow.persistencia.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/doadores")
public class DoadorController {
    private final DoadorRepository doadorRepo;
    private final HospitalRepository hospitalRepo;
    private final DoacaoRealizadaRepository doacaoRepo;

    public DoadorController(DoadorRepository doadorRepo, HospitalRepository hospitalRepo, DoacaoRealizadaRepository doacaoRepo) {
        this.doadorRepo = doadorRepo; this.hospitalRepo = hospitalRepo; this.doacaoRepo = doacaoRepo;
    }

    @PostMapping
    public ResponseEntity<?> cadastrar(@RequestBody Map<String, String> body, HttpSession session) {
        if (doadorRepo.findByEmail(body.get("email")).isPresent())
            return ResponseEntity.badRequest().body(Map.of("erro", "Email ja cadastrado"));
        Doador d = new Doador(
            body.get("nome"), body.get("email"), body.get("senha"),
            TipoSanguineo.valueOf(body.get("tipoSanguineo")),
            LocalDate.parse(body.get("dataNascimento"))
        );
        Doador salvo = doadorRepo.save(d);
        session.setAttribute("doadorId", salvo.getId());
        session.setAttribute("doadorNome", salvo.getNome());
        session.setAttribute("role", "DOADOR");
        session.removeAttribute("adminId");
        session.removeAttribute("adminNome");
        Map<String, Object> resposta = new LinkedHashMap<>(toMap(salvo));
        resposta.put("ok", true);
        resposta.put("redirect", "/mapa");
        return ResponseEntity.ok(resposta);
    }

    @GetMapping
    public List<Map<String, Object>> listar() {
        return doadorRepo.findAll().stream().map(this::toMap).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscar(@PathVariable Long id) {
        return doadorRepo.findById(id).map(d -> ResponseEntity.ok(toMap(d)))
            .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return doadorRepo.findById(id).map(d -> {
            if (body.containsKey("nome")) d.setNome(body.get("nome"));
            if (body.containsKey("tipoSanguineo")) d.setTipoSanguineo(TipoSanguineo.valueOf(body.get("tipoSanguineo")));
            return ResponseEntity.ok(toMap(doadorRepo.save(d)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(@PathVariable Long id) {
        return doadorRepo.findById(id).map(d -> {
            d.setAtivo(false); doadorRepo.save(d);
            return ResponseEntity.<Void>ok().build();
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/hospitais-recomendados")
    public ResponseEntity<?> hospitaisRecomendados(@PathVariable Long id) {
        return doadorRepo.findById(id).map(d -> {
            List<Map<String, Object>> hospitais = hospitalRepo.findAll().stream()
                .filter(Hospital::isAtivo)
                .map(h -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", h.getId());
                    m.put("nome", h.getNome());
                    m.put("latitude", h.getLatitude() != null ? h.getLatitude() : 0.0);
                    m.put("longitude", h.getLongitude() != null ? h.getLongitude() : 0.0);
                    m.put("tipoSanguineoDoador", d.getTipoSanguineo().name());
                    m.put("codigoNo", h.getLocalizacao() != null ? h.getLocalizacao().getCodigo() : "");
                    m.put("ponto", h.getLocalizacao() != null ? h.getLocalizacao().getNome() : h.getNome());
                    return m;
                })
                .toList();
            return ResponseEntity.ok(hospitais);
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/doacoes")
    public ResponseEntity<?> historicoDoacoes(@PathVariable Long id) {
        List<Map<String, Object>> hist = doacaoRepo.findByDoadorIdOrderByDataDoacaoDesc(id).stream().map(dr -> {
            java.util.LinkedHashMap<String, Object> m = new java.util.LinkedHashMap<>();
            m.put("id", dr.getId());
            m.put("dataDoacao", dr.getDataDoacao().toString());
            m.put("hospital", dr.getHospital().getNome());
            m.put("volumeMl", dr.getVolumeMl());
            return (Map<String, Object>) m;
        }).toList();
        return ResponseEntity.ok(hist);
    }

    private Map<String, Object> toMap(Doador d) {
        return Map.of(
            "id", d.getId(),
            "nome", d.getNome(),
            "email", d.getEmail(),
            "tipoSanguineo", d.getTipoSanguineo().name(),
            "dataNascimento", d.getDataNascimento().toString(),
            "ativo", d.isAtivo()
        );
    }
}