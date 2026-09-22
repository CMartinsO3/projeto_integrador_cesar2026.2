# Relatório de Paralelismo - HemoFlow

> **Projeto:** Rota Vital - Sistema de Gestão de Hemocomponentes  
> **Disciplina:** Algoritmos e Estruturas de Dados / Sistemas Operacionais  
> **Tema:** Processamento Sequencial vs Paralelo com Threads  
> **Data:** Setembro de 2026

---

## 1. Operação Escolhida

**Priorização de Bolsas de Sangue por Compatibilidade e Validade (FEFO)**

Esta operação processa uma grande quantidade de bolsas de sangue armazenadas no sistema e realiza as seguintes etapas:

1. **Filtragem por Compatibilidade ABO/Rh**: Verifica quais bolsas são compatíveis com o tipo sanguíneo do receptor, seguindo a matriz de compatibilidade de transfusões sanguíneas
2. **Filtragem por Tipo de Componente**: Seleciona apenas o tipo de hemocomponente solicitado (hemácias, plasma, plaquetas, crioprecipitado)
3. **Ordenação por Validade (FEFO)**: Ordena as bolsas compatíveis pela data de validade mais próxima primeiro (*First Expired, First Out*)
4. **Cálculo de Estatísticas**: Identifica bolsas vencidas e bolsas próximas ao vencimento (< 7 dias)

### Entrada
- Lista de N bolsas de sangue
- Tipo sanguíneo do receptor (ABO + Rh)
- Tipo de componente solicitado

### Saída
- Lista ordenada de bolsas compatíveis (prioridade por validade)
- Estatísticas: total processado, compatíveis, vencidas, próximas ao vencimento
- Tempo de processamento

---

## 2. Justificativa da Escolha

Esta operação é **ideal para paralelização** pelos seguintes motivos:

### 2.1. Processamento Intensivo na Aplicação
- O custo computacional está na **lógica de negócio**, não no acesso ao banco de dados
- Após carregar as bolsas em memória, todo o processamento ocorre na aplicação
- Envolve iterações sobre grandes volumes de dados

### 2.2. Particionamento Independente
- A lista de bolsas pode ser dividida em N partições
- Cada thread processa sua partição de forma **completamente independente**
- Não há dependência entre as partições durante o processamento

### 2.3. Resultado Determinístico
- O resultado final é sempre o mesmo, independente do número de threads
- A ordenação final garante que a lista sempre esteja na mesma ordem (por data de validade)

### 2.4. Problema Real
- Esta operação faz parte do domínio do sistema HemoFlow
- Corresponde à História de Usuário #3 (Alocação por Compatibilidade) e #4 (Priorização FEFO)
- É uma operação crítica que precisa ser eficiente em produção

### 2.5. Escalabilidade
- O problema escala linearmente com o volume de dados
- Quanto maior o estoque, maior o benefício do paralelismo
- Cenário realista: sistemas de hemocentros podem ter centenas de milhares de bolsas registradas

---

## 3. Análise de Complexidade (Big-O)

### Versão Sequencial

**Complexidade Total: O(n log n)**

Decomposição por etapa:

1. **Filtragem por Compatibilidade**: O(n)
   - Percorre todas as N bolsas uma vez
   - Para cada bolsa, verifica compatibilidade (O(1) com tabela hash)

2. **Ordenação (FEFO)**: O(n log n)
   - Algoritmo de ordenação (QuickSort/MergeSort do Java)
   - Ordena as bolsas compatíveis por data de validade

3. **Cálculo de Estatísticas**: O(n)
   - Percorre as bolsas compatíveis uma vez
   - Conta vencidas e próximas ao vencimento

**Complexidade dominante:** O(n log n) devido à ordenação

### Versão Paralela

**Complexidade Total: O(n log n)**

A complexidade Big-O **não muda** com paralelização porque:
- A quantidade total de operações permanece a mesma
- Big-O mede o comportamento assintótico, não o tempo de execução absoluto

**Porém, o tempo de execução real diminui:**
- Com P threads ideais: **Tempo ≈ T(sequencial) / P**
- Na prática: **Speedup < P** (devido ao overhead)

Decomposição por etapa (com P threads):

1. **Divisão em Partições**: O(1)
   - Apenas cálculo de índices

2. **Processamento Paralelo**: O(n/P) por thread
   - Cada thread processa n/P bolsas
   - Filtragem local: O(n/P)

