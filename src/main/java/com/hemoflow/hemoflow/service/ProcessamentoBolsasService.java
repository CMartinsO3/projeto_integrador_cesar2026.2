package com.hemoflow.hemoflow.service;

import com.hemoflow.hemoflow.domain.entity.BolsaComponente;
import com.hemoflow.hemoflow.domain.enums.FatorRh;
import com.hemoflow.hemoflow.domain.enums.TipoABO;
import com.hemoflow.hemoflow.domain.enums.TipoComponente;
import com.hemoflow.hemoflow.domain.model.CompatibilidadeABO;
import com.hemoflow.hemoflow.domain.model.ResultadoProcessamento;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Serviço responsável pelo processamento e priorização de bolsas de sangue.
 * 
 * Complexidade: O(n log n)
 * - Filtragem por compatibilidade: O(n)
 * - Ordenação por data de validade (FEFO): O(n log n)
 * - Detecção de vencimento: O(n)
 * 
 * Complexidade dominante: O(n log n) devido à ordenação
 */
@Service
public class ProcessamentoBolsasService {
    
    private static final int DIAS_ALERTA_VENCIMENTO = 7;
    
    /**
     * VERSÃO SEQUENCIAL
     * Processa todas as bolsas em uma única thread.
     * 
     * @param bolsas Lista de bolsas a processar
     * @param tipoReceptor Tipo ABO do receptor
     * @param fatorRhReceptor Fator Rh do receptor
     * @param tipoComponente Tipo de componente solicitado
     * @return Resultado com bolsas priorizadas e estatísticas
     */
    public ResultadoProcessamento processarSequencial(List<BolsaComponente> bolsas,
                                                      TipoABO tipoReceptor,
                                                      FatorRh fatorRhReceptor,
                                                      TipoComponente tipoComponente) {
        
        long inicio = System.nanoTime();
        
        // Etapa 1: Filtrar por compatibilidade ABO/Rh e tipo de componente - O(n)
        List<BolsaComponente> bolsasCompativeis = filtrarCompativeis(
                bolsas, tipoReceptor, fatorRhReceptor, tipoComponente);
        
        // Etapa 2: Ordenar por data de validade (FEFO) - O(n log n)
        bolsasCompativeis.sort(Comparator.comparing(BolsaComponente::getDataValidade));
        
        // Etapa 3: Calcular estatísticas - O(n)
        int bolsasVencidas = 0;
        int bolsasProximasVencimento = 0;
        
        for (BolsaComponente bolsa : bolsasCompativeis) {
            if (bolsa.isVencida()) {
                bolsasVencidas++;
            } else if (bolsa.diasParaVencimento() <= DIAS_ALERTA_VENCIMENTO) {
                bolsasProximasVencimento++;
            }
        }
        
        long fim = System.nanoTime();
        long tempoMs = TimeUnit.NANOSECONDS.toMillis(fim - inicio);
        
        return new ResultadoProcessamento(
                bolsasCompativeis,
                bolsas.size(),
                bolsasCompativeis.size(),
                bolsasVencidas,
                bolsasProximasVencimento,
                tempoMs
        );
    }
    
