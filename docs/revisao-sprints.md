# Revisão das sprints (cronograma de Projetos 3)

Data da revisão: 05/11/2026 — estamos na **Week 14–15** (02/11–13/11, "Preparar Apresentação").
Legenda: ✅ evidência no repositório · ⚠️ parcial · ❌ não encontrado no repositório · 📎 artefato fora do código (conferir com o grupo).

| Sprint | Período | Entregável previsto | Status | Evidência / observação |
|---|---|---|---|---|
| W01 Kick-off | 03–07/08 | Tema do PI | ✅ | `README.md` |
| | | Calendário único, critérios de avaliação | 📎 | Não faz parte do repo |
| W02–03 Modelo/Dados/Alg. | 10–21/08 | Modelo de domínio | ✅ | `docs/modelo-dominio.md` |
| | | Escopo do grafo de rotas | ✅ | `docs/escopo-grafo.md` |
| | | Contratos de API | ✅ | `docs/contrato-api.md` (atualizado com `/estatisticas/*`) |
| W04–05 Setup infra e algoritmos | 24/08–04/09 | Repositório, pipeline CI/CD | ⚠️ | CI ok (`.github/workflows/ci.yml`); CD não existe (ver W08–09) |
| | | Topologia de comunicação | ❌ | Só há o grafo de hospitais (`/api/v1/topologia`); falta diagrama/doc de comunicação |
| | | Primeiras estruturas | ✅ | `c_structures/`, `docs/estruturas-u1.md` |
| W06–07 Integração | 07–18/09 | Build do app, módulos de algoritmos | ✅ | App Spring Boot, `algoritmos/`, `docs/algoritmos-u2.md` |
| | | Indicadores | ✅ | Antes só contagens; agora estatística completa (`/estatisticas`) |
| | | Pipeline | ✅ | `ci.yml` |
| | | Diagrama de rede | ❌ | Não há diagrama |
| W08–09 Checkpoint U1 | 21/09–02/10 | App com CRUD | ✅ | Controllers de bolsas, requisições, hospitais, doadores, agendamentos |
| | | Grafo + FEFO + hash | ✅ | `RoteadorVizinhoMaisProximo`, `FilaFEFO`, `TabelaHash`, `IndiceEstoque` |
| | | CI/CD + deploy | ⚠️ | Existe `Dockerfile` e `docker-compose.yml`, mas não há publicação/hospedagem nem etapa de CD |
| | | Arquitetura de comunicação | ❌ | Não documentada |
| W10–11 Replanejamento U2 | 05–16/10 | Quadro replanejado | 📎 | Quadro do grupo |
| | | Módulo de compatibilidade | ✅ | `CompatibilidadeAboRh` + testes |
| | | Integração com rota/validade | ✅ | `ProcessadorPedidoService`, `README-U2-ENTREGA.md` |
| W12–13 Telemetria/Conc./Painéis | 19–30/10 | Processamento concorrente | ⚠️ | Só `synchronized` em `IndiceEstoque`; não há execução paralela demonstrável nem teste de concorrência |
| | | Métricas de rede | ⚠️ | `/monitoramento` mostra status de dispositivos e temperatura; faltam métricas (latência, perda, disponibilidade) |
| | | Painéis de estoque/demanda | ✅ | Nova página `/estatisticas` + índice `/` |
| W14–15 Apresentação | 02–13/11 | Build final | ⚠️ | Não consegui compilar nem rodar os testes aqui (sem JDK). Rode `./mvnw -B verify` antes de apresentar |
| | | Documentação | ⚠️ | Falta `docs/estatistica.md` ser citado na entrega; README "Entrega 02" tem seção de vídeos vazia |
| | | Roteiro de apresentação e divisão de falas | ❌ | Não existe |

## Observações gerais

- O README tem a seção "Entrega 02" praticamente vazia (sem links de artefatos/vídeo).
- A tabela de integrantes e funções cita "RSD / Topologia" e "Infraestrutura / Telemetria"; os artefatos dessas funções (diagramas, métricas de rede) são justamente os que faltam.
- A US 9 (Indicadores de Estoque e Demanda) agora está coberta com risco de desabastecimento por tipo sanguíneo.

## Pendências sugeridas (ordem de prioridade)

1. Rodar `./mvnw -B verify` e corrigir qualquer falha.
2. `docs/arquitetura-comunicacao.md` com diagrama de rede/topologia (cobre W04–05, W06–07, W08–09 e W12–13).
3. Demonstração de concorrência (ex.: teste com várias threads alocando bolsas do mesmo tipo) e métricas simples de rede.
4. Etapa de CD (push da imagem Docker ou deploy em Render/Railway) ou, no mínimo, instruções reproduzíveis de deploy.
5. `docs/roteiro-apresentacao.md` com divisão de falas e demo.
6. Preencher "Entrega 02" no README.

