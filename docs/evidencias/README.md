# Índice de Evidências — Desafio do PI (E1, E2 e E3)

Este documento reúne as evidências reproduzíveis do HemoFlow. Os resultados
locais foram executados no branch `feature/entrega-u2` em 20/10/2026. Os itens
que dependem de uma execução hospedada no GitHub Actions ou de uma URL pública
não são marcados como concluídos sem um link verificável.

## Resumo da validação local

| Validação | Resultado |
| :--- | :--- |
| Maven (`./mvnw -B verify`) | **CONCLUÍDO** — 39 testes, 0 falhas, 0 erros |
| Cucumber | **CONCLUÍDO** — 4 cenários e 15 passos aprovados |
| Docker Compose | **CONCLUÍDO** — API e PostgreSQL saudáveis |
| Smoke test HTTP | **CONCLUÍDO** — `/actuator/health`, `/actuator/info` e `/api/v1/topologia` |
| Persistência do volume PostgreSQL | **CONCLUÍDO** — `down` preserva e `down -v` remove o volume |
| Estruturas C++ | **CONCLUÍDO** — compilação sem warnings bloqueantes |
| Matriz ABO/Rh C++ | **CONCLUÍDO** — 27 pares compatíveis e 0 falhas |

## E1 — Container Automatizado (`docs/evidencias/e1/`)

| Item | Evidência | Status | Resultado |
| :--- | :--- | :--- | :--- |
| Validação do `compose.yaml` (`docker compose config`) | [`compose-config.txt`](./e1/compose-config.txt) | **CONCLUÍDO** | Exit code 0 |
| Subida e status `healthy` (`docker compose ps`) | [`compose-ps.txt`](./e1/compose-ps.txt) | **CONCLUÍDO** | `hemoflow-api` e `hemoflow-db` saudáveis |
| Resposta de `/actuator/health` | [`health.json`](./e1/health.json) | **CONCLUÍDO** | `{"status":"UP"}` |
| Resposta de `/actuator/info` | [`info.json`](./e1/info.json) | **CONCLUÍDO** | Versão `0.0.1-SNAPSHOT`, SHA local `local-compose` |
| Resposta de `/api/v1/topologia` | [`api-topologia.json`](./e1/api-topologia.json) | **CONCLUÍDO** | Nós e ligações da rede retornados |
| Teste de persistência (`down` vs `down -v`) | [`persistencia-volume.txt`](./e1/persistencia-volume.txt) | **CONCLUÍDO** | `down` preservou `hemoflow_pgdata`; `down -v` removeu |

## E2 — Ciclo TDD Red → Green (`docs/evidencias/tdd/`)

### Fase RED

A fase RED foi registrada no commit [`14fd97d`](https://github.com/CMartinsO3/projeto_integrador_cesar2026.2/commit/14fd97d).
Os testes foram executados contra a implementação U1, sem ABO/Rh e FEFO
integrados ao `EstoqueService`, e produziram falhas funcionais intencionais:

- bolsa incompatível sendo alocada;
- bolsa com vencimento mais próximo não sendo escolhida;
- requisição sem bolsa compatível não ficando em `AGUARDANDO_ESTOQUE`.

### Fase GREEN

A implementação foi corrigida no [EstoqueService.java](../../src/main/java/com/hemoflow/hemoflow/estoque/EstoqueService.java),
filtrando compatibilidade ABO/Rh, ignorando bolsas vencidas e ordenando por
FEFO (`dataValidade ASC`, com desempate por `id ASC`).

| História | Testes e evidência | Status | Referência |
| :--- | :--- | :--- | :--- |
| **História A — ABO/Rh** | [`suite-green.txt`](./tdd/suite-green.txt) | **CONCLUÍDO** | 39 testes aprovados; testes unitário e de persistência |
| **História B — FEFO** | [`suite-green.txt`](./tdd/suite-green.txt) | **CONCLUÍDO** | seleção por validade mais próxima aprovada |
| **História C — Dijkstra** | [`suite-green.txt`](./tdd/suite-green.txt) | **CONCLUÍDO** | teste unitário e Cucumber aprovados |
| **Compatibilidade C/C++** | [`cpp-compatibilidade.txt`](./tdd/cpp-compatibilidade.txt) | **CONCLUÍDO** | 27 pares compatíveis; 0 falhas |

Resumo final do Maven:

```text
Tests run: 39, Failures: 0, Errors: 0, Skipped: 0
4 Scenarios (4 passed)
15 Steps (15 passed)
BUILD SUCCESS
```

## E3 — Pipeline CI/CD, Deploy e Rollback

Workflow de CI: [`ci.yml`](../../.github/workflows/ci.yml)
Workflow de CD: [`deploy.yml`](../../.github/workflows/deploy.yml)

| Evidência exigida | Referência verificável | Status |
| :--- | :--- | :--- |
| Execução RED bloqueando o pipeline | Commit [`14fd97d`](https://github.com/CMartinsO3/projeto_integrador_cesar2026.2/commit/14fd97d); a URL da run precisa ser adicionada após o push | **PENDENTE — execução hospedada** |
| Execução GREEN do pipeline | Workflow [`CI`](https://github.com/CMartinsO3/projeto_integrador_cesar2026.2/actions/workflows/ci.yml); a URL da run precisa ser adicionada após o push | **PENDENTE — execução hospedada** |
| Imagem Docker publicada no GHCR | Job `publish-image` do [`deploy.yml`](../../.github/workflows/deploy.yml) | **PENDENTE — requer execução na `main`** |
| URL pública no ar + `/actuator/health` | Requer `PUBLIC_APP_URL` configurada no environment `production` | **PENDENTE — URL não fornecida** |
| `/actuator/info` com o SHA implantado | Verificação automática no job `deploy` | **PENDENTE — requer deploy real** |
| Rollback via `workflow_dispatch` | Input `target_sha` do [`deploy.yml`](../../.github/workflows/deploy.yml) | **PENDENTE — requer run real de rollback** |

### Procedimento para fechar E3

Depois de publicar os commits:

```powershell
git push origin feature/entrega-u2
```

1. Abra **Actions** e copie a URL da execução do CI.
2. Confirme uma execução RED referente ao commit `14fd97d`, se a evidência
   histórica ainda for exigida pelo roteiro.
3. Faça merge da implementação GREEN na `main` e copie a URL da execução do
   CD.
4. Configure no environment `production` os secrets
   `RENDER_DEPLOY_HOOK_URL` e `PUBLIC_APP_URL`.
5. Para rollback, execute o workflow `CD` manualmente informando em
   `target_sha` um commit GREEN anterior e copie a URL dessa execução.

Nenhuma URL de produção ou execução hospedada é inventada neste índice; elas
devem ser adicionadas somente após serem verificadas no GitHub/Render.