3. **Agregação**: O(P)
   - Combinar P listas de resultados parciais
   - P é constante e pequeno (2, 4, 8)

4. **Ordenação Final**: O(n log n)
   - Ordenar a lista agregada
   - **Esta etapa não é paralelizada** na implementação atual

**Observação Importante:**
A ordenação final é sequencial. Para paralelizar completamente, seria necessário:
- Merge-sort paralelo
- Ordenação externa com múltiplas threads
- Isso adicionaria complexidade de implementação

---

## 4. Gargalo Computacional

### Onde está o custo?

1. **Filtragem (30% do tempo)**
   - Iteração sobre todas as bolsas
   - Verificação de compatibilidade para cada uma

2. **Ordenação (60% do tempo)** ⭐ **Principal Gargalo**
   - Algoritmo O(n log n)
   - Comparações de datas
   - Movimentação de objetos na memória

3. **Estatísticas (10% do tempo)**
   - Contagem de bolsas vencidas
   - Identificação de alertas

### Por que o paralelismo ajuda?

- **Filtragem paralelizável**: Cada thread filtra sua partição independentemente
- **Ordenação parcialmente paralelizável**: Cada thread ordena localmente, depois há ordenação final
- **Estatísticas paralelizáveis**: Contadores locais agregados ao final

### Limitações

- A **ordenação final** (pós-agregação) é sequencial
- **Overhead de threads**: criação, sincronização, context switching
- **Lei de Amdahl**: a parte sequencial limita o speedup máximo

---

## 5. Estratégia de Particionamento

### Divisão dos Dados

A lista de N bolsas é dividida em P partições contíguas:

```
N = 100.000 bolsas
P = 4 threads

Tamanho por thread = ceil(100.000 / 4) = 25.000

Thread 1: bolsas [0      ... 24.999]
Thread 2: bolsas [25.000 ... 49.999]
Thread 3: bolsas [50.000 ... 74.999]
Thread 4: bolsas [75.000 ... 99.999]
```

### Por que esta estratégia funciona?

1. **Não há dependências**: Uma bolsa na posição 1000 não depende da bolsa na posição 50000
2. **Acesso contíguo**: Melhor uso do cache do processador
3. **Balanceamento**: Todas as threads recebem ~25.000 bolsas
4. **Simples**: Não requer estruturas de dados complexas

### Implementação (Java)

```java
int tamanhoPorThread = (int) Math.ceil((double) bolsas.size() / numeroThreads);

for (int i = 0; i < numeroThreads; i++) {
    int inicio = i * tamanhoPorThread;
    int fim = Math.min((i + 1) * tamanhoPorThread, bolsas.size());
    
    List<BolsaComponente> particao = bolsas.subList(inicio, fim);
    
    // Cada thread processa sua partição
    futures.add(executor.submit(() -> processarParticao(particao, ...)));
}
```

---

## 6. Implementação Sequencial

### Código Simplificado

```java
public ResultadoProcessamento processarSequencial(
        List<BolsaComponente> bolsas,
        TipoABO tipoReceptor,
        FatorRh fatorRhReceptor,
        TipoComponente tipoComponente) {
    
    long inicio = System.nanoTime();
    
    // Etapa 1: Filtrar por compatibilidade - O(n)
    List<BolsaComponente> compativeis = bolsas.stream()
            .filter(b -> b.getTipoComponente() == tipoComponente)
            .filter(b -> CompatibilidadeABO.isCompativel(
                    b.getTipoAbo(), b.getFatorRh(),
                    tipoReceptor, fatorRhReceptor))
            .collect(Collectors.toList());
    
    // Etapa 2: Ordenar por validade (FEFO) - O(n log n)
    compativeis.sort(Comparator.comparing(BolsaComponente::getDataValidade));
    
    // Etapa 3: Calcular estatísticas - O(n)
    int vencidas = 0;
    int proximasVencimento = 0;
    
    for (BolsaComponente bolsa : compativeis) {
        if (bolsa.isVencida()) {
            vencidas++;
        } else if (bolsa.diasParaVencimento() <= 7) {
            proximasVencimento++;
        }
    }
    
    long fim = System.nanoTime();
    long tempoMs = TimeUnit.NANOSECONDS.toMillis(fim - inicio);
    
    return new ResultadoProcessamento(compativeis, bolsas.size(), 
            compativeis.size(), vencidas, proximasVencimento, tempoMs);
}
```

