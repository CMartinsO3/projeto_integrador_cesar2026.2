package com.hemoflow.hemoflow.api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.hemoflow.hemoflow.api.dto.MonitoramentoDTOs;
import com.hemoflow.hemoflow.estoque.MonitoramentoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/monitoramento")
public class MonitoramentoController {

    private final MonitoramentoService monitoramentoService;

    public MonitoramentoController(MonitoramentoService monitoramentoService) {
        this.monitoramentoService = monitoramentoService;
    }

    @GetMapping
    public MonitoramentoDTOs.Painel consultarPainel() {
        return monitoramentoService.consultarPainel();
    }

    @PostMapping("/leituras")
    @ResponseStatus(HttpStatus.CREATED)
    public MonitoramentoDTOs.Leitura registrarLeitura(
            @Valid @RequestBody MonitoramentoDTOs.RegistroTemperatura dto
    ) {
        return monitoramentoService.registrarLeitura(dto);
    }

    @PostMapping("/leituras/simular")
    public List<MonitoramentoDTOs.Leitura> simularLeituras(
            @RequestParam(defaultValue = "false") boolean forcarAlerta
    ) {
        return monitoramentoService.simularLeituras(forcarAlerta);
    }
}
