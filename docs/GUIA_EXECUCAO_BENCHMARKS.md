# Guia de Execução dos Benchmarks

## 🎯 Objetivo

Este guia explica como executar os benchmarks de paralelismo do HemoFlow e obter os resultados para o relatório.

---

## 📋 Pré-requisitos

1. **Java 17** instalado
2. **Maven** instalado
3. Aplicação **HemoFlow** compilada
4. Porta **8080** disponível

---

## 🚀 Passo a Passo

### 1. Compilar a Aplicação

```bash
mvn clean compile
```

### 2. Executar a Aplicação

```bash
mvn spring-boot:run
```

Aguarde até ver a mensagem:
```
Started HemoflowApplication in X.XXX seconds
```

### 3. Verificar que está Funcionando

Acesse no navegador:
```
http://localhost:8080/docs
```

Você deve ver a documentação Swagger da API.

---

## 🧪 Opção 1: Executar com Script Automatizado (RECOMENDADO)

### Windows PowerShell

```powershell
cd scripts
.\executar-benchmarks.ps1
```

O script executará automaticamente:
- 100.000 registros: Sequencial, 2 threads, 4 threads, 8 threads
- 500.000 registros: Sequencial, 2 threads, 4 threads, 8 threads
- 1.000.000 registros: Sequencial, 2 threads, 4 threads, 8 threads

**Total: 12 testes × 3 repetições = 36 execuções**

### Resultado

O script exibirá:
1. ✅ Resultados em tempo real
2. 📊 Tabela consolidada com speedup e eficiência
3. 💾 Arquivo CSV com todos os dados
4. 📝 Tabela formatada em Markdown para copiar no relatório

---

## 🧪 Opção 2: Executar Manualmente

### Swagger UI (Interface Gráfica)

1. Acesse: `http://localhost:8080/docs`
2. Encontre o endpoint: `POST /api/v1/processamento-bolsas/executar`
3. Clique em "Try it out"
4. Cole o JSON e clique em "Execute"

### Exemplos de Requisições

#### Teste 1: 100.000 registros - Sequencial

```json
{
  "quantidadeRegistros": 100000,
  "modo": "SEQUENCIAL",
  "numeroThreads": 1,
  "tipoAbo": "A",
  "fatorRh": "POSITIVO",
  "tipoComponente": "CONCENTRADO_HEMACIAS"
}
```

#### Teste 2: 100.000 registros - 2 threads

```json
{
  "quantidadeRegistros": 100000,
  "modo": "PARALELO",
  "numeroThreads": 2,
  "tipoAbo": "A",
  "fatorRh": "POSITIVO",
  "tipoComponente": "CONCENTRADO_HEMACIAS"
}
```

#### Teste 3: 100.000 registros - 4 threads

```json
{
  "quantidadeRegistros": 100000,
  "modo": "PARALELO",
  "numeroThreads": 4,
  "tipoAbo": "A",
  "fatorRh": "POSITIVO",
  "tipoComponente": "CONCENTRADO_HEMACIAS"
}
```

#### Teste 4: 100.000 registros - 8 threads

```json
{
  "quantidadeRegistros": 100000,
  "modo": "PARALELO",
  "numeroThreads": 8,
  "tipoAbo": "A",
  "fatorRh": "POSITIVO",
  "tipoComponente": "CONCENTRADO_HEMACIAS"
}
```

**Repita para 500.000 e 1.000.000 de registros.**

### Exemplo de Resposta

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

**Anote o campo `tempoProcessamentoMs` de cada teste.**

---

## 📊 Calcular Speedup e Eficiência

### Fórmulas

```
Speedup = Tempo Sequencial / Tempo Paralelo

Eficiência = (Speedup / Número de Threads) × 100%
```

### Exemplo

```
100.000 registros:
- Sequencial: 1000 ms
- 2 threads: 550 ms
- 4 threads: 310 ms
- 8 threads: 180 ms

Speedup 2 threads = 1000 / 550 = 1.82x
Eficiência 2 threads = (1.82 / 2) × 100% = 91%

Speedup 4 threads = 1000 / 310 = 3.23x
Eficiência 4 threads = (3.23 / 4) × 100% = 81%

Speedup 8 threads = 1000 / 180 = 5.56x
Eficiência 8 threads = (5.56 / 8) × 100% = 69%
```