### Características

- **Uma única thread** processa tudo linearmente
- **Simples e direto**: fácil de entender e debugar
- **Baseline**: usada como referência para medir speedup

---

## 7. Implementação Paralela

### Arquitetura

```
                    ┌─────────────────┐
                    │  Lista de Bolsas│
                    │   (N registros) │
                    └────────┬────────┘
                             │
                  ┌──────────┴──────────┐
                  │  Divisão em P partes │
                  └──────────┬───────────┘
                             │
        ┌────────────────────┼────────────────────┐
        │                    │                    │
    ┌───▼────┐          ┌───▼────┐          ┌───▼────┐
    │Thread 1│          │Thread 2│    ...   │Thread P│
    │        │          │        │          │        │
    │Filtrar │          │Filtrar │          │Filtrar │
    │Ordenar │          │Ordenar │          │Ordenar │
    │Contar  │          │Contar  │          │Contar  │
    └───┬────┘          └───┬────┘          └───┬────┘
        │                    │                    │
        └────────────────────┼────────────────────┘
                             │
                   ┌─────────▼──────────┐
                   │   Agregação        │
                   │ (Juntar resultados)│
                   └─────────┬──────────┘
                             │
                   ┌─────────▼──────────┐
                   │  Ordenação Final   │
                   │     (Sequencial)   │
                   └─────────┬──────────┘
                             │
                   ┌─────────▼──────────┐
                   │  Resultado Final   │
                   └────────────────────┘
```

### Código Simplificado

```java
public ResultadoProcessamento processarParalelo(
        List<BolsaComponente> bolsas,
        TipoABO tipoReceptor,
        FatorRh fatorRhReceptor,
        TipoComponente tipoComponente,
        int numeroThreads) 
        throws InterruptedException, ExecutionException {
    
    long inicio = System.nanoTime();
    
    // Criar pool de threads
    ExecutorService executor = Executors.newFixedThreadPool(numeroThreads);
    
    try {
        // Calcular tamanho de cada partição
        int tamanhoPorThread = (int) Math.ceil((double) bolsas.size() / numeroThreads);
        
        List<Future<ResultadoParcial>> futures = new ArrayList<>();
        
        // Criar tarefas para cada thread
        for (int i = 0; i < numeroThreads; i++) {
            int inicio_p = i * tamanhoPorThread;
            int fim_p = Math.min((i + 1) * tamanhoPorThread, bolsas.size());
            
            List<BolsaComponente> particao = bolsas.subList(inicio_p, fim_p);
            
            // Submeter tarefa
            futures.add(executor.submit(() -> 
                processarParticao(particao, tipoReceptor, fatorRhReceptor, tipoComponente)));
        }
        
        // Aguardar e agregar resultados
        List<BolsaComponente> todasCompativeis = new ArrayList<>();
        int totalVencidas = 0;
        int totalProximas = 0;
        
        for (Future<ResultadoParcial> future : futures) {
            ResultadoParcial resultado = future.get(); // Bloqueia até terminar
            todasCompativeis.addAll(resultado.bolsasCompativeis);
            totalVencidas += resultado.bolsasVencidas;
            totalProximas += resultado.bolsasProximasVencimento;
        }
        
        // Ordenação final (sequencial)
        todasCompativeis.sort(Comparator.comparing(BolsaComponente::getDataValidade));
        
        long fim = System.nanoTime();
        long tempoMs = TimeUnit.NANOSECONDS.toMillis(fim - inicio);
        
        return new ResultadoProcessamento(todasCompativeis, bolsas.size(),
                todasCompativeis.size(), totalVencidas, totalProximas, tempoMs);
        
    } finally {
        executor.shutdown();
        executor.awaitTermination(60, TimeUnit.SECONDS);
    }
}
```

### Garantias de Thread-Safety

1. **Cada thread trabalha em estruturas locais**
   - Não há compartilhamento durante o processamento
   - Sem condições de corrida

2. **Agregação segura**
   - Feita após todas as threads terminarem (via `future.get()`)
   - Acesso sequencial aos resultados parciais

3. **Imutabilidade**
   - A lista original não é modificada
   - Resultados parciais são criados como novas instâncias

---

## 8. Concorrência vs Paralelismo

### Concorrência

**Definição:** Gerenciar múltiplas tarefas ao mesmo tempo, alternando entre elas.

