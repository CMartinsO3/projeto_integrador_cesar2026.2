# ADR-003 — Evoluir por monólito modular e Strangler Fig, sem reescrever tudo de uma vez

- **Status:** Aceito
- **Data:** 24/11/2026

## Contexto
O HemoFlow já está em produção acadêmica, com CI/CD (GitHub Actions → GHCR → Render) e testes. Reescrever tudo como microsserviços de uma vez travaria entregas e exigiria operar 8 serviços com uma equipe pequena.

## Decisão
1. **Fase 1 — Monólito modular:** reorganizar os pacotes por bounded context (`alocacao/`, `logistica/`, `estoque/`…), com regras de dependência verificadas por teste de arquitetura (ArchUnit).
2. **Fase 2 — Strangler Fig:** extrair primeiro os contextos com maior necessidade de escala ou isolamento: **Telemetria** (alto volume de escrita) e **Logística** (core, com picos). Um gateway roteia as chamadas para o serviço novo enquanto o monólito continua atendendo o restante.
3. **Fase 3:** extrair os demais contextos só se as métricas justificarem (veja o ADR-007).

## Consequências
- (+) O risco fica baixo: cada extração é reversível e o sistema continua entregando valor.
- (+) Os limites dos contextos são validados no código antes de virarem rede.
- (−) Durante a transição, convivem dois estilos de implantação.
- (−) É preciso disciplina para não criar dependências novas entre os módulos.

## Alternativas consideradas
- **Reescrever tudo como microsserviços (big bang):** tem alto risco e custo, e não traz ganho imediato.
- **Manter o monólito como está:** não resolve o acoplamento nem o problema de escalar a telemetria.
