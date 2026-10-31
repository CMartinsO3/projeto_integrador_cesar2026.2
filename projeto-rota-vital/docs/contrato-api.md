# Contrato de API — HemoFlow

> Documento de definição dos contratos de API do projeto HemoFlow, referente à Unidade 1 (POO). Define os recursos expostos, o formato de requisição/resposta, os códigos de status e as regras de erro usadas pela aplicação.

---

## 1. Objetivo do Contrato

Definir a interface REST que a aplicação expõe para o CRUD das entidades principais do domínio (doações, bolsas/componentes, estoque, hospitais, requisições) e para a consulta de rota na malha de transporte, permitindo que qualquer cliente (front-end, painel de indicadores, outra disciplina do PI) consuma o sistema sem depender da implementação interna.

---

## 2. Padrões Gerais

| Item | Definição |
|---|---|
| Base path | `/api/v1` |
| Formato | JSON (`application/json`) |
| Autenticação | Não exigida na Unidade 1 (pode ser adicionada como extensão na U2) |
| Versionamento | Prefixo de versão na URL (`/v1`); mudanças incompatíveis sobem para `/v2` |
| Documentação viva | `/docs` (springdoc-openapi / Swagger UI) |
| Nomenclatura | Recursos no plural, em português (`/hospitais`, `/requisicoes`) |

---

## 3. Recursos e Endpoints

### Nós da malha (`/nos-rede`)

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/nos-rede` | Cria um vértice (centro de coleta, hemocentro ou hospital) |
| `GET` | `/nos-rede` | Lista todos os vértices |
| `GET` | `/nos-rede/{id}` | Busca um vértice pelo id |
| `PUT` | `/nos-rede/{id}` | Atualiza um vértice |
| `DELETE` | `/nos-rede/{id}` | Remove um vértice |

Os cadastros e respostas de nós aceitam/informam `latitude` e `longitude` em graus decimais. Para criar uma ligação, ambos os nós precisam ter as duas coordenadas.

### Ligações e topologia (`/ligacoes`, `/topologia`)

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/ligacoes` | Cria uma aresta direcionada com `origemId`, `destinoId` e `tempoMinutos` |
| `GET` | `/ligacoes` | Lista as arestas com tempo e distância em quilômetros |
| `GET` | `/topologia` | Lista os vértices e as arestas da rede |

`distanciaKm` é calculada em linha reta pela fórmula de Haversine a partir das coordenadas dos nós; não equivale à distância percorrida pelas vias.

### Hospitais (`/hospitais`)

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/hospitais` | Cadastra um hospital |
| `GET` | `/hospitais` | Lista todos os hospitais |
| `GET` | `/hospitais/{id}` | Busca um hospital pelo id |
| `PUT` | `/hospitais/{id}` | Atualiza um hospital |
| `DELETE` | `/hospitais/{id}` | Inativação lógica (não exclui histórico) |

### Agendamentos de doação (`/agendamentos`)

| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/agendamentos/hospital/{hospitalId}/disponibilidade?data=AAAA-MM-DD` | Lista horários livres de 30 em 30 minutos, de segunda a sábado, entre 8h e 18h; cada horário aceita até 5 doadores |
| `POST` | `/agendamentos` | Agenda para o doador autenticado, informando `hospitalId` e `dataHora` |
| `GET` | `/agendamentos/doador/{doadorId}` | Consulta os agendamentos do doador autenticado; outro doador recebe `403` |
| `PATCH` | `/agendamentos/{id}/status` | Atualiza o status; exige sessão da equipe clínica |

O `POST` exige sessão autenticada com perfil `DOADOR`; o identificador do doador vem da sessão, não do corpo enviado pelo navegador. A data precisa estar entre hoje e 30 dias à frente. O servidor valida novamente o horário e a capacidade antes de salvar, serializando novas reservas por hospital para evitar ultrapassar o limite de vagas. Horários cancelados não ocupam vaga.

### Doações (`/doacoes`)

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/doacoes` | Registra uma doação |
| `GET` | `/doacoes` | Lista todas as doações |
| `GET` | `/doacoes/{id}` | Busca uma doação pelo id |
| `DELETE` | `/doacoes/{id}` | Remove um registro de doação |

> Sem `PUT`: doação é um fato ocorrido, não um dado editável.

### Bolsas/componentes (`/bolsas`)

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/bolsas` | Cria uma bolsa a partir de uma doação |
| `GET` | `/bolsas?status=` | Lista bolsas, com filtro opcional de status |
| `GET` | `/bolsas/{id}` | Busca uma bolsa pelo id |
| `PATCH` | `/bolsas/{id}/status` | Transiciona o status da bolsa |
| `DELETE` | `/bolsas/{id}` | Remove uma bolsa |

### Estoque (`/estoque`)

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/estoque` | Cria ou atualiza os parâmetros de estoque (local + tipo) |
| `GET` | `/estoque` | Lista o saldo agregado por local e tipo |
| `GET` | `/estoque/{id}` | Busca um registro de estoque pelo id |
| `DELETE` | `/estoque/{id}` | Remove um registro de estoque |

### Requisições (`/requisicoes`)

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/requisicoes` | Abre uma requisição hospitalar |
| `POST` | `/requisicoes/{id}/processar` | Executa a cadeia hash → compatibilidade → FEFO → rota e devolve bolsas e trajetos |
| `GET` | `/requisicoes?status=` | Lista requisições, com filtro opcional de status |
| `GET` | `/requisicoes/{id}` | Busca uma requisição pelo id |
| `PATCH` | `/requisicoes/{id}/status` | Transiciona o status da requisição |
| `DELETE` | `/requisicoes/{id}` | Cancela a requisição (soft delete) |

