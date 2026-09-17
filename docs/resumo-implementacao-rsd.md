# Resumo de Implementação - RSD (Redes e Sistemas Distribuídos)

> Documento de referência das implementações realizadas referentes às pendências de RSD identificadas na auditoria das Sprints 2-7.

---

## ✅ 1. Contratos de API e Endpoints Implementados

### 🔧 Configuração do Swagger/OpenAPI

**Dependência Adicionada no pom.xml:**
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.3.0</version>
</dependency>
```

**Arquivo de Configuração:**
- `src/main/java/com/hemoflow/hemoflow/config/OpenApiConfig.java`

**Endpoints de Documentação:**
- Swagger UI: `http://localhost:8080/docs`
- OpenAPI JSON: `http://localhost:8080/api-docs`

---

### 🏥 HospitalController - COMPLETO

**Localização:** `src/main/java/com/hemoflow/hemoflow/api/controller/HospitalController.java`

| Método | Endpoint | Status | Descrição |
|--------|----------|--------|-----------|
| ✅ POST | `/api/v1/hospitais` | **Implementado** | Cadastrar hospital |
| ✅ GET | `/api/v1/hospitais` | **Implementado** | Listar hospitais |
| ✅ GET | `/api/v1/hospitais/{id}` | **Implementado** | Buscar por ID |
| ✅ PUT | `/api/v1/hospitais/{id}` | **Implementado** | Atualizar hospital |
| ✅ DELETE | `/api/v1/hospitais/{id}` | **Implementado** | Inativar (soft delete) |

**Funcionalidades:**
- Validação com Bean Validation (`@Valid`)
- Soft delete (inativação lógica)
- Anotações Swagger para documentação

---

### 💉 BolsaController - COMPLETO

**Localização:** `src/main/java/com/hemoflow/hemoflow/api/controller/BolsaController.java`

| Método | Endpoint | Status | Descrição |
|--------|----------|--------|-----------|
| ✅ POST | `/api/v1/bolsas` | **Implementado** | Criar bolsa |
| ✅ GET | `/api/v1/bolsas?status=` | **Implementado** | Listar com filtro |
| ✅ GET | `/api/v1/bolsas/{id}` | **Implementado** | Buscar por ID |
| ✅ PATCH | `/api/v1/bolsas/{id}/status` | **Implementado** | Transição de status |
| ✅ DELETE | `/api/v1/bolsas/{id}` | **Implementado** | Remover bolsa |

**Funcionalidades:**
- Geração automática de código de rastreio
- Cálculo automático de data de validade
- Validação de transições de status (bolsa vencida → só DESCARTADA)
- Método `isVencida()` e `diasParaVencimento()`

---

### 📋 RequisicaoController - COMPLETO

**Localização:** `src/main/java/com/hemoflow/hemoflow/api/controller/RequisicaoController.java`

| Método | Endpoint | Status | Descrição |
|--------|----------|--------|-----------|
| ✅ POST | `/api/v1/requisicoes` | **Implementado** | Criar requisição |
| ✅ GET | `/api/v1/requisicoes?status=` | **Implementado** | Listar com filtro |
| ✅ GET | `/api/v1/requisicoes/{id}` | **Implementado** | Buscar por ID |
| ✅ PATCH | `/api/v1/requisicoes/{id}/status` | **Implementado** | Atualizar status |
| ✅ DELETE | `/api/v1/requisicoes/{id}` | **Implementado** | Cancelar (soft delete) |

**Funcionalidades:**
- Validação de hospital ativo
- Bloqueio de alteração de status final (ATENDIDA/CANCELADA)
- Soft delete com transição para CANCELADA

---

### 🩸 DoacaoController - COMPLETO

**Localização:** `src/main/java/com/hemoflow/hemoflow/api/controller/DoacaoController.java`