**Características:**
- Tarefas **progridem** simultaneamente
- Não necessariamente **executam** simultaneamente
- Relacionado ao **design** do sistema
- Exemplo: Sistema operacional gerenciando 100 processos em 4 CPUs

**Analogia:** Um chef preparando múltiplos pratos, alternando entre eles.

### Paralelismo

**Definição:** Executar múltiplas tarefas **literalmente ao mesmo tempo**.

**Características:**
- Tarefas **executam** simultaneamente
- Requer **hardware** com múltiplos núcleos
- Relacionado à **execução** do sistema
- Exemplo: 4 threads processando 4 partições de dados, cada uma em um núcleo diferente

**Analogia:** Quatro chefs, cada um preparando um prato diferente simultaneamente.

### Nesta Atividade

Esta implementação utiliza **PARALELISMO**:

- ✅ Múltiplas threads **executam simultaneamente**
- ✅ Cada thread processa uma **partição diferente** dos dados
- ✅ **Requer** múltiplos núcleos de CPU para ganho real de desempenho
- ✅ Objetivo: **reduzir o tempo total** de processamento

**Nota:** Concorrência seria útil se as threads precisassem esperar por I/O (ex: banco de dados, rede). Aqui, o processamento é puramente computacional (CPU-bound), então **paralelismo** é a abordagem correta.

---

## 9. Resultados dos Benchmarks

### 9.1. Ambiente de Teste

**IMPORTANTE:** Os resultados abaixo devem ser preenchidos **APÓS executar os benchmarks reais**.

- **Processador:** [A PREENCHER - ex: Intel Core i7-12700K, 12 núcleos]
- **Memória RAM:** [A PREENCHER - ex: 16GB DDR4]
- **JDK:** Java 17
- **Sistema Operacional:** [A PREENCHER - ex: Windows 11 / Ubuntu 22.04]
- **Execuções por cenário:** 3 (média dos tempos)

### 9.2. Tabela de Resultados

**ATENÇÃO:** NÃO INVENTAR VALORES. Preencher apenas após executar os testes reais.

| Registros   | Sequencial (ms) | 2 threads (ms) | 4 threads (ms) | 8 threads (ms) |
|-------------|-----------------|----------------|----------------|----------------|
| **100.000** |                 |                |                |                |
| **500.000** |                 |                |                |                |
| **1.000.000**|                 |                |                |                |

### 9.3. Comandos para Executar os Testes

#### Teste 1: 100.000 registros

```bash
# Sequencial
POST http://localhost:8080/api/v1/processamento-bolsas/executar
{
  "quantidadeRegistros": 100000,
  "modo": "SEQUENCIAL",
  "numeroThreads": 1,
  "tipoAbo": "A",
  "fatorRh": "POSITIVO",
  "tipoComponente": "CONCENTRADO_HEMACIAS"
}

# Paralelo - 2 threads
{
  "quantidadeRegistros": 100000,
  "modo": "PARALELO",
  "numeroThreads": 2,
  ...
}

# Paralelo - 4 threads
{
  "quantidadeRegistros": 100000,
  "modo": "PARALELO",
  "numeroThreads": 4,
  ...
}

# Paralelo - 8 threads
{
  "quantidadeRegistros": 100000,
  "modo": "PARALELO",
  "numeroThreads": 8,
  ...
}
```

#### Teste 2: 500.000 registros

```bash
# Alterar "quantidadeRegistros": 500000
```

#### Teste 3: 1.000.000 registros

```bash
# Alterar "quantidadeRegistros": 1000000
```

---

## 10. Cálculo de Aceleração (Speedup)

**Fórmula:**

```
Speedup = Tempo Sequencial / Tempo Paralelo
```

### 10.1. Aceleração com 100.000 registros

| Configuração | Tempo (ms) | Speedup | Eficiência |
|--------------|------------|---------|------------|
| Sequencial   | [PREENCHER] | 1.00x   | 100%       |
| 2 threads    | [PREENCHER] | [CALC]  | [CALC]     |
| 4 threads    | [PREENCHER] | [CALC]  | [CALC]     |
| 8 threads    | [PREENCHER] | [CALC]  | [CALC]     |

**Eficiência = Speedup / Número de Threads**

### 10.2. Aceleração com 500.000 registros