### Rota na malha (`/rotas`)

| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/rotas/vizinho-mais-proximo?origemId=&destinoId=` | Constrói uma rota gulosa, escolhendo o vizinho de menor tempo a partir da origem |

---

## 4. Exemplos de Requisição e Resposta

### Criar bolsa/componente

```http
POST /api/v1/bolsas
Content-Type: application/json

{
  "doacaoId": 10,
  "tipoComponente": "CONCENTRADO_HEMACIAS",
  "volumeMl": 350,
  "dataProducao": "2026-09-10",
  "localizacaoAtualId": 2
}
```

```json
201 Created

{
  "id": 55,
  "codigoRastreio": "BC-A1B2C3D4",
  "tipoComponente": "CONCENTRADO_HEMACIAS",
  "tipoAbo": "O",
  "fatorRh": "NEGATIVO",
  "dataProducao": "2026-09-10",
  "dataValidade": "2026-10-22",
  "status": "DISPONIVEL",
  "localizacaoAtualId": 2,
  "nomeLocalizacao": "Hemocentro Central",
  "vencida": false
}
```

### Atualizar status de uma requisição

```http
PATCH /api/v1/requisicoes/12/status
Content-Type: application/json

{ "status": "ATENDIDA" }
```

```json
200 OK

{
  "id": 12,
  "hospitalId": 3,
  "nomeHospital": "Hospital Barão de Lucena",
  "tipoAbo": "A",
  "fatorRh": "POSITIVO",
  "tipoComponente": "CONCENTRADO_PLAQUETAS",
  "quantidade": 2,
  "urgencia": "URGENTE",
  "status": "ATENDIDA",
  "dataSolicitacao": "2026-09-15T09:30:00",
  "prazoLimite": "2026-09-15T14:00:00"
}
```

### Construir rota pelo vizinho mais próximo

```http
GET /api/v1/rotas/vizinho-mais-proximo?origemId=4&destinoId=7
```

```json
200 OK

{
  "rota": [
    { "id": 4, "codigo": "HC", "nome": "Hemocentro", "tipo": "HEMOCENTRO", "latitude": -8.063, "longitude": -34.8712 },
    { "id": 8, "codigo": "N1", "nome": "Nó intermediário", "tipo": "INTERMEDIARIO", "latitude": -8.06, "longitude": -34.9 },
    { "id": 7, "codigo": "H1", "nome": "Hospital", "tipo": "HOSPITAL", "latitude": -8.0609, "longitude": -34.9119 }
  ],
  "tempoTotalMinutos": 25,
  "distanciaTotalKm": 4.6,
  "encontrada": true
}
```

O algoritmo escolhe o vizinho não visitado com menor tempo a cada passo. Por ser guloso, não garante o menor custo total e pode não alcançar o destino mesmo havendo outro caminho; nesse caso, `encontrada` é `false` e `rota` contém o trecho percorrido.

### Processar pedido de ponta a ponta

```http
POST /api/v1/requisicoes/12/processar
```

Consulta as bolsas no índice hash, filtra compatibilidade ABO/Rh (para hemácias), prioriza FEFO, calcula as rotas a partir da localização de cada bolsa e aloca a quantidade solicitada. A resposta contém cada bolsa selecionada e sua rota, tempo e distância. Para componentes sem matriz ABO/Rh específica, mantém-se a regra conservadora de tipo sanguíneo idêntico. Se não houver bolsas suficientes com rota válida, a requisição fica em `AGUARDANDO_ESTOQUE` e a API retorna erro de regra de negócio.

---

## 5. Contrato de Erro

Todo erro segue o mesmo formato, independentemente do endpoint:

```json
{
  "timestamp": "2026-09-15T10:00:00",
  "status": 422,
  "erro": "Regra de negócio violada",
  "mensagem": "Bolsa vencida em 2026-09-01 só pode ser movida para DESCARTADA",
  "detalhes": []
}
```

| Status | Significado | Quando ocorre |
|---|---|---|
| `400` | Erro de validação | Campo obrigatório ausente ou inválido (`detalhes` traz um item por campo) |
| `404` | Recurso não encontrado | Id inexistente em qualquer entidade |
| `422` | Regra de negócio violada | Ex.: bolsa vencida, requisição em status final, hospital inativo |
| `500` | Erro interno | Falha inesperada |

---

## 6. Estrutura de Dados

Representação dos contratos como **DTOs (records)**, separando o que entra (`Requisicao`) do que sai (`Resposta`) e mantendo a camada de API independente das entidades JPA:

```java
public class BolsaComponenteDTO {

    public record Requisicao(
            Long doacaoId,
            TipoComponente tipoComponente,
            Integer volumeMl,
            LocalDate dataProducao,
            Long localizacaoAtualId
    ) {}

    public record AtualizacaoStatus(StatusBolsa status) {}

    public record Resposta(
            Long id,
            String codigoRastreio,
            Long doacaoId,
            TipoComponente tipoComponente,
            TipoABO tipoAbo,
            FatorRh fatorRh,
            Integer volumeMl,
            LocalDate dataProducao,
            LocalDate dataValidade,
            StatusBolsa status,
            Long localizacaoAtualId,
            String nomeLocalizacao,
            boolean vencida
    ) {}
}
```

O tratamento de erro é centralizado num único `@RestControllerAdvice`, garantindo que todo endpoint devolva o mesmo formato de erro descrito na seção 5, sem repetir tratamento em cada controller.

---
