# 🩸 HemoFlow — Função HTTP: Guia de Transporte (Cloudflare Workers)

Atividade individual — **Ruan Carlos Oliveira da Silva** (rcos3@cesar.school)

Função HTTP *serverless* que recebe do HemoFlow uma **requisição de hemocomponente já alocada** (bolsas escolhidas por FEFO + rota calculada pelo Dijkstra) e devolve a **guia de transporte** pronta para o motorista e para o hospital.

- **Provedor:** Cloudflare Workers (plano gratuito, sem cartão de crédito)
- **Linguagem:** JavaScript (ES Modules), sem dependências de runtime
- **Endpoint:** `POST /guia-transporte` (+ `GET /health`)

## Onde ela entra no fluxo do HemoFlow

```
Hospital ──POST /api/v1/requisicoes──▶ Backend Spring Boot
                                        │ alocar (FEFO + ABO/Rh)
                                        │ rota (Dijkstra no grafo da rede)
                                        ▼
                          POST /guia-transporte  ◀── esta função (Cloudflare)
                                        │
                                        ▼
                     Guia JSON (sistema) ou HTML imprimível (motorista)
```

A função é **stateless**: o backend envia tudo o que ela precisa no corpo da requisição, e ela revalida as regras críticas antes de emitir a guia (defesa em profundidade):

| Regra | Origem no backend Java | O que a função faz |
|---|---|---|
| Compatibilidade ABO/Rh | `CompatibilidadeAboRh` (matriz 8×8) | mesma matriz; bolsa incompatível → pendência |
| FEFO | `FilaFEFO` | ordena as bolsas pela validade mais próxima |
| Validade na entrega | `Bolsa.validade` | bolsa que vence antes da chegada → pendência |
| Cadeia fria | `Hemocomponente.limiteCadeiaFriaMinutos` | tempo de rota > limite → pendência |
| Janela de entrega | `Requisicao.janelaEntrega` | chegada prevista após a janela → pendência |
| Quantidade | `Requisicao.quantidade` | nº de bolsas diferente → pendência |

## Contrato

### Requisição — `POST /guia-transporte`

```json
{
  "requisicao": {
    "id": 17,
    "hospital": { "id": 3, "nome": "Hospital da Restauração" },
    "tipoSanguineo": "A_POS",
    "hemocomponente": "HEMACIAS",
    "quantidade": 2,
    "janelaEntrega": "2030-12-01T14:00:00-03:00"
  },
  "bolsas": [
    { "id": "BOL-0007", "tipoSanguineo": "A_POS", "hemocomponente": "HEMACIAS", "validade": "2030-12-20T00:00:00-03:00" },
    { "id": "BOL-0042", "tipoSanguineo": "O_NEG", "hemocomponente": "HEMACIAS", "validade": "2030-12-05T00:00:00-03:00" }
  ],
  "rota": {
    "caminho": ["HEMOPE (Recife)", "Centro de Distribuição Boa Vista", "Hospital da Restauração"],
    "distanciaKm": 6.4,
    "tempoEstimadoMin": 25,
    "saidaPrevista": "2030-12-01T12:30:00-03:00"
  }
}
```

Os enums são os mesmos do backend (`TipoSanguineo`: `A_POS`…`O_NEG`; `Hemocomponente`: `HEMACIAS`, `PLASMA`, `PLAQUETAS`, `CRIOPRECIPITADO`).

### Respostas

| Status | Quando |
|---|---|
| `201 Created` | guia emitida com `status: "APTA_PARA_TRANSPORTE"` |
| `422 Unprocessable Entity` | guia emitida com `status: "BLOQUEADA"` e a lista `pendencias` |
| `400 Bad Request` | JSON malformado ou campos inválidos (`detalhes` lista os erros) |
| `405 Method Not Allowed` | método diferente de POST |

Resposta (resumida):

```json
{
  "numeroGuia": "HF-20261124-176BCB",
  "status": "APTA_PARA_TRANSPORTE",
  "pendencias": [],
  "requisicao": { "hospital": "Hospital da Restauração", "tipoSanguineoReceptor": "A+", "hemocomponente": "Hemácias", "quantidade": 2 },
  "transporte": { "origem": "HEMOPE (Recife)", "destino": "Hospital da Restauração", "chegadaPrevista": "...", "tempoEstimadoMin": 25 },
  "cadeiaFria": { "temperaturaExigida": "2 °C a 6 °C", "acondicionamento": "Caixa térmica refrigerada", "limiteMin": 120, "folgaMin": 95 },
  "itens": [
    { "ordemFEFO": 1, "bolsaId": "BOL-0042", "tipoSanguineo": "O−", "compativel": true, "validaNaEntrega": true },
    { "ordemFEFO": 2, "bolsaId": "BOL-0007", "tipoSanguineo": "A+", "compativel": true, "validaNaEntrega": true }
  ],
  "checklist": ["Conferir etiqueta e número de cada bolsa com esta guia", "..."]
}
```

Com `?formato=html` (ou `Accept: text/html`) a função devolve a **guia imprimível**, com campos de assinatura — veja [evidencias/guia-exemplo.html](evidencias/guia-exemplo.html).

## Como rodar

Pré-requisito: Node.js 18+.

```powershell
cd functions/guia-transporte
npm install
npm test            # 8 testes unitários (node:test)
npm run dev         # emulador local da Cloudflare em http://127.0.0.1:8787 (runtime workerd)
```

Testando:

```powershell
curl.exe -X POST http://127.0.0.1:8787/guia-transporte -H "Content-Type: application/json" -d "@exemplo-requisicao.json"
```

## Deploy na Cloudflare (opcional)

```powershell
npx wrangler login      # abre o navegador para autorizar sua conta Cloudflare (grátis)
npm run deploy          # publica em https://hemoflow-guia-transporte.<seu-subdominio>.workers.dev
```

Depois do deploy, teste a URL pública:

```powershell
curl.exe -X POST https://hemoflow-guia-transporte.<seu-subdominio>.workers.dev/guia-transporte -H "Content-Type: application/json" -d "@exemplo-requisicao.json"
```

## Estrutura

```
functions/guia-transporte/
├── src/index.js            # handler HTTP (roteamento, status codes, CORS)
├── src/guia.js             # regras de negócio puras (ABO/Rh, FEFO, cadeia fria)
├── src/html.js             # versão imprimível da guia
├── test/guia.test.js       # testes unitários
├── exemplo-requisicao.json # payload de exemplo
├── evidencias/             # saídas reais da execução local
└── wrangler.toml           # configuração do Worker
```

> Dados sintéticos e regras didáticas — não substitui protocolos clínicos/transfusionais reais.
