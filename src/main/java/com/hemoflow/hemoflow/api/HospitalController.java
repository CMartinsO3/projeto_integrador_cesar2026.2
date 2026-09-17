package com.hemoflow.hemoflow.api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.hemoflow.hemoflow.api.dto.HospitalDTOs;
import com.hemoflow.hemoflow.api.dto.NoRedeDTOs;
import com.hemoflow.hemoflow.rede.RedeService;

import jakarta.validation.Valid;

/**
 * CRUD completo de hospitais.
 *
 * <pre>
 * POST   /api/v1/hospitais           → cria hospital + nó de rede (tipo HOSPITAL)
 * GET    /api/v1/hospitais           → lista todos
 * GET    /api/v1/hospitais/{id}      → busca por id
 * PUT    /api/v1/hospitais/{id}      → atualiza nome e status ativo
 * DELETE /api/v1/hospitais/{id}      → inativação lógica (não remove o histórico)
 * </pre>
 */
@RestController
@RequestMapping("/api/v1/hospitais")
public class HospitalController {

    private final RedeService redeService;

    public HospitalController(RedeService redeService) {
        this.redeService = redeService;
    }

    /**
     * Cria um hospital a partir de um nó de rede com tipo HOSPITAL.
     * Internamente, cria primeiro o {@code NoRede} e depois associa o hospital a ele.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public HospitalDTOs.Resposta criar(@Valid @RequestBody HospitalDTOs.Cadastro dto) {
        // Monta um NoRedeDTOs.Cadastro para reutilizar criarNo() no RedeService
        NoRedeDTOs.Cadastro noDto = new NoRedeDTOs.Cadastro(
                "H-" + dto.nome().toUpperCase().replaceAll("\\s+", "_").substring(0, Math.min(dto.nome().length(), 8)),
                dto.nome(),
                com.hemoflow.hemoflow.dominio.TipoNo.HOSPITAL
        );
        return HospitalDTOs.Resposta.de(redeService.criarHospital(noDto));
    }

    @GetMapping
    public List<HospitalDTOs.Resposta> listar() {
        return redeService.listarHospitais().stream()
                .map(HospitalDTOs.Resposta::de)
                .toList();
    }

    @GetMapping("/{id}")
    public HospitalDTOs.Resposta buscar(@PathVariable Long id) {
        return HospitalDTOs.Resposta.de(redeService.buscarHospital(id));
    }

    /**
     * Atualiza o nome e o estado ativo/inativo de um hospital existente.
     */
    @PutMapping("/{id}")
    public HospitalDTOs.Resposta atualizar(
            @PathVariable Long id,
            @Valid @RequestBody HospitalDTOs.Atualizacao dto) {
        return HospitalDTOs.Resposta.de(redeService.atualizarHospital(id, dto));
    }

    /**
     * Inativação lógica — o hospital é marcado como inativo mas não é excluído,
     * preservando o histórico de requisições associadas.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void inativar(@PathVariable Long id) {
        redeService.inativarHospital(id);
    }
}
