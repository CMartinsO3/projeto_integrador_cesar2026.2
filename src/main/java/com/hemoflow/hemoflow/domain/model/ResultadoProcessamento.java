package com.hemoflow.hemoflow.domain.model;

import com.hemoflow.hemoflow.domain.entity.BolsaComponente;
import java.util.List;

/**
 * Resultado do processamento de priorização de bolsas.
 * Contém as bolsas ordenadas e estatísticas do processamento.
 */
public class ResultadoProcessamento {
    
    private List<BolsaComponente> bolsasPriorizadas;
    private int totalProcessadas;
    private int totalCompativeis;
    private int bolsasVencidas;
    private int bolsasProximasVencimento; // < 7 dias
    private long tempoProcessamentoMs;
    
    public ResultadoProcessamento() {
    }
    
    public ResultadoProcessamento(List<BolsaComponente> bolsasPriorizadas, 
                                   int totalProcessadas,
                                   int totalCompativeis,
                                   int bolsasVencidas,
                                   int bolsasProximasVencimento,
                                   long tempoProcessamentoMs) {
        this.bolsasPriorizadas = bolsasPriorizadas;
        this.totalProcessadas = totalProcessadas;
        this.totalCompativeis = totalCompativeis;
        this.bolsasVencidas = bolsasVencidas;
        this.bolsasProximasVencimento = bolsasProximasVencimento;
        this.tempoProcessamentoMs = tempoProcessamentoMs;
    }
    
    // Getters and Setters
    public List<BolsaComponente> getBolsasPriorizadas() {
        return bolsasPriorizadas;
    }
    
    public void setBolsasPriorizadas(List<BolsaComponente> bolsasPriorizadas) {
        this.bolsasPriorizadas = bolsasPriorizadas;
    }
    
    public int getTotalProcessadas() {
        return totalProcessadas;
    }
    
    public void setTotalProcessadas(int totalProcessadas) {
        this.totalProcessadas = totalProcessadas;
    }
    
    public int getTotalCompativeis() {
        return totalCompativeis;
    }
    
    public void setTotalCompativeis(int totalCompativeis) {
        this.totalCompativeis = totalCompativeis;
    }
    
    public int getBolsasVencidas() {
        return bolsasVencidas;
    }
    
    public void setBolsasVencidas(int bolsasVencidas) {
        this.bolsasVencidas = bolsasVencidas;
    }
    
    public int getBolsasProximasVencimento() {
        return bolsasProximasVencimento;
    }
    
    public void setBolsasProximasVencimento(int bolsasProximasVencimento) {
        this.bolsasProximasVencimento = bolsasProximasVencimento;
    }
    
    public long getTempoProcessamentoMs() {
        return tempoProcessamentoMs;
    }
    
    public void setTempoProcessamentoMs(long tempoProcessamentoMs) {
        this.tempoProcessamentoMs = tempoProcessamentoMs;
    }
}
