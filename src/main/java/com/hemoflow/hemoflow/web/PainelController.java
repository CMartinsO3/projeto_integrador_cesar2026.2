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

    @GetMapping("/estatisticas")
    public String estatisticas() { return "estatisticas"; }

    @GetMapping("/doe-agora")
    public String doeAgora() { return "doe-agora"; }

    @GetMapping("/mapa")
    public String mapa() { return "mapa-doacao"; }

    @GetMapping("/rotas")
    public String rotas() { return "rotas"; }

    @GetMapping("/requisicoes-hospital")
    public String requisicoesHospital() { return "requisicoes-hospital"; }

    @GetMapping("/motorista")
    public String motorista() { return "motorista"; }

    @GetMapping("/monitoramento")
    public String monitoramento() { return "monitoramento"; }

    @GetMapping("/bolsas-painel")
    public String painelBolsas() { return "painel-bolsas"; }

    @GetMapping("/login-admin")
    public String loginAdmin() { return "login-admin"; }

    @GetMapping("/login-usuario")
    public String loginUsuario() { return "login-usuario"; }

    @GetMapping({"/login-hospital", "/login-motorista"})
    public String loginOperacional() { return "login-operacional"; }

    @GetMapping("/cadastro-operacional")
    public String cadastroOperacional() { return "cadastro-operacional"; }

    @GetMapping("/dashboard-admin")
    public String dashboardAdmin() { return "dashboard-admin"; }

    @GetMapping("/contas-operacionais")
    public String contasOperacionais() { return "contas-operacionais"; }

    @GetMapping("/dashboard-usuario")
    public String dashboardUsuario() { return "dashboard-usuario"; }
}
