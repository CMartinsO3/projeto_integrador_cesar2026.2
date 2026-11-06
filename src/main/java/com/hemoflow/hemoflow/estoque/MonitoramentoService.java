package com.hemoflow.hemoflow.estoque;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hemoflow.hemoflow.api.RegraNegocioException;
import com.hemoflow.hemoflow.api.dto.MonitoramentoDTOs;
import com.hemoflow.hemoflow.config.DemoMonitoramentoProperties;
import com.hemoflow.hemoflow.dominio.Bolsa;
import com.hemoflow.hemoflow.dominio.Hemocomponente;
import com.hemoflow.hemoflow.dominio.LeituraTemperatura;
import com.hemoflow.hemoflow.dominio.NoRede;
import com.hemoflow.hemoflow.dominio.StatusBolsa;
import com.hemoflow.hemoflow.persistencia.BolsaRepository;
import com.hemoflow.hemoflow.persistencia.LeituraTemperaturaRepository;
import com.hemoflow.hemoflow.persistencia.NoRedeRepository;

@Service
public class MonitoramentoService {

    private final BolsaRepository bolsaRepository;
    private final LeituraTemperaturaRepository leituraRepository;
    private final NoRedeRepository noRedeRepository;
    private final DemoMonitoramentoProperties properties;

    public MonitoramentoService(
            BolsaRepository bolsaRepository,
            LeituraTemperaturaRepository leituraRepository,
            NoRedeRepository noRedeRepository,
            DemoMonitoramentoProperties properties
    ) {
        this.bolsaRepository = bolsaRepository;
        this.leituraRepository = leituraRepository;
        this.noRedeRepository = noRedeRepository;
        this.properties = properties;
    }

    @Transactional(readOnly = true)
    public MonitoramentoDTOs.Painel consultarPainel() {
        List<MonitoramentoDTOs.Faixa> faixas = java.util.Arrays.stream(Hemocomponente.values())
                .map(hemocomponente -> MonitoramentoDTOs.Faixa.de(
                        hemocomponente, properties.faixaTemperatura(hemocomponente)))
                .toList();
        List<MonitoramentoDTOs.Leitura> leituras = bolsaRepository.findByStatus(StatusBolsa.EM_TRANSITO).stream()
                .map(bolsa -> leituraRepository.findTopByBolsaIdOrderByRegistradaEmDesc(bolsa.getId()))
                .flatMap(java.util.Optional::stream)
                .map(MonitoramentoDTOs.Leitura::de)
                .toList();
        List<MonitoramentoDTOs.Dispositivo> dispositivos = noRedeRepository.findAll().stream()
                .map(MonitoramentoDTOs.Dispositivo::de)
                .toList();
        return new MonitoramentoDTOs.Painel(
                properties.getLimiteRotaMinutos(),
                properties.getIntervaloLeituraSegundos(),
                "Simulação didática. Valores fictícios, sem validade clínica ou operacional.",
                faixas,
                leituras,
                dispositivos
        );
    }

    @Transactional
    public MonitoramentoDTOs.Leitura registrarLeitura(MonitoramentoDTOs.RegistroTemperatura dto) {
        if (dto.temperaturaCelsius() == null || !Double.isFinite(dto.temperaturaCelsius())
                || dto.temperaturaCelsius() < -100 || dto.temperaturaCelsius() > 100) {
            throw new RegraNegocioException("A temperatura de demonstração deve ser finita entre -100 e 100 °C");
        }
        Bolsa bolsa = bolsaRepository.findById(dto.bolsaId())
                .orElseThrow(() -> new RegraNegocioException("Bolsa não encontrada: " + dto.bolsaId()));
        if (bolsa.getStatus() != StatusBolsa.EM_TRANSITO) {
            throw new RegraNegocioException("Só é possível registrar leitura para bolsa em trânsito");
        }

        DemoMonitoramentoProperties.FaixaTemperatura faixa =
                properties.faixaTemperatura(bolsa.getHemocomponente());
        boolean alerta = dto.temperaturaCelsius() < faixa.minimoCelsius()
                || dto.temperaturaCelsius() > faixa.maximoCelsius();
        LeituraTemperatura leitura = leituraRepository.save(new LeituraTemperatura(
                bolsa,
                dto.temperaturaCelsius(),
                faixa.minimoCelsius(),
                faixa.maximoCelsius(),
                alerta
        ));
        return MonitoramentoDTOs.Leitura.de(leitura);
    }

    @Transactional
    public List<MonitoramentoDTOs.Leitura> simularLeituras(boolean forcarAlerta) {
        return bolsaRepository.findByStatus(StatusBolsa.EM_TRANSITO).stream()
                .map(bolsa -> simularLeitura(bolsa, forcarAlerta))
                .toList();
    }

    private MonitoramentoDTOs.Leitura simularLeitura(Bolsa bolsa, boolean forcarAlerta) {
        DemoMonitoramentoProperties.FaixaTemperatura faixa =
                properties.faixaTemperatura(bolsa.getHemocomponente());
        double temperatura;
        if (forcarAlerta) {
            temperatura = faixa.maximoCelsius() + 1.0;
        } else {
            double largura = faixa.maximoCelsius() - faixa.minimoCelsius();
            temperatura = faixa.minimoCelsius()
                    + ThreadLocalRandom.current().nextDouble(largura);
        }
        return registrarLeitura(new MonitoramentoDTOs.RegistroTemperatura(bolsa.getId(), temperatura));
    }

    @Transactional(readOnly = true)
    public Bolsa buscarBolsaEmTransito(Long bolsaId) {
        return bolsaRepository.findById(bolsaId)
                .filter(bolsa -> bolsa.getStatus() == StatusBolsa.EM_TRANSITO)
                .orElseThrow(() -> new RegraNegocioException("Bolsa em trânsito não encontrada: " + bolsaId));
    }
}
