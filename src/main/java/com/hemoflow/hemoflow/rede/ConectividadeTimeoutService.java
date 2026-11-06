package com.hemoflow.hemoflow.rede;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hemoflow.hemoflow.dominio.NoRede;
import com.hemoflow.hemoflow.dominio.StatusConectividade;
import com.hemoflow.hemoflow.persistencia.NoRedeRepository;

@Service
public class ConectividadeTimeoutService {

    private final NoRedeRepository noRedeRepository;
    private final int timeoutSegundos;

    public ConectividadeTimeoutService(
            NoRedeRepository noRedeRepository,
            @Value("${hemoflow.demo.timeout-conectividade-segundos:120}") int timeoutSegundos
    ) {
        if (timeoutSegundos <= 0) {
            throw new IllegalArgumentException("O timeout de conectividade deve ser positivo");
        }
        this.noRedeRepository = noRedeRepository;
        this.timeoutSegundos = timeoutSegundos;
    }

    @Scheduled(fixedDelayString = "${hemoflow.demo.verificacao-timeout-ms:10000}")
    @Transactional
    public void verificarTimeouts() {
        LocalDateTime limite = LocalDateTime.now().minusSeconds(timeoutSegundos);
        List<NoRede> expirados = noRedeRepository.findByAtualizadoStatusEmBeforeAndStatusConectividadeNot(
                limite, StatusConectividade.OFFLINE);
        expirados.forEach(NoRede::marcarOffline);
        noRedeRepository.saveAll(expirados);
    }
}