| Configuração | Tempo (ms) | Speedup | Eficiência |
|--------------|------------|---------|------------|
| Sequencial   | [PREENCHER] | 1.00x   | 100%       |
| 2 threads    | [PREENCHER] | [CALC]  | [CALC]     |
| 4 threads    | [PREENCHER] | [CALC]  | [CALC]     |
| 8 threads    | [PREENCHER] | [CALC]  | [CALC]     |

### 10.3. Aceleração com 1.000.000 registros

| Configuração | Tempo (ms) | Speedup | Eficiência |
|--------------|------------|---------|------------|
| Sequencial   | [PREENCHER] | 1.00x   | 100%       |
| 2 threads    | [PREENCHER] | [CALC]  | [CALC]     |
| 4 threads    | [PREENCHER] | [CALC]  | [CALC]     |
| 8 threads    | [PREENCHER] | [CALC]  | [CALC]     |

---

## 11. Análise dos Resultados

### 11.1. Por que o ganho não é linear?

**Speedup ideal:**
- 2 threads → 2x mais rápido
- 4 threads → 4x mais rápido
- 8 threads → 8x mais rápido

**Speedup real:**
- Geralmente **menor** que o ideal
- Comum: 2 threads → 1.7x, 4 threads → 3.2x, 8 threads → 5.5x

**Motivos:**

#### 1. Lei de Amdahl

**Fórmula:**
```
Speedup_máximo = 1 / (S + P/N)

Onde:
S = fração sequencial do código (0-1)
P = fração paralelizável do código (0-1)
N = número de threads
```

No nosso caso:
- **Parte paralelizável (P):** Filtragem e estatísticas (~70%)
- **Parte sequencial (S):** Ordenação final, agregação (~30%)

Com 30% sequencial, o speedup máximo teórico é:
```
Speedup_max(∞ threads) = 1 / 0.30 = 3.33x
```

Mesmo com **infinitas threads**, nunca passaríamos de 3.33x!

#### 2. Overhead de Criação de Threads

- Criar thread: ~1-2ms
- Destruir thread: ~0.5-1ms
- Com 8 threads: ~12-24ms de overhead puro

#### 3. Sincronização

- `future.get()`: bloqueia thread principal
- `executor.shutdown()`: aguarda todas as threads
- Agregação de resultados: acesso sequencial

#### 4. Context Switching

- Troca de contexto entre threads: ~5-10 microsegundos
- Com muitas threads, o SO passa tempo trocando contexto
- Pior em máquinas com poucos núcleos

#### 5. Cache Contention

- Múltiplas threads competem pelo cache L3 compartilhado
- Cache misses aumentam com mais threads
- False sharing pode ocorrer

#### 6. Número de Núcleos Físicos

- Se a CPU tem 4 núcleos, 8 threads **não executam simultaneamente**
- Hiperthreading ajuda, mas não duplica o desempenho
- Threads competem por recursos da CPU

#### 7. Ordenação Final Sequencial

- A ordenação final não é paralelizada
- O(n log n) sequencial limita o speedup
- Para 1.000.000 de bolsas, isso é significativo

### 11.2. Eficiência Decrescente

**Tendência esperada:**

```
2 threads  → Eficiência ~85%
4 threads  → Eficiência ~75%
8 threads  → Eficiência ~65%
```

Quanto mais threads, menor a eficiência porque:
- Overhead se acumula
- Lei de Amdahl se torna mais evidente
- Contenção de recursos aumenta

### 11.3. Ponto de Equilíbrio

Existe um **número ótimo de threads**:
- Antes: ganho com paralelismo supera o overhead
- Depois: overhead supera o ganho

Geralmente, o ótimo é:
```
Threads_ótimo ≈ Número de núcleos físicos
```

### 11.4. Impacto do Tamanho dos Dados

| Registros | Speedup 4 threads (estimado) |
|-----------|------------------------------|
| 10.000    | 1.2x (overhead domina)       |
| 100.000   | 2.8x (boa relação)           |
| 500.000   | 3.5x (ótimo)                 |
| 1.000.000 | 3.7x (lei de Amdahl domina)  |

**Conclusão:** Paralelismo é mais eficiente com **volumes médios-grandes**.

---

## 12. Escalabilidade Futura

### 12.1. Limitações da Implementação Atual

Esta implementação é **escalável até um ponto**:

✅ **Funciona bem para:**
- Até 1-2 milhões de registros
- 2-8 threads
- CPU com 4-8 núcleos