| Método | Endpoint | Status | Descrição |
|--------|----------|--------|-----------|
| ✅ POST | `/api/v1/doacoes` | **Implementado** | Registrar doação |
| ✅ GET | `/api/v1/doacoes` | **Implementado** | Listar doações |
| ✅ GET | `/api/v1/doacoes/{id}` | **Implementado** | Buscar por ID |

**Funcionalidades:**
- Registro imutável (doação é fato ocorrido, sem PUT)
- Timestamp automático de cadastro

---

## 🗂️ Estrutura Implementada

### Pacotes Criados

```
src/main/java/com/hemoflow/hemoflow/
├── api/
│   ├── controller/
│   │   ├── BolsaController.java         ✅
│   │   ├── DoacaoController.java        ✅
│   │   ├── HospitalController.java      ✅
│   │   └── RequisicaoController.java    ✅
│   ├── dto/
│   │   ├── BolsaComponenteDTO.java      ✅
│   │   ├── DoacaoDTO.java               ✅
│   │   ├── HospitalDTO.java             ✅
│   │   └── RequisicaoDTO.java           ✅
│   └── exception/
│       └── GlobalExceptionHandler.java  ✅
├── config/
│   └── OpenApiConfig.java               ✅
├── domain/
│   ├── entity/
│   │   ├── BolsaComponente.java         ✅
│   │   ├── Doacao.java                  ✅
│   │   ├── Hospital.java                ✅
│   │   └── Requisicao.java              ✅
│   └── enums/
│       ├── FatorRh.java                 ✅
│       ├── NivelUrgencia.java           ✅
│       ├── StatusBolsa.java             ✅
│       ├── StatusRequisicao.java        ✅
│       ├── TipoABO.java                 ✅
│       └── TipoComponente.java          ✅
├── repository/
│   ├── BolsaComponenteRepository.java   ✅
│   ├── DoacaoRepository.java            ✅
│   ├── HospitalRepository.java          ✅
│   └── RequisicaoRepository.java        ✅
├── service/
│   ├── BolsaComponenteService.java      ✅
│   ├── DoacaoService.java               ✅
│   ├── HospitalService.java             ✅
│   └── RequisicaoService.java           ✅
└── [estrutura existente...]
```

---

## 🛡️ Tratamento de Erros

**Classe:** `GlobalExceptionHandler.java`

Centraliza o tratamento de exceções com formato padronizado:

```json
{
  "timestamp": "2026-09-15T10:00:00",
  "status": 422,
  "erro": "Regra de negócio violada",
  "mensagem": "Bolsa vencida só pode ser movida para DESCARTADA",
  "detalhes": []
}
```

**Códigos HTTP Implementados:**
- `400` - Erro de validação (campos obrigatórios/formato)
- `404` - Recurso não encontrado
- `422` - Regra de negócio violada
- `500` - Erro interno

---

## 📊 2. Arquitetura e Diagramas

### Diagrama PlantUML

**Localização:** `docs/diagrama-arquitetura.puml`

**Conteúdo:**
- ✅ Camadas da arquitetura (Apresentação, API, Negócio, Persistência)
- ✅ Controllers REST mapeados
- ✅ Services e algoritmos (Dijkstra, FEFO)
- ✅ Topologia de rede com nós (Hemocentro + 5 Hospitais + Nó Intermediário)
- ✅ Arestas com pesos (tempo em minutos)
- ✅ Fluxos de dados principais documentados
- ✅ Relacionamentos entre camadas

**Instruções de Exportação:** `docs/instrucoes-diagrama.md`

**Formatos de Exportação Suportados:**
- PNG
- SVG
- PDF

