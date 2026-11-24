# ADR-006 — Serverless apenas para funções pontuais e sem estado

- **Status:** Aceito
- **Data:** 24/11/2026

## Contexto
Algumas capacidades são acionadas por evento, não guardam estado e têm uso irregular: a emissão da **guia de transporte** (já implementada como função HTTP no Cloudflare Workers), o envio de **notificações** e a geração de relatórios em PDF. Manter um container ligado o tempo todo para isso desperdiça dinheiro.

## Decisão
- Usar **funções serverless** (Cloudflare Workers ou AWS Lambda) para:
  - `fn-guia-transporte`: recebe a requisição alocada e a rota e devolve a guia em JSON ou HTML;
  - `fn-notificacoes`: consome `AlocacaoConfirmada`, `EstoqueInsuficiente` e `AlertaCadeiaFria` e envia e-mail ou push.
- As funções **não acessam bancos de outros contextos**: recebem tudo no evento ou no corpo da requisição.
- Os **contextos core** (Alocação e Logística) continuam em containers, porque precisam de transações e de latência estável.

## Consequências
- (+) O custo é praticamente zero em volume acadêmico (a franquia gratuita cobre o uso), e a escala é automática.
- (+) A função da guia já existe e está testada.
- (−) A depuração é diferente e há cold start (pequeno no Workers).
- (−) Existe dependência do runtime do provedor, mitigada por manter a regra em funções puras (`guia.js`).

## Alternativas consideradas
- **Tudo em containers:** é mais simples de padronizar, mas tem custo fixo maior para cargas raras.
- **Tudo serverless:** a alocação, com transações e reservas, ficaria complexa e cara em chamadas a banco.