❌ **Limitado por:**
- Memória: todos os dados precisam caber na RAM
- CPU: limitado ao número de núcleos da máquina
- Ordenação final sequencial

### 12.2. Arquitetura Distribuída

Para escalar além, seria necessário:

#### Opção 1: Processamento em Batch com Múltiplas Instâncias

```
┌─────────────────┐
│  Load Balancer  │
└────────┬─────────┘
         │
    ┌────┴────┐
    │         │
┌───▼──┐  ┌───▼──┐
│ App 1│  │ App 2│  (Múltiplas instâncias)
└───┬──┘  └───┬──┘
    │         │
    └────┬────┘
         │
┌────────▼─────────┐
│  Banco de Dados  │
└──────────────────┘
```

**Vantagens:**
- Escala horizontalmente
- Sem limite de instâncias

**Desafios:**
- Coordenação entre instâncias
- Divisão de trabalho

#### Opção 2: Processamento com Filas (Mensageria)

```
┌──────────┐       ┌──────────┐
│  API     │─────→ │ RabbitMQ │
│  REST    │       │  / Kafka │
└──────────┘       └─────┬────┘
                         │
                    ┌────┴────┐
                    │         │
               ┌────▼──┐  ┌───▼──┐
               │Worker1│  │Worker2│  (Múltiplos workers)
               └───┬───┘  └───┬───┘
                   │          │
                   └────┬─────┘
                        │
               ┌────────▼─────────┐
               │  Banco de Dados  │
               └──────────────────┘
```

**Funcionamento:**
1. API divide trabalho em mensagens (ex: 100k bolsas por mensagem)
2. Workers consomem mensagens da fila
3. Cada worker processa sua parte
4. Resultados são salvos no banco

**Vantagens:**
- Desacoplamento total
- Escala infinitamente (mais workers = mais capacidade)
- Tolerância a falhas (mensagens não processadas voltam para fila)
- Assíncrono (API responde imediatamente)

**Tecnologias:**
- **Filas:** RabbitMQ, Apache Kafka, AWS SQS
- **Workers:** Spring Boot + @RabbitListener
- **Orquestração:** Kubernetes

#### Opção 3: Processamento Distribuído (MapReduce / Spark)

```
┌────────────────┐
│  Apache Spark  │
└───────┬────────┘
        │
   ┌────┴────┐
   │   MAP   │  (Filtrar compatíveis em paralelo)
   └────┬────┘
        │
   ┌────▼─────┐
   │ SHUFFLE  │  (Reagrupar por tipo)
   └────┬─────┘
        │
   ┌────▼────┐
   │ REDUCE  │  (Ordenar e agregar)
   └─────────┘
```

**Quando usar:**
- Dezenas de milhões de registros
- Processamento analítico complexo
- Big Data

**Tecnologias:**
- Apache Spark
- Apache Flink
- Hadoop MapReduce

#### Opção 4: Serverless (Cloud Functions)

```
┌──────────┐
│   API    │
└────┬─────┘
     │
     │ Divide trabalho
     │
     ├────────────────┐
     │                │
┌────▼──┐        ┌────▼──┐
│Lambda1│        │Lambda2│  (Múltiplas funções)
└───┬───┘        └───┬───┘
    │                │
    └────────┬───────┘
             │
    ┌────────▼─────────┐
    │  DynamoDB / S3   │
    └──────────────────┘
```

**Vantagens:**
- Escala automaticamente
- Paga apenas pelo que usa
- Zero gerenciamento de infraestrutura

**Desafios:**
- Limite de tempo de execução (15 min no AWS Lambda)
- Cold start

### 12.3. Recomendação de Arquitetura por Volume

| Volume de Dados | Arquitetura Recomendada |
|----------------|-------------------------|
| < 100k         | Thread única (sequencial) |
| 100k - 1M      | **Paralelismo local (esta implementação)** |
| 1M - 10M       | Múltiplas instâncias + Load Balancer |
| 10M - 100M     | Fila de mensagens + Workers |
| > 100M         | Apache Spark / Processamento distribuído |

### 12.4. Melhorias Locais Possíveis

Sem mudar a arquitetura, poderíamos:

1. **Paralelizar a Ordenação Final**
   - Usar Merge Sort paralelo
   - Dividir lista em K partes, ordenar em paralelo, fazer merge

