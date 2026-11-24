# ADR-004 — Integração entre contextos por eventos de domínio

- **Status:** Aceito
- **Data:** 24/11/2026

## Contexto
O fluxo principal atravessa vários contextos: Requisição → Alocação → Logística → Telemetria → Indicadores. Com chamadas síncronas em cadeia, uma falha na Logística impediria a confirmação de uma alocação, e os Indicadores precisariam consultar todos os outros contextos.

## Decisão
- Os **comandos** que precisam de resposta imediata continuam **síncronos** (REST). Exemplo: a Alocação reserva bolsas no Estoque por meio do Open Host Service.
- Os **fatos de negócio** são publicados como **eventos de domínio** em um broker: `RequisicaoAberta`, `AlocacaoConfirmada`, `EstoqueInsuficiente`, `AlocacaoCancelada`, `GuiaEmitida`, `LeituraRegistrada` e `AlertaCadeiaFria`.
- A publicação usa o padrão **Transactional Outbox**: o evento é gravado na mesma transação do agregado e depois enviado ao broker.
- Os consumidores são **idempotentes** (chave = id do evento), e as mensagens que falham vão para uma **DLQ**.
- O contrato dos eventos (Published Language) é versionado em JSON Schema.
- No monólito modular, os eventos usam `ApplicationEventPublisher` do Spring. O broker entra quando o primeiro serviço for extraído.

## Consequências
- (+) Há baixo acoplamento temporal: a Logística pode estar fora do ar sem bloquear a Alocação.
- (+) Os Indicadores montam o próprio read model (CQRS) sem consultar outros bancos.
- (−) A consistência entre contextos passa a ser **eventual**, e a interface precisa mostrar estados intermediários.
- (−) Rastrear o fluxo exige correlation-id e tracing distribuído.

## Alternativas consideradas
- **Tudo síncrono (REST em cadeia):** é simples, mas frágil e acopla a disponibilidade dos serviços.
- **Banco compartilhado:** a integração fica implícita e as fronteiras dos contextos se perdem.
