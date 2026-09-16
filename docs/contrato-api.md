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

### Hospitais (`/hospitais`)

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/hospitais` | Cadastra um hospital |
| `GET` | `/hospitais` | Lista todos os hospitais |
| `GET` | `/hospitais/{id}` | Busca um hospital pelo id |
| `PUT` | `/hospitais/{id}` | Atualiza um hospital |
| `DELETE` | `/hospitais/{id}` | Inativação lógica (não exclui histórico) |

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
| `GET` | `/requisicoes?status=` | Lista requisições, com filtro opcional de status |
| `GET` | `/requisicoes/{id}` | Busca uma requisição pelo id |
| `PATCH` | `/requisicoes/{id}/status` | Transiciona o status da requisição |
| `DELETE` | `/requisicoes/{id}` | Cancela a requisição (soft delete) |

### Rota na malha (`/rotas`)

| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/rotas/caminho-minimo?origemId=&destinoId=&exigirCadeiaFria=` | Calcula a rota de menor tempo entre dois vértices |

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

### Consultar caminho mínimo

```http
GET /api/v1/rotas/caminho-minimo?origemId=4&destinoId=7&exigirCadeiaFria=true
```

```json
200 OK

{
  "caminho": [4, 8, 7],
  "custoTotalMinutos": 25.0,
  "existeRota": true
}
```

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
