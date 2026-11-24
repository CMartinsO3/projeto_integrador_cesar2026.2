# ADR-005 — Banco de dados por serviço (PostgreSQL), com séries temporais para telemetria

- **Status:** Aceito
- **Data:** 24/11/2026

## Contexto
Hoje todas as tabelas ficam em um único PostgreSQL, e qualquer módulo pode ler qualquer tabela. Ao extrair serviços, um banco compartilhado anularia a independência de implantação e de evolução do esquema.

## Decisão
- Cada bounded context é **dono exclusivo** dos seus dados. Outro contexto só acessa esses dados por API ou evento.
- **No monólito modular:** um único servidor PostgreSQL com **um schema por contexto** e um usuário de banco por schema.
- **Nos microsserviços:** uma instância (ou um banco lógico) PostgreSQL por serviço. Para economizar, os contextos de suporte podem dividir a mesma instância, cada um em seu banco lógico.
- **Telemetria:** armazenamento de **séries temporais** (TimescaleDB ou DynamoDB), com retenção de 90 dias.

## Consequências
- (+) Cada serviço pode mudar o esquema sem quebrar os outros, e a migração do monólito vira trabalho mecânico (schema → banco).
- (−) Não há mais JOIN entre contextos: os relatórios usam o read model dos Indicadores.
- (−) Há mais instâncias para operar e pagar (impacto direto no orçamento dos cenários B e C).

## Alternativas consideradas
- **Banco único compartilhado:** é mais barato, mas acopla os serviços, porque toda mudança de esquema exige coordenação.
- **NoSQL para tudo:** a alocação precisa de transações ACID e de consultas por validade, o que se encaixa melhor no PostgreSQL.
