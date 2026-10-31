package com.hemoflow.hemoflow.estoque;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.hemoflow.hemoflow.dominio.StatusBolsa;
import com.hemoflow.hemoflow.persistencia.BolsaRepository;

/**
 * Monta o {@link IndiceEstoque} a partir do banco quando a aplicação termina
 * de subir (depois da carga de dados iniciais).
 */
@Component
public class IndiceEstoqueCarga {

    private final IndiceEstoque indice;
    private final BolsaRepository bolsaRepository;

    public IndiceEstoqueCarga(IndiceEstoque indice, BolsaRepository bolsaRepository) {
        this.indice = indice;
        this.bolsaRepository = bolsaRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void carregar() {
        indice.reconstruir(bolsaRepository.findByStatus(StatusBolsa.DISPONIVEL));
    }
}
