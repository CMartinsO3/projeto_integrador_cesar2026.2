# Índice de Evidências — Desafio do PI (E1, E2 e E3)

## E1 — Container Automatizado (`docs/evidencias/e1/`)

| Item | Arquivo / Evidência | Status | Commit / Data |
| :--- | :--- | :--- | :--- |
| Validação do `compose.yaml` (`docker compose config`) | `./e1/compose-config.txt` | PENDENTE | — |
| Subida e status `healthy` (`docker compose ps`) | `./e1/compose-ps.txt` | PENDENTE | — |
| Resposta de `/actuator/health` e `/actuator/info` | `./e1/actuator-health-info.txt` | PENDENTE | — |
| Resposta de `/api/v1/topologia` | `./e1/api-topologia.json` | PENDENTE | — |
| Teste de persistência (`down` vs `down -v`) | `./e1/persistencia-volume.txt` | PENDENTE | — |

## E2 — Ciclo TDD Red → Green (`docs/evidencias/tdd/`)

| História | Evidência Red (Falha) | Evidência Green (Sucesso) | Status | Commits (`test:` / `feat:`) |
| :--- | :--- | :--- | :--- | :--- |
| **História A (ABO/Rh)** | `./tdd/historia-a-red.txt` | `./tdd/historia-a-green.txt` | PENDENTE | — |
| **História B (FEFO)** | `./tdd/historia-b-red.txt` | `./tdd/historia-b-green.txt` | PENDENTE | — |
| **História C (Dijkstra)** | `./tdd/historia-c-red.txt` | `./tdd/historia-c-green.txt` | PENDENTE | — |

## E3 — Pipeline CI/CD, Deploy e Rollback

| Evidência Exigida | Link Verificável no GitHub Actions / Produção | Status |
| :--- | :--- | :--- |
| **Execução Vermelha que Barrou o Merge/Deploy** | *(Colar link da run vermelha do commit `test:` no PR)* | PENDENTE |
| **Execução Verde que Publicou em Produção** | *(Colar link da run verde na `main`)* | PENDENTE |
| **URL Pública no Ar + `/actuator/info`** | *(Colar URL do Render + saída JSON)* | PENDENTE |
| **Execução Real de Rollback Verificada** | *(Colar link da run `workflow_dispatch` de rollback)* | PENDENTE |