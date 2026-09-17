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

import com.hemoflow.hemoflow.api.dto.NoRedeDTOs;
import com.hemoflow.hemoflow.rede.RedeService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
public class RedeController {

    private final RedeService redeService;

    public RedeController(RedeService redeService) {
        this.redeService = redeService;
    }

    @GetMapping("/nos-rede")
    public List<NoRedeDTOs.Resposta> listarNos() {
        return redeService.listarNos().stream().map(NoRedeDTOs.Resposta::de).toList();
    }

    @GetMapping("/nos-rede/{id}")
    public NoRedeDTOs.Resposta buscarNo(@PathVariable Long id) {
        return NoRedeDTOs.Resposta.de(redeService.buscarNo(id));
    }

    @PostMapping("/nos-rede")
    @ResponseStatus(HttpStatus.CREATED)
    public NoRedeDTOs.Resposta criarNo(@Valid @RequestBody NoRedeDTOs.Cadastro dto) {
        return NoRedeDTOs.Resposta.de(redeService.criarNo(dto));
    }

    @PutMapping("/nos-rede/{id}")
    public NoRedeDTOs.Resposta atualizarNo(@PathVariable Long id, @Valid @RequestBody NoRedeDTOs.Cadastro dto) {
        return NoRedeDTOs.Resposta.de(redeService.atualizarNo(id, dto));
    }

    @DeleteMapping("/nos-rede/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removerNo(@PathVariable Long id) {
        redeService.removerNo(id);
    }

    @GetMapping("/topologia")
    public TopologiaResposta topologia() {
        return new TopologiaResposta(
                redeService.listarNos().stream().map(NoRedeDTOs.Resposta::de).toList(),
                redeService.listarLigacoes().stream().map(NoRedeDTOs.LigacaoResposta::de).toList()
        );
    }

    @GetMapping("/ligacoes")
    public List<NoRedeDTOs.LigacaoResposta> listarLigacoes() {
        return redeService.listarLigacoes().stream().map(NoRedeDTOs.LigacaoResposta::de).toList();
    }

    @PostMapping("/ligacoes")
    @ResponseStatus(HttpStatus.CREATED)
    public NoRedeDTOs.LigacaoResposta criarLigacao(@Valid @RequestBody NoRedeDTOs.LigacaoCadastro dto) {
        return NoRedeDTOs.LigacaoResposta.de(
                redeService.criarLigacao(dto.origemId(), dto.destinoId(), dto.tempoMinutos())
        );
    }

    public record TopologiaResposta(
            List<NoRedeDTOs.Resposta> nos,
            List<NoRedeDTOs.LigacaoResposta> ligacoes
    ) {
    }
}
