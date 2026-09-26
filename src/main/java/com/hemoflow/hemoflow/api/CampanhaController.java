package com.hemoflow.hemoflow.api;

import com.hemoflow.hemoflow.dominio.Campanha;
import com.hemoflow.hemoflow.persistencia.CampanhaRepository;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/campanhas")
public class CampanhaController {
    private final CampanhaRepository repo;
    public CampanhaController(CampanhaRepository repo) { this.repo = repo; }

    @GetMapping
    public List<Map<String, Object>> listar() {
        return repo.findByAtivaTrue().stream().map(c -> {
            Map<String, Object> m = new java.util.LinkedHashMap<>();
            m.put("id", c.getId());
            m.put("titulo", c.getTitulo());
            m.put("descricao", c.getDescricao() != null ? c.getDescricao() : "");
            m.put("dataInicio", c.getDataInicio().toString());
            m.put("dataFim", c.getDataFim().toString());
            m.put("hospital", c.getHospital() != null ? c.getHospital().getNome() : "Geral");
            m.put("hospitalId", c.getHospital() != null ? c.getHospital().getId() : null);
            m.put("tiposSanguineos", c.getTiposSanguineosAlvo() != null ? c.getTiposSanguineosAlvo() : "Todos");
            return m;
        }).toList();
    }
}