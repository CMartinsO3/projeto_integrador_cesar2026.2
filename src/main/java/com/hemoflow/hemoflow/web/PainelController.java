package com.hemoflow.hemoflow.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.hemoflow.hemoflow.dominio.StatusBolsa;
import com.hemoflow.hemoflow.estoque.EstoqueService;
import com.hemoflow.hemoflow.rede.RedeService;

@Controller
public class PainelController {

    private final RedeService redeService;
    private final EstoqueService estoqueService;

    public PainelController(RedeService redeService, EstoqueService estoqueService) {
        this.redeService = redeService;
        this.estoqueService = estoqueService;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("nos", redeService.listarNos());
        model.addAttribute("ligacoes", redeService.listarLigacoes());
        model.addAttribute("bolsas", estoqueService.listar(StatusBolsa.DISPONIVEL));
        return "index";
    }
}
