# Guia de Uso da API - HemoFlow

> Guia prático com exemplos de uso de todos os endpoints da API REST implementados.

---

## 🌐 Informações Gerais

- **Base URL:** `http://localhost:8080/api/v1`
- **Formato:** JSON (`application/json`)
- **Documentação Interativa:** http://localhost:8080/docs

---

## 🏥 Hospitais

### Criar Hospital

```bash
POST http://localhost:8080/api/v1/hospitais
Content-Type: application/json

{
  "nome": "Hospital Barão de Lucena",
  "localizacao": "Recife - PE"
}
```

**Resposta (201 Created):**
```json
{
  "id": 1,
  "nome": "Hospital Barão de Lucena",
  "localizacao": "Recife - PE",
  "ativo": true,
  "dataCadastro": "2026-09-17T10:30:00"
}
```

### Listar Todos os Hospitais

```bash
GET http://localhost:8080/api/v1/hospitais
```

**Resposta (200 OK):**
```json
[
  {
    "id": 1,
    "nome": "Hospital Barão de Lucena",
    "localizacao": "Recife - PE",
    "ativo": true,
    "dataCadastro": "2026-09-17T10:30:00"
  },
  {
    "id": 2,
    "nome": "Hospital das Clínicas",
    "localizacao": "São Paulo - SP",
    "ativo": true,
    "dataCadastro": "2026-09-17T11:00:00"
  }
]
```

### Buscar Hospital por ID

```bash
GET http://localhost:8080/api/v1/hospitais/1
```

### Atualizar Hospital

```bash
PUT http://localhost:8080/api/v1/hospitais/1
Content-Type: application/json

{
  "nome": "Hospital Barão de Lucena - Unidade Central",
  "localizacao": "Recife - PE, Centro"
}
```

### Inativar Hospital

```bash
DELETE http://localhost:8080/api/v1/hospitais/1
```

**Resposta (204 No Content):**
Hospital inativado (soft delete), não é removido do banco.

---

## 🩸 Doações

### Registrar Doação

```bash
POST http://localhost:8080/api/v1/doacoes
Content-Type: application/json

{
  "tipoAbo": "O",
  "fatorRh": "NEGATIVO",
  "dataColeta": "2026-09-17",
  "volumeColetadoMl": 450,
  "centroColetaId": 1,
  "centroColetaNome": "Centro de Coleta Norte"
}
```

**Resposta (201 Created):**
```json
{
  "id": 1,
  "tipoAbo": "O",
  "fatorRh": "NEGATIVO",
  "dataColeta": "2026-09-17",
  "volumeColetadoMl": 450,
  "centroColetaId": 1,
  "centroColetaNome": "Centro de Coleta Norte"
}
```

### Listar Doações

```bash
GET http://localhost:8080/api/v1/doacoes
```

### Buscar Doação por ID

```bash
GET http://localhost:8080/api/v1/doacoes/1
```

---

## 💉 Bolsas/Componentes

### Criar Bolsa

```bash
POST http://localhost:8080/api/v1/bolsas
Content-Type: application/json

{
  "doacaoId": 1,
  "tipoComponente": "CONCENTRADO_HEMACIAS",
  "tipoAbo": "O",
  "fatorRh": "NEGATIVO",
  "volumeMl": 350,
  "dataProducao": "2026-09-17",
  "localizacaoAtualId": 2
}
```

**Resposta (201 Created):**
```json
{
  "id": 1,
  "codigoRastreio": "BC-A7F3D8E1",
  "doacaoId": 1,
  "tipoComponente": "CONCENTRADO_HEMACIAS",
  "tipoAbo": "O",
  "fatorRh": "NEGATIVO",
  "volumeMl": 350,
  "dataProducao": "2026-09-17",
  "dataValidade": "2026-10-29",
  "status": "DISPONIVEL",
  "localizacaoAtualId": 2,
  "nomeLocalizacao": null,
  "vencida": false,
  "diasParaVencimento": 42
}
```

### Listar Bolsas (com filtro)

```bash
# Todas as bolsas
GET http://localhost:8080/api/v1/bolsas

# Apenas disponíveis
GET http://localhost:8080/api/v1/bolsas?status=DISPONIVEL

# Apenas alocadas
GET http://localhost:8080/api/v1/bolsas?status=ALOCADA
```

