# Atividade individual — Função HTTP serverless (HemoFlow)

**Aluno:** Ruan Carlos Oliveira da Silva — rcos3@cesar.school
**Projeto:** HemoFlow — Gestão e Distribuição de Hemocomponentes
**Provedor:** Cloudflare Workers
**Fluxo escolhido:** emissão da **Guia de Transporte**

**Execução:** local, no emulador oficial da Cloudflare (`wrangler dev`, runtime `workerd`) — publicar na nuvem é opcional nesta atividade
**Vídeo (2min45s):** ▶️ https://youtu.be/2YgPv3JjHj4 — cópia no repositório: [`video/apresentacao.mp4`](video/apresentacao.mp4)

## O que a função faz

Recebe via `POST /guia-transporte` a requisição do hospital, as bolsas alocadas (FEFO) e a rota calculada (Dijkstra). Antes de devolver a guia de transporte, revalida as regras do HemoFlow:

- compatibilidade ABO/Rh (mesma matriz 8×8 do backend Java)
- ordem FEFO das bolsas
- validade de cada bolsa na chegada prevista
- limite de cadeia fria do hemocomponente
- janela de entrega do hospital
- quantidade pedida

Respostas: **201** (guia apta), **422** (guia bloqueada, com as pendências), **400** (entrada inválida). Com `?formato=html` devolve a guia imprimível.

## Conteúdo desta pasta

| Pasta / arquivo | O que tem |
|---|---|
| `codigo/` | código-fonte da função, testes, payloads de exemplo e `README.md` com o contrato completo |
| `codigo/src/index.js` | handler HTTP (Cloudflare Workers) |
| `codigo/src/guia.js` | regras de negócio (funções puras) |
| `codigo/src/html.js` | versão imprimível da guia |
| `codigo/test/guia.test.js` | 8 testes unitários |
| `evidencias/testes-unitarios.txt` | saída do `npm test` (8/8 passando) |
| `evidencias/local-apta.txt` | chamada HTTP real ao emulador local (`wrangler dev`) → 201 |
| `evidencias/bloqueada-422.txt` | guia bloqueada com as pendências → 422 |
| `evidencias/guia-exemplo.html` | guia imprimível gerada pela função |
| `video/apresentacao.mp4` | vídeo explicando a solução e mostrando a demonstração |
| `video/` (demais arquivos) | slides, roteiro e script da demonstração usada no vídeo |

## Como executar

```powershell
cd codigo
npm.cmd install
npm.cmd test
npm.cmd run dev
```

Em outro terminal, dentro de `codigo`:

```powershell
curl.exe -i -X POST http://127.0.0.1:8787/guia-transporte -H "Content-Type: application/json" -d "@exemplo-requisicao.json"
```

Demonstração completa, com pausas (rodar dentro desta pasta): `powershell -ExecutionPolicy Bypass -File video/demo.ps1`

Deploy (opcional): `npx.cmd wrangler login` e depois `npm.cmd run deploy`.
