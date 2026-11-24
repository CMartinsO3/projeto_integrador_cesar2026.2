# ADR-007 — Escolha do cenário de implantação para os próximos 12 meses

- **Status:** Proposto (para validação com o professor e a equipe)
- **Data:** 24/11/2026

## Contexto
Foram orçados três cenários (planilha em `docs/arquitetura/orcamento/orcamento-cenarios.xlsx`):

| Cenário | Descrição | Infra/mês | Esforço (uma vez) | Total em 12 meses |
|---|---|---|---|---|
| **A — Monólito modular** | Render (web + PostgreSQL) + Worker da guia | US$ 44,62 (R$ 241) | 112 h (R$ 8.960) | **R$ 11.851** |
| **B — Híbrido** | monólito + Logística e Telemetria extraídas, broker gerenciado, funções serverless | US$ 200,62 (R$ 1.083) | 416 h (R$ 33.280) | **R$ 46.280** |
| **C — Microsserviços completos** | AWS ECS Fargate, RDS por serviço, SNS/SQS, API Gateway, observabilidade | US$ 498,91 (R$ 2.694) | 980 h (R$ 78.400) | **R$ 110.729** |

Premissas: câmbio de R$ 5,40/US$, valor-hora de R$ 80 e horizonte de 12 meses. Todas são editáveis na aba *Premissas*, e a planilha recalcula sozinha.

## Decisão
Adotar o **Cenário B (Híbrido)** como alvo:
- ele resolve os dois problemas concretos: a escala de escrita da telemetria e o isolamento da logística, que é core;
- custa cerca de 40% da infraestrutura do Cenário C (US$ 201 contra US$ 499 por mês) e 42% do custo total em 12 meses;
- segue o caminho Strangler do ADR-003, e o Cenário C continua possível se as métricas indicarem necessidade (gatilhos abaixo).

**Gatilhos para migrar ao C:** mais de 50 hospitais ativos; mais de 1 milhão de leituras de telemetria por dia; exigência de SLA de 99,9%; equipe com mais de 3 squads.

## Consequências
- (+) O custo é previsível e cabe em um piloto regional.
- (−) Continua existindo parte monolítica, com deploy conjunto de Captação, Estoque, Requisições e Alocação.

## Alternativas consideradas
- **Cenário A:** é o mais barato, mas não isola a telemetria e mantém o risco de um pico derrubar o sistema todo.
- **Cenário C:** tem custo e esforço de operação desproporcionais ao volume atual.