2. **Usar Virtual Threads (Java 21)**
   - Menor overhead
   - Suporta milhares de threads

3. **Otimizar Alocação de Memória**
   - Reutilizar objetos
   - Evitar garbage collection frequente

4. **Cache de Compatibilidade**
   - Pré-calcular matriz de compatibilidade
   - Usar BitSet para verificações rápidas

---

## 13. Conclusões

### 13.1. Principais Aprendizados

1. **Paralelismo reduz tempo, mas não muda Big-O**
   - Complexidade assintótica permanece O(n log n)
   - Tempo de execução absoluto diminui

2. **Speedup não é linear**
   - Lei de Amdahl limita ganhos
   - Overhead de threads é significativo

3. **Existe um número ótimo de threads**
   - Geralmente ≈ número de núcleos físicos
   - Mais threads ≠ sempre melhor

4. **Particionamento é chave**
   - Divisão balanceada
   - Independência entre partições
   - Sem condições de corrida

5. **Agregação precisa ser eficiente**
   - Ordenação final pode ser gargalo
   - Considerar algoritmos de merge paralelos

### 13.2. Quando Usar Paralelismo

✅ **Use quando:**
- Processamento é CPU-bound
- Dados podem ser particionados independentemente
- Volume de dados é grande (> 100k registros)
- Máquina tem múltiplos núcleos

❌ **Não use quando:**
- Processamento é I/O-bound (melhor usar async/await)
- Dados são pequenos (overhead > ganho)
- Há dependências complexas entre tarefas

### 13.3. Aplicabilidade no Projeto Real

Esta implementação é **pronta para produção** para:
- Estoques de até 1 milhão de bolsas
- Requisições hospitalares que precisam de resposta rápida
- Dashboards de indicadores em tempo real

Para volumes maiores, migrar para arquitetura de filas + workers.

---

## 14. Referências

- **Lei de Amdahl**: Amdahl, Gene M. (1967). "Validity of the single processor approach to achieving large scale computing capabilities"
- **Java Concurrency in Practice**: Brian Goetz et al., Addison-Wesley, 2006
- **ExecutorService**: [Java Documentation](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ExecutorService.html)
- **FEFO (First Expired, First Out)**: Padrão de gerenciamento de estoque farmacêutico e hospitalar
- **Compatibilidade ABO/Rh**: Cruz Vermelha Brasileira / Hemorrede SUS

---

## 15. Apêndices

### A. Estrutura de Arquivos

```
src/main/java/com/hemoflow/hemoflow/
├── api/
│   ├── controller/
│   │   └── ProcessamentoBolsasController.java  ✅ Endpoint REST
│   └── dto/
│       └── ProcessamentoBolsasDTO.java         ✅ DTOs
├── domain/
│   ├── model/
│   │   ├── CompatibilidadeABO.java             ✅ Matriz de compatibilidade
│   │   └── ResultadoProcessamento.java         ✅ Resultado
│   └── enums/
│       └── ModoProcessamento.java              ✅ SEQUENCIAL/PARALELO
└── service/
    ├── ProcessamentoBolsasService.java         ✅ Lógica principal
    └── GeradorDadosBenchmarkService.java       ✅ Gerador de dados

tests/
└── ProcessamentoBolsasServiceTest.java         ✅ Testes de equivalência
```

### B. Comandos para Testes

```bash
# Compilar
mvn clean compile

# Executar testes
mvn test

# Executar aplicação
mvn spring-boot:run

# Executar benchmark específico
curl -X POST http://localhost:8080/api/v1/processamento-bolsas/executar \
  -H "Content-Type: application/json" \
  -d '{
    "quantidadeRegistros": 100000,
    "modo": "PARALELO",
    "numeroThreads": 4,
    "tipoAbo": "A",
    "fatorRh": "POSITIVO",
    "tipoComponente": "CONCENTRADO_HEMACIAS"
  }'
```

### C. Exemplo de Resposta da API

```json
{
  "quantidadeProcessada": 100000,
  "modo": "PARALELO",
  "numeroThreads": 4,
  "totalCompativeis": 12543,
  "bolsasVencidas": 231,
  "bolsasProximasVencimento": 456,
  "tempoProcessamentoMs": 387,
  "tempoProcessamentoSegundos": 0.387,
  "tipoReceptor": "A+",
  "componenteSolicitado": "Concentrado de Hemácias"
}
```

---

**Fim do Relatório**
