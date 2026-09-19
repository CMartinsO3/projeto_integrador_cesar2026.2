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
        return repo.findByAtivaTrue().stream().map(c -> Map.<String, Object>of(
            "id", c.getId(), "titulo", c.getTitulo(),
            "descricao", c.getDescricao() != null ? c.getDescricao() : "",
            "dataInicio", c.getDataInicio().toString(),
            "dataFim", c.getDataFim().toString(),
            "hospital", c.getHospital() != null ? c.getHospital().getNome() : "Geral",
            "tiposSanguineos", c.getTiposSanguineosAlvo() != null ? c.getTiposSanguineosAlvo() : "Todos"
        )).toList();
    }
}