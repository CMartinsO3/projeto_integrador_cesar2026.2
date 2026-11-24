# Arquitetura — Planejamento da remodelagem do HemoFlow

Entrega de 24/11/2026: DDD (subdomínios, bounded contexts, glossário e mapa de contextos), modelo tático do contexto core com cenário Gherkin, arquitetura em três camadas, projeção com microsserviços (serverless opcional) e três cenários com orçamento.

## Documentos entregues

| Arquivo | Conteúdo |
|---|---|
| [pdf/HemoFlow-planejamento-remodelagem.pdf](pdf/HemoFlow-planejamento-remodelagem.pdf) | Documento completo (13 páginas) |
| [pdf/HemoFlow-remodelagem-slides.pdf](pdf/HemoFlow-remodelagem-slides.pdf) | Slides da apresentação (14 slides, 16:9) |

## Diagramas

| Diagrama | Arquivo |
|---|---|
| Subdomínios (core, suporte, genérico) | [diagramas/01-subdominios.svg](diagramas/01-subdominios.svg) |
| Mapa de contextos | [diagramas/02-mapa-de-contextos.svg](diagramas/02-mapa-de-contextos.svg) |
| Modelo tático do contexto core *Alocação* | [diagramas/03-modelo-tatico-alocacao.svg](diagramas/03-modelo-tatico-alocacao.svg) |
| Arquitetura em três camadas | [diagramas/04-arquitetura-tres-camadas.svg](diagramas/04-arquitetura-tres-camadas.svg) |
| Projeção com microsserviços + serverless | [diagramas/05-microsservicos.svg](diagramas/05-microsservicos.svg) |

## Orçamento

[orcamento/orcamento-cenarios.xlsx](orcamento/orcamento-cenarios.xlsx) tem as abas *Premissas*, *Resumo*, *Cenario_A*, *Cenario_B* e *Cenario_C*. As células em azul com fundo amarelo são entradas (câmbio, valor-hora, horizonte); o resto são fórmulas.

| Cenário | Infra/mês | Esforço | Total em 12 meses |
|---|---|---|---|
| A — Monólito modular (Render) | US$ 44,62 | 112 h | R$ 11.851 |
| **B — Híbrido (alvo)** | US$ 200,62 | 416 h | **R$ 46.280** |
| C — Microsserviços completos (AWS) | US$ 498,91 | 980 h | R$ 110.729 |

Premissas: R$ 5,40/US$, R$ 80/h e preços de lista estimados (nov/2026).

## ADRs

| ADR | Decisão | Status |
|---|---|---|
| [001](adr/ADR-001-ddd-bounded-contexts.md) | Organizar por DDD: subdomínios e 8 bounded contexts | Aceito |
| [002](adr/ADR-002-tres-camadas.md) | Três camadas dentro de cada contexto | Aceito |
| [003](adr/ADR-003-monolito-modular-strangler.md) | Monólito modular + Strangler Fig | Aceito |
| [004](adr/ADR-004-eventos-de-dominio.md) | Integração por eventos de domínio | Aceito |
| [005](adr/ADR-005-banco-por-servico.md) | Banco por serviço | Aceito |
| [006](adr/ADR-006-serverless-pontual.md) | Serverless só para funções pontuais | Aceito |
| [007](adr/ADR-007-cenario-de-implantacao.md) | Cenário B como alvo de 12 meses | Proposto |

## Como regerar

- **Planilha:** `python docs/arquitetura/fontes/gerar_planilha.py` (requer `openpyxl`).
- **PDFs:** abra `fontes/documento.html` ou `fontes/slides.html` no Edge ou no Chrome e use Imprimir → Salvar como PDF, sem cabeçalhos e rodapés. Também dá pela linha de comando: `msedge --headless --no-pdf-header-footer --print-to-pdf=saida.pdf fontes/documento.html`.