    /**
     * VERSÃO PARALELA
     * Divide o processamento entre múltiplas threads.
     * 
     * Estratégia:
     * 1. Dividir a lista de bolsas em partições (uma por thread)
     * 2. Cada thread processa sua partição independentemente:
     *    - Filtra bolsas compatíveis
     *    - Calcula estatísticas locais
     * 3. Agregar resultados de todas as threads
     * 4. Ordenar o resultado final
     * 
     * @param bolsas Lista de bolsas a processar
     * @param tipoReceptor Tipo ABO do receptor
     * @param fatorRhReceptor Fator Rh do receptor
     * @param tipoComponente Tipo de componente solicitado
     * @param numeroThreads Número de threads a utilizar
     * @return Resultado com bolsas priorizadas e estatísticas
     */
    public ResultadoProcessamento processarParalelo(List<BolsaComponente> bolsas,
                                                    TipoABO tipoReceptor,
                                                    FatorRh fatorRhReceptor,
                                                    TipoComponente tipoComponente,
                                                    int numeroThreads) 
            throws InterruptedException, ExecutionException {
        
        long inicio = System.nanoTime();
        
        // Criar ExecutorService com número fixo de threads
        ExecutorService executor = Executors.newFixedThreadPool(numeroThreads);
        
        try {
            // Calcular tamanho de cada partição
            int tamanhoPorThread = (int) Math.ceil((double) bolsas.size() / numeroThreads);
            
            // Lista para armazenar as tarefas (Futures)
            List<Future<ResultadoParcial>> futures = new ArrayList<>();
            
            // Criar e submeter tarefas para cada thread
            for (int i = 0; i < numeroThreads; i++) {
                int inicio_particao = i * tamanhoPorThread;
                int fim_particao = Math.min((i + 1) * tamanhoPorThread, bolsas.size());
                
                // Não criar tarefa se a partição está vazia
                if (inicio_particao >= bolsas.size()) {
                    break;
                }
                
                List<BolsaComponente> particao = bolsas.subList(inicio_particao, fim_particao);
                
                // Submeter tarefa callable
                Callable<ResultadoParcial> tarefa = () -> processarParticao(
                        particao, tipoReceptor, fatorRhReceptor, tipoComponente);
                
                futures.add(executor.submit(tarefa));
            }
            
            // Aguardar conclusão de todas as threads e agregar resultados
            List<BolsaComponente> todasBolsasCompativeis = new ArrayList<>();
            int totalVencidas = 0;
            int totalProximasVencimento = 0;
            
            for (Future<ResultadoParcial> future : futures) {
                ResultadoParcial resultado = future.get(); // Bloqueia até a thread terminar
                todasBolsasCompativeis.addAll(resultado.bolsasCompativeis);
                totalVencidas += resultado.bolsasVencidas;
                totalProximasVencimento += resultado.bolsasProximasVencimento;
            }
            
            // Ordenação final após agregação - O(n log n)
            todasBolsasCompativeis.sort(Comparator.comparing(BolsaComponente::getDataValidade));
            
            long fim = System.nanoTime();
            long tempoMs = TimeUnit.NANOSECONDS.toMillis(fim - inicio);
            
            return new ResultadoProcessamento(
                    todasBolsasCompativeis,
                    bolsas.size(),
                    todasBolsasCompativeis.size(),
                    totalVencidas,
                    totalProximasVencimento,
                    tempoMs
            );
            
        } finally {
            // Sempre encerrar o executor
            executor.shutdown();
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        }
    }
    
    /**
     * Processa uma partição de bolsas em uma thread.
     * Cada thread trabalha de forma independente em sua própria estrutura de dados.
     * 
     * @param particao Subconjunto de bolsas a processar
     * @return Resultado parcial desta partição
     */
    private ResultadoParcial processarParticao(List<BolsaComponente> particao,
                                               TipoABO tipoReceptor,
                                               FatorRh fatorRhReceptor,
                                               TipoComponente tipoComponente) {
        
        // Cada thread trabalha em suas próprias estruturas locais (thread-safe por design)
        List<BolsaComponente> compativeis = filtrarCompativeis(
                particao, tipoReceptor, fatorRhReceptor, tipoComponente);
        
        int vencidas = 0;
        int proximasVencimento = 0;
        
        for (BolsaComponente bolsa : compativeis) {
            if (bolsa.isVencida()) {
                vencidas++;
            } else if (bolsa.diasParaVencimento() <= DIAS_ALERTA_VENCIMENTO) {
                proximasVencimento++;
            }
        }
        
        return new ResultadoParcial(compativeis, vencidas, proximasVencimento);
    }
    
    /**
     * Filtra bolsas compatíveis com o receptor e tipo de componente.
     * Complexidade: O(n)
     */
    private List<BolsaComponente> filtrarCompativeis(List<BolsaComponente> bolsas,
                                                     TipoABO tipoReceptor,
                                                     FatorRh fatorRhReceptor,
                                                     TipoComponente tipoComponente) {
        return bolsas.stream()
                .filter(bolsa -> bolsa.getTipoComponente() == tipoComponente)
                .filter(bolsa -> CompatibilidadeABO.isCompativel(
                        bolsa.getTipoAbo(),
                        bolsa.getFatorRh(),
                        tipoReceptor,
                        fatorRhReceptor))
                .collect(Collectors.toList());
    }
    
    /**
     * Classe interna para armazenar resultado parcial de cada thread.
     * Imutável para garantir thread-safety.
     */
    private static class ResultadoParcial {
        final List<BolsaComponente> bolsasCompativeis;
        final int bolsasVencidas;
        final int bolsasProximasVencimento;
        
        ResultadoParcial(List<BolsaComponente> bolsasCompativeis,
                        int bolsasVencidas,
                        int bolsasProximasVencimento) {
            this.bolsasCompativeis = bolsasCompativeis;
            this.bolsasVencidas = bolsasVencidas;
            this.bolsasProximasVencimento = bolsasProximasVencimento;
        }
    }
}