---

## 📝 Preencher o Relatório

### 1. Abrir o Relatório

```bash
docs/RELATORIO_PARALELISMO.md
```

### 2. Localizar a Seção "9.2. Tabela de Resultados"

### 3. Preencher a Tabela

**Antes:**
```markdown
| Registros   | Sequencial (ms) | 2 threads (ms) | 4 threads (ms) | 8 threads (ms) |
|-------------|-----------------|----------------|----------------|----------------|
| **100.000** |                 |                |                |                |
| **500.000** |                 |                |                |                |
| **1.000.000**|                 |                |                |                |
```

**Depois:**
```markdown
| Registros   | Sequencial (ms) | 2 threads (ms) | 4 threads (ms) | 8 threads (ms) |
|-------------|-----------------|----------------|----------------|----------------|
| **100.000** | 1000            | 550            | 310            | 180            |
| **500.000** | 4800            | 2600           | 1450           | 850            |
| **1.000.000**| 9500            | 5100           | 2800           | 1600           |
```

### 4. Preencher as Tabelas de Aceleração

Calcule speedup e eficiência para cada cenário usando as fórmulas acima.

### 5. Preencher Ambiente de Teste

```markdown
- **Processador:** [SEU PROCESSADOR - ex: Intel Core i7-12700K]
- **Memória RAM:** [SUA RAM - ex: 16GB DDR4]
- **Sistema Operacional:** [SEU SO - ex: Windows 11]
```

Para descobrir:

**Windows:**
```powershell
# Processador
Get-WmiObject Win32_Processor | Select-Object Name

# RAM
Get-WmiObject Win32_ComputerSystem | Select-Object TotalPhysicalMemory

# SO
Get-WmiObject Win32_OperatingSystem | Select-Object Caption, Version
```

**Linux/Mac:**
```bash
# Processador
lscpu | grep "Model name"

# RAM
free -h

# SO
uname -a
```

---

## ✅ Checklist de Execução

- [ ] Aplicação compilada com sucesso
- [ ] Aplicação rodando na porta 8080
- [ ] Swagger acessível em /docs
- [ ] Executado benchmark 100k - Sequencial
- [ ] Executado benchmark 100k - 2 threads
- [ ] Executado benchmark 100k - 4 threads
- [ ] Executado benchmark 100k - 8 threads
- [ ] Executado benchmark 500k - Sequencial
- [ ] Executado benchmark 500k - 2 threads
- [ ] Executado benchmark 500k - 4 threads
- [ ] Executado benchmark 500k - 8 threads
- [ ] Executado benchmark 1M - Sequencial
- [ ] Executado benchmark 1M - 2 threads
- [ ] Executado benchmark 1M - 4 threads
- [ ] Executado benchmark 1M - 8 threads
- [ ] Tempos anotados
- [ ] Speedup calculado
- [ ] Eficiência calculada
- [ ] Relatório preenchido
- [ ] Ambiente de teste documentado

---

## 🐛 Troubleshooting

### Erro: "Porta 8080 já está em uso"

**Solução:**
```powershell
# Encontrar processo usando porta 8080
netstat -ano | findstr :8080

# Encerrar processo (substitua PID pelo número encontrado)
taskkill /PID <PID> /F
```

### Erro: "Out of Memory"

**Solução:** Aumentar heap do Java
```bash
# Linux/Mac
export MAVEN_OPTS="-Xmx4g"
mvn spring-boot:run

# Windows PowerShell
$env:MAVEN_OPTS="-Xmx4g"
mvn spring-boot:run
```

### Erro: "Connection refused"

**Solução:** Verificar se a aplicação iniciou completamente
- Aguarde a mensagem "Started HemoflowApplication"
- Verifique logs para erros de compilação

### Testes muito lentos (> 10 segundos)

**Normal para:**
- 1.000.000 de registros
- Máquinas com poucos núcleos (< 4)
- Pouca RAM (< 8GB)

**Se estiver MUITO lento (> 60 segundos):**
- Reduza para 500.000 registros máximo
- Use apenas 2 e 4 threads
- Feche outros programas

---

## 📧 Suporte

Se encontrar problemas:

1. Verifique os logs da aplicação
2. Consulte a documentação do Spring Boot
3. Revise a seção de troubleshooting acima

---

**Boa sorte com os benchmarks!** 🚀
