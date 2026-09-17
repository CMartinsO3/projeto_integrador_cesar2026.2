package com.hemoflow.hemoflow.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hemoflow.hemoflow.api.dto.HospitalDTOs;
import com.hemoflow.hemoflow.rede.RedeService;

@RestController
@RequestMapping("/api/v1/hospitais")
public class HospitalController {

    private final RedeService redeService;

    public HospitalController(RedeService redeService) {
        this.redeService = redeService;
    }

    @GetMapping
    public List<HospitalDTOs.Resposta> listar() {
        return redeService.listarHospitais().stream().map(HospitalDTOs.Resposta::de).toList();
    }

    @GetMapping("/{id}")
    public HospitalDTOs.Resposta buscar(@PathVariable Long id) {
        return HospitalDTOs.Resposta.de(redeService.buscarHospital(id));
    }
}