**Valores válidos para status:**
- `DISPONIVEL`
- `ALOCADA`
- `EM_TRANSITO`
- `ENTREGUE`
- `DESCARTADA`

### Buscar Bolsa por ID

```bash
GET http://localhost:8080/api/v1/bolsas/1
```

### Atualizar Status da Bolsa

```bash
PATCH http://localhost:8080/api/v1/bolsas/1/status
Content-Type: application/json

{
  "status": "ALOCADA"
}
```

**Resposta (200 OK):**
```json
{
  "id": 1,
  "codigoRastreio": "BC-A7F3D8E1",
  "tipoComponente": "CONCENTRADO_HEMACIAS",
  "status": "ALOCADA",
  ...
}
```

**Validações:**
- ❌ Bolsa vencida só pode ser movida para `DESCARTADA`

### Remover Bolsa

```bash
DELETE http://localhost:8080/api/v1/bolsas/1
```

**Resposta (204 No Content)**

---

## 📋 Requisições

### Criar Requisição Hospitalar

```bash
POST http://localhost:8080/api/v1/requisicoes
Content-Type: application/json

{
  "hospitalId": 1,
  "tipoAbo": "A",
  "fatorRh": "POSITIVO",
  "tipoComponente": "CONCENTRADO_PLAQUETAS",
  "quantidade": 2,
  "urgencia": "URGENTE",
  "prazoLimite": "2026-09-17T18:00:00"
}
```

**Resposta (201 Created):**
```json
{
  "id": 1,
  "hospitalId": 1,
  "nomeHospital": "Hospital Barão de Lucena",
  "tipoAbo": "A",
  "fatorRh": "POSITIVO",
  "tipoComponente": "CONCENTRADO_PLAQUETAS",
  "quantidade": 2,
  "urgencia": "URGENTE",
  "status": "PENDENTE",
  "dataSolicitacao": "2026-09-17T10:30:00",
  "prazoLimite": "2026-09-17T18:00:00"
}
```

**Valores válidos para urgência:**
- `ROTINA` (prioridade 3)
- `URGENTE` (prioridade 2)
- `EMERGENCIA` (prioridade 1)

**Validações:**
- ❌ Hospital deve existir e estar ativo

### Listar Requisições (com filtro)

```bash
# Todas as requisições
GET http://localhost:8080/api/v1/requisicoes

# Apenas pendentes
GET http://localhost:8080/api/v1/requisicoes?status=PENDENTE

# Apenas atendidas
GET http://localhost:8080/api/v1/requisicoes?status=ATENDIDA
```

**Valores válidos para status:**
- `PENDENTE`
- `AGUARDANDO_ESTOQUE`
- `ALOCADA`
- `EM_TRANSITO`
- `ATENDIDA`
- `CANCELADA`

### Buscar Requisição por ID

```bash
GET http://localhost:8080/api/v1/requisicoes/1
```

### Atualizar Status da Requisição

```bash
PATCH http://localhost:8080/api/v1/requisicoes/1/status
Content-Type: application/json

{
  "status": "ALOCADA"
}
```

**Validações:**
- ❌ Requisição em status `ATENDIDA` ou `CANCELADA` não pode ser alterada

### Cancelar Requisição

```bash
DELETE http://localhost:8080/api/v1/requisicoes/1
```

**Resposta (204 No Content)**

Soft delete: requisição é marcada como `CANCELADA`, não é removida do banco.

---

## ⚠️ Tratamento de Erros

### Erro de Validação (400)

```bash
POST http://localhost:8080/api/v1/hospitais
Content-Type: application/json

{
  "nome": "",
  "localizacao": ""
}
```

**Resposta:**
```json
{
  "timestamp": "2026-09-17T10:30:00",
  "status": 400,
  "erro": "Erro de validação",
  "mensagem": "Um ou mais campos estão inválidos",
  "detalhes": [
    "nome: Nome é obrigatório",
    "localizacao: Localização é obrigatória"
  ]
}
```

### Recurso Não Encontrado (404)

```bash
GET http://localhost:8080/api/v1/hospitais/999
```

**Resposta:**
```json
{
  "timestamp": "2026-09-17T10:30:00",
  "status": 422,
  "erro": "Regra de negócio violada",
  "mensagem": "Hospital não encontrado com id: 999",
  "detalhes": []
}
```

### Regra de Negócio Violada (422)

