package com.hemoflow.hemoflow.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hemoflow.hemoflow.dominio.Hemocomponente;
import com.hemoflow.hemoflow.grafo.RoteirizacaoService;

@RestController
@RequestMapping("/api/v1/rotas")
public class RotaController {

    private final RoteirizacaoService roteirizacaoService;

    public RotaController(RoteirizacaoService roteirizacaoService) {
        this.roteirizacaoService = roteirizacaoService;
    }

    @GetMapping("/caminho-minimo")
    public RoteirizacaoService.ResultadoRota caminhoMinimo(
            @RequestParam Long origemId,
            @RequestParam Long destinoId,
            @RequestParam(defaultValue = "false") boolean exigirCadeiaFria,
            @RequestParam(required = false) Hemocomponente hemocomponente
    ) {
        return roteirizacaoService.caminhoMinimo(origemId, destinoId, exigirCadeiaFria, hemocomponente);
    }
}
