package com.hemoflow.hemoflow.service;

import com.hemoflow.hemoflow.domain.entity.BolsaComponente;
import com.hemoflow.hemoflow.domain.enums.FatorRh;
import com.hemoflow.hemoflow.domain.enums.StatusBolsa;
import com.hemoflow.hemoflow.domain.enums.TipoABO;
import com.hemoflow.hemoflow.domain.enums.TipoComponente;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Serviço para gerar dados sintéticos determinísticos para benchmark.
 * Os dados gerados são consistentes e permitem testes reproduzíveis.
 */
@Service
public class GeradorDadosBenchmarkService {
    
    private static final int SEED = 42; // Seed fixa para resultados determinísticos
    private final Random random = new Random(SEED);
    
    private final TipoABO[] tiposABO = TipoABO.values();
    private final FatorRh[] fatoresRh = FatorRh.values();
    private final TipoComponente[] tiposComponente = TipoComponente.values();
    
    /**
     * Gera uma lista de bolsas sintéticas para benchmark.
     * 
     * Distribuição aproximada:
     * - 40% O (mais comum)
     * - 30% A
     * - 20% B
     * - 10% AB (mais raro)
     * 
     * - 85% Rh+ (mais comum)
     * - 15% Rh- (mais raro)
     * 
     * @param quantidade Número de bolsas a gerar
     * @return Lista de bolsas geradas
     */
    public List<BolsaComponente> gerarBolsas(int quantidade) {
        List<BolsaComponente> bolsas = new ArrayList<>(quantidade);
        
        // Resetar seed para garantir determinismo
        random.setSeed(SEED);
        
        LocalDate hoje = LocalDate.now();
        
        for (int i = 0; i < quantidade; i++) {
            BolsaComponente bolsa = new BolsaComponente();
            
            // Tipo ABO com distribuição realista
            TipoABO tipoABO = gerarTipoABOComDistribuicao();
            bolsa.setTipoAbo(tipoABO);
            
            // Fator Rh com distribuição realista
            FatorRh fatorRh = random.nextInt(100) < 85 ? FatorRh.POSITIVO : FatorRh.NEGATIVO;
            bolsa.setFatorRh(fatorRh);
            
            // Tipo de componente distribuído uniformemente
            TipoComponente tipoComponente = tiposComponente[random.nextInt(tiposComponente.length)];
            bolsa.setTipoComponente(tipoComponente);
            
            // Volume entre 250ml e 450ml
            int volume = 250 + random.nextInt(201);
            bolsa.setVolumeMl(volume);
            
            // Data de produção: entre 60 dias atrás e hoje
            int diasAtras = random.nextInt(61);
            LocalDate dataProducao = hoje.minusDays(diasAtras);
            bolsa.setDataProducao(dataProducao);
            
            // Data de validade calculada automaticamente pelo componente
            // (será feita no @PrePersist, mas vamos calcular manualmente aqui)
            LocalDate dataValidade = dataProducao.plusDays(tipoComponente.getDiasValidade());
            bolsa.setDataValidade(dataValidade);
            
            // Status: maioria disponível
            bolsa.setStatus(StatusBolsa.DISPONIVEL);
            
            // Código de rastreio único
            bolsa.setCodigoRastreio("BM-" + String.format("%06d", i));
            
            bolsas.add(bolsa);
        }
        
        return bolsas;
    }
    
    /**
     * Gera tipo ABO com distribuição realista:
     * - 40% O
     * - 30% A
     * - 20% B
     * - 10% AB
     */
    private TipoABO gerarTipoABOComDistribuicao() {
        int valor = random.nextInt(100);
        
        if (valor < 40) {
            return TipoABO.O;
        } else if (valor < 70) {
            return TipoABO.A;
        } else if (valor < 90) {
            return TipoABO.B;
        } else {
            return TipoABO.AB;
        }
    }
    
    /**
     * Retorna a seed utilizada para geração determinística.
     */
    public int getSeed() {
        return SEED;
    }
}