**Métodos de Geração:**
1. PlantUML Online (https://www.plantuml.com/plantuml/)
2. VS Code + Extensão PlantUML
3. PlantUML CLI (Java + JAR)
4. Draw.io com importação PlantUML

---

## 🗄️ Configuração de Banco de Dados

**Arquivo:** `src/main/resources/application.properties`

```properties
# H2 Database (In-Memory)
spring.datasource.url=jdbc:h2:mem:hemoflowdb
spring.jpa.hibernate.ddl-auto=update

# H2 Console
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# Swagger/OpenAPI
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/docs
```

---

## ✨ Funcionalidades Implementadas

### Regras de Negócio

1. **Bolsas:**
   - Código de rastreio único gerado automaticamente (BC-XXXXXXXX)
   - Data de validade calculada automaticamente baseada no tipo de componente
   - Validação de transições de status
   - Bolsa vencida só pode ser descartada

2. **Hospitais:**
   - Soft delete (inativação lógica)
   - Hospital inativo não pode criar requisições

3. **Requisições:**
   - Timestamps automáticos (solicitação, atualização)
   - Bloqueio de alteração em status final
   - Soft delete com transição para CANCELADA

4. **Doações:**
   - Registro imutável (sem PUT)
   - Timestamp de cadastro automático

### Validações

- ✅ Bean Validation (`@NotNull`, `@NotBlank`, `@Positive`)
- ✅ Validação de regras de negócio nos serviços
- ✅ Tratamento centralizado de erros
- ✅ Mensagens de erro padronizadas

---

## 🚀 Como Executar

### Pré-requisitos
- Java 17+
- Maven 3.6+

### Comandos

```bash
# Compilar
mvn clean compile

# Executar testes
mvn test

# Executar aplicação
mvn spring-boot:run
```

### URLs Importantes

| Serviço | URL |
|---------|-----|
| API Base | http://localhost:8080/api/v1 |
| Swagger UI | http://localhost:8080/docs |
| OpenAPI JSON | http://localhost:8080/api-docs |
| H2 Console | http://localhost:8080/h2-console |

---

## 📝 Checklist de Auditoria

### ✅ Contratos de API (Sprints 2-3)

- [x] HospitalController: POST, PUT, DELETE implementados
- [x] BolsaController: PATCH /status e DELETE implementados
- [x] RequisicaoController: PATCH /status implementado
- [x] DoacaoController: POST e GET implementados
- [x] Dependência springdoc-openapi-starter-webmvc-ui adicionada
- [x] Configuração OpenApiConfig criada
- [x] Endpoint /docs disponível

### ✅ Arquitetura e Diagramas (Sprints 4-5)

- [x] Diagrama de arquitetura em PlantUML criado
- [x] Topologia de rede documentada
- [x] Instruções de exportação para PDF/PNG fornecidas
- [x] Fluxos de dados mapeados
- [x] Camadas da arquitetura documentadas

### ✅ Documentação

- [x] README.md atualizado com seção de API
- [x] Exemplos de requisições fornecidos
- [x] Tabela de endpoints completa
- [x] Links para documentação técnica

---

## 🎯 Próximos Passos Recomendados

1. **Implementar GrafoService e algoritmo de Dijkstra**
   - Calcular rotas entre Hemocentro e Hospitais
   - Endpoint `/api/v1/rotas/caminho-minimo`

2. **Implementar compatibilidade ABO/Rh**
   - Matriz de compatibilidade
   - Filtro de bolsas compatíveis com requisição

3. **Implementar FilaFEFO**
   - Priorização por validade
   - Integração com BolsaService

4. **Testes Unitários**
   - Cobertura de Services
   - Cobertura de Controllers

5. **Testes de Integração**
   - Testes de API com MockMvc
   - Validação de contratos

---

## 📚 Referências

- [Contrato de API](./contrato-api.md)
- [Modelo de Domínio](./modelo-dominio.md)
- [Escopo do Grafo](./escopo-grafo.md)
- [Histórias de Usuário](./historia-usuarios.md)
- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Springdoc OpenAPI](https://springdoc.org/)

---

**Data da Implementação:** 17 de setembro de 2026  
**Responsável:** RSD / Contratos de API e Topologia de Rede  
**Status:** ✅ Completo
