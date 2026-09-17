package com.hemoflow.hemoflow.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.hemoflow.hemoflow.estatistica.EstatisticaService;

/**
 * Controller MVC que serve o painel HTML da aplicação.
 * Agrega indicadores de estoque e requisições via EstatisticaService
 * e os repassa ao template Thymeleaf.
 */
@Controller
public class PainelController {

    private final EstatisticaService estatisticaService;

    public PainelController(EstatisticaService estatisticaService) {
        this.estatisticaService = estatisticaService;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("estoque", estatisticaService.resumoEstoque());
        model.addAttribute("requisicoes", estatisticaService.resumoRequisicoes());
        return "index";
    }
}
