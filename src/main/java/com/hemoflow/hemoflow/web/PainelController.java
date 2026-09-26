package com.hemoflow.hemoflow.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.hemoflow.hemoflow.estatistica.EstatisticaService;

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

    @GetMapping("/doe-agora")
    public String doeAgora() { return "doe-agora"; }

    @GetMapping("/mapa")
    public String mapa() { return "mapa-doacao"; }

    @GetMapping("/bolsas-painel")
    public String painelBolsas() { return "painel-bolsas"; }

    @GetMapping("/login-admin")
    public String loginAdmin() { return "login-admin"; }

    @GetMapping("/login-usuario")
    public String loginUsuario() { return "login-usuario"; }

    @GetMapping("/dashboard-admin")
    public String dashboardAdmin() { return "dashboard-admin"; }

    @GetMapping("/dashboard-usuario")
    public String dashboardUsuario() { return "dashboard-usuario"; }
}