```bash
PATCH http://localhost:8080/api/v1/bolsas/1/status
Content-Type: application/json

{
  "status": "ALOCADA"
}
```

**Resposta (se bolsa estiver vencida):**
```json
{
  "timestamp": "2026-09-17T10:30:00",
  "status": 422,
  "erro": "Regra de negócio violada",
  "mensagem": "Bolsa vencida só pode ser movida para DESCARTADA",
  "detalhes": []
}
```

---

## 🧪 Testando com cURL

### Criar Hospital

```bash
curl -X POST http://localhost:8080/api/v1/hospitais \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "Hospital Barão de Lucena",
    "localizacao": "Recife - PE"
  }'
```

### Listar Hospitais

```bash
curl http://localhost:8080/api/v1/hospitais
```

### Criar Bolsa

```bash
curl -X POST http://localhost:8080/api/v1/bolsas \
  -H "Content-Type: application/json" \
  -d '{
    "tipoComponente": "CONCENTRADO_HEMACIAS",
    "tipoAbo": "O",
    "fatorRh": "NEGATIVO",
    "volumeMl": 350,
    "dataProducao": "2026-09-17"
  }'
```

### Atualizar Status

```bash
curl -X PATCH http://localhost:8080/api/v1/bolsas/1/status \
  -H "Content-Type: application/json" \
  -d '{
    "status": "ALOCADA"
  }'
```

---

## 📊 Tipos de Componentes e Validades

| Tipo de Componente | Dias de Validade | Descrição |
|-------------------|------------------|-----------|
| `CONCENTRADO_HEMACIAS` | 42 dias | Hemácias concentradas |
| `PLASMA_FRESCO` | 365 dias | Plasma fresco congelado |
| `CONCENTRADO_PLAQUETAS` | 5 dias | Plaquetas (menor validade) |
| `CRIOPRECIPITADO` | 365 dias | Crioprecipitado |

---

## 🔗 Recursos Adicionais

- **Swagger UI:** http://localhost:8080/docs
  - Teste interativo de todos os endpoints
  - Visualização de schemas
  - Exemplos de requisições/respostas

- **OpenAPI JSON:** http://localhost:8080/api-docs
  - Especificação OpenAPI completa
  - Pode ser importada em ferramentas como Postman, Insomnia

- **H2 Console:** http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:hemoflowdb`
  - Username: `sa`
  - Password: (vazio)

---

## 📝 Fluxo de Uso Completo

### Cenário: Criar e atender uma requisição hospitalar

```bash
# 1. Criar hospital
POST /api/v1/hospitais
{
  "nome": "Hospital A",
  "localizacao": "Recife"
}
# Retorna: id=1

# 2. Registrar doação
POST /api/v1/doacoes
{
  "tipoAbo": "O",
  "fatorRh": "NEGATIVO",
  "dataColeta": "2026-09-17",
  "volumeColetadoMl": 450
}
# Retorna: id=1

# 3. Criar bolsa
POST /api/v1/bolsas
{
  "doacaoId": 1,
  "tipoComponente": "CONCENTRADO_HEMACIAS",
  "tipoAbo": "O",
  "fatorRh": "NEGATIVO",
  "volumeMl": 350,
  "dataProducao": "2026-09-17"
}
# Retorna: id=1, status=DISPONIVEL

# 4. Criar requisição
POST /api/v1/requisicoes
{
  "hospitalId": 1,
  "tipoAbo": "O",
  "fatorRh": "NEGATIVO",
  "tipoComponente": "CONCENTRADO_HEMACIAS",
  "quantidade": 1,
  "urgencia": "URGENTE"
}
# Retorna: id=1, status=PENDENTE

# 5. Alocar bolsa
PATCH /api/v1/bolsas/1/status
{
  "status": "ALOCADA"
}

# 6. Atualizar requisição
PATCH /api/v1/requisicoes/1/status
{
  "status": "ALOCADA"
}

# 7. Colocar em trânsito
PATCH /api/v1/bolsas/1/status
{
  "status": "EM_TRANSITO"
}

PATCH /api/v1/requisicoes/1/status
{
  "status": "EM_TRANSITO"
}

# 8. Confirmar entrega
PATCH /api/v1/bolsas/1/status
{
  "status": "ENTREGUE"
}

PATCH /api/v1/requisicoes/1/status
{
  "status": "ATENDIDA"
}
```

---

**Última Atualização:** 17 de setembro de 2026
