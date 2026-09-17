# 🩸 HemoFlow — Gestão e Distribuição de Hemocomponentes

> **Plataforma web para gestão inteligente de estoque, compatibilidade, roteirização e monitoramento da cadeia fria de hemocomponentes.**

---

## 📖 Sobre o Projeto

O **HemoFlow** é uma aplicação web desenvolvida em **Java com Spring Boot** voltada a apoiar a rede de distribuição de sangue. Inspirado no fluxo operacional da **Hemorrede/SUS** (*centros de coleta → hemocentros de processamento/estoque → hospitais*), o sistema visa solucionar gargalos operacionais críticos no transporte e armazenamento de hemoderivados.

### ⚠️ O Problema Central
A distribuição de hemocomponentes exige entregar a bolsa certa (**compatível e válida**), no lugar certo, dentro da janela de tempo adequada e sob controle rígido de temperatura. Falhas nesse processo resultam em:
* Desabastecimento de emergência em hospitais;
* Descarte de bolsas por vencimento de validade;
* Risco direto à vida dos pacientes.

### 💡 A Solução
O HemoFlow unifica em uma única plataforma web:
1. **Gestão e Alocação de Estoque:** Controle por tipo sanguíneo e componente, alocando bolsas compatíveis prioritariamente por validade (**FEFO** — *First Expired, First Out*).
2. **Roteirização Inteligente:** Cálculo de rotas otimizadas de distribuição, respeitando restrições de tempo e especificidades da **cadeia fria**.
3. **Painel de Monitoramento (Telemetria):** *Dashboards* para acompanhamento em tempo real do estoque/demanda, telemetria simulada de temperatura e status da rede de transporte.

---

## 🛠️ Tecnologias e Conceitos Utilizados

* **Backend / Aplicação Web:** Java, Spring Boot
* **Estruturas de Dados & Algoritmos:**
  * **Grafos e Caminhos Mínimos:** Roteirização e logística de entregas
  * **Filas de Prioridade & Tabela Hash:** Ordenação por validade (FEFO) e rápida busca por tipo/compatibilidade
  * **Matching de Compatibilidade:** Validação de regras ABO/Rh
* **Análise & Estatística:** Estatística descritiva e probabilidade aplicadas ao estoque/demanda
* **Infraestrutura & Redes:** Concorrência, CI/CD, Nuvem e Simulação de Telemetria/Comunicação de Rede

---

## 🎯 Limites e Escopo do Projeto

Para garantir um escopo controlado e seguro, o projeto adota os seguintes limites:
* **Dados Sintéticos:** Sem dados reais de doadores ou pacientes (aderência à LGPD).
* **Compatibilidade Didática:** Lógica de compatibilidade ABO/Rh voltada ao contexto acadêmico (não substitui protocolos clínicos formais).
* **Telemetria Simulada:** Dados de GPS e sensores de temperatura são gerados via simulação de software.
* **Isolamento:** Sistema independente, sem integração com sistemas oficiais do SUS/Hemorrede.

---

## 👥 Integrantes do Grupo

| Foto | Nome | E-mail (School) | Função / Responsabilidade | Redes / Contatos |
| :---: | :--- | :--- | :--- | :--- |
| <img src="https://github.com/github.png" width="80" height="80" style="border-radius: 50%;"> | **Bruna Tiburtino** | bft@cesar.school | *Frontend / Dashboards* | [![LinkedIn](https://img.shields.io/badge/LinkedIn-0077B5?style=flat&logo=linkedin&logoColor=white)](https://linkedin.com) [![GitHub](https://img.shields.io/badge/GitHub-100000?style=flat&logo=github&logoColor=white)](https://github.com) |
| <img src="https://github.com/github.png" width="80" height="80" style="border-radius: 50%;"> | **Caio Martins Oliveira** | cmo3@cesar.school | *SO / Pipeline* | [![LinkedIn](https://img.shields.io/badge/LinkedIn-0077B5?style=flat&logo=linkedin&logoColor=white)](https://linkedin.com) [![GitHub](https://img.shields.io/badge/GitHub-100000?style=flat&logo=github&logoColor=white)](https://github.com) |
| <img src="https://github.com/github.png" width="80" height="80" style="border-radius: 50%;"> | **Cauã Henrique Melo Almeida** | chma@cesar.school | *RSD / Topologia* | [![LinkedIn](https://img.shields.io/badge/LinkedIn-0077B5?style=flat&logo=linkedin&logoColor=white)](https://linkedin.com) [![GitHub](https://img.shields.io/badge/GitHub-100000?style=flat&logo=github&logoColor=white)](https://github.com) |
| <img src="https://github.com/github.png" width="80" height="80" style="border-radius: 50%;"> | **João Felipe Bonifácio Barros da Silva** | jfbbs@cesar.school | *Estatística / Front* | [![LinkedIn](https://img.shields.io/badge/LinkedIn-0077B5?style=flat&logo=linkedin&logoColor=white)](https://linkedin.com/in/joão-felipe-bonifácio00) [![GitHub](https://img.shields.io/badge/GitHub-100000?style=flat&logo=github&logoColor=white)](https://github.com) |
| <img src="https://github.com/github.png" width="80" height="80" style="border-radius: 50%;"> | **Luís Felipe Carneiro da Silva** | lfcs2@cesar.school | *SO / Deploy* | [![LinkedIn](https://img.shields.io/badge/LinkedIn-0077B5?style=flat&logo=linkedin&logoColor=white)](https://linkedin.com) [![GitHub](https://img.shields.io/badge/GitHub-100000?style=flat&logo=github&logoColor=white)](https://github.com) |
| <img src="https://github.com/github.png" width="80" height="80" style="border-radius: 50%;"> | **Luís Henrique Vilas Boas Silva de Sousa** | lhvbss@cesar.school | *Infraestrutura / Telemetria* | [![LinkedIn](https://img.shields.io/badge/LinkedIn-0077B5?style=flat&logo=linkedin&logoColor=white)](https://linkedin.com) [![GitHub](https://img.shields.io/badge/GitHub-100000?style=flat&logo=github&logoColor=white)](https://github.com) |
| <img src="https://github.com/github.png" width="80" height="80" style="border-radius: 50%;"> | **Priscila Pontes Martins da Cunha** | rpmc@cesar.school | *RSD / API* | [![LinkedIn](https://img.shields.io/badge/LinkedIn-0077B5?style=flat&logo=linkedin&logoColor=white)](https://linkedin.com) [![GitHub](https://img.shields.io/badge/GitHub-100000?style=flat&logo=github&logoColor=white)](https://github.com) |
| <img src="https://github.com/github.png" width="80" height="80" style="border-radius: 50%;"> | **Rafael Medeiros Machado Dias** | rmmd@cesar.school | *Estatística & Regras de Negócio* | [![LinkedIn](https://img.shields.io/badge/LinkedIn-0077B5?style=flat&logo=linkedin&logoColor=white)](https://linkedin.com) [![GitHub](https://img.shields.io/badge/GitHub-100000?style=flat&logo=github&logoColor=white)](https://github.com) |
| <img src="https://github.com/github.png" width="80" height="80" style="border-radius: 50%;"> | **Ruan Carlos Oliveira da Silva** | rcos3@cesar.school | *Backend / Grafos* | [![LinkedIn](https://img.shields.io/badge/LinkedIn-0077B5?style=flat&logo=linkedin&logoColor=white)](https://linkedin.com) [![GitHub](https://img.shields.io/badge/GitHub-100000?style=flat&logo=github&logoColor=white)](https://github.com) |

### 🔄 Membros Anteriores / Novos

> Preencher apenas se houver entrada ou saída de integrantes ao longo do projeto. Caso não haja, manter a tabela abaixo vazia/indicada como "Não houve alterações".

| Nome | E-mail (School) | Data de Entrada | Data de Saída |
| :--- | :--- | :--- | :--- |
| — | — | — | — |

---

## 📦 Entregas

> Cada entrega possui sua própria seção abaixo, com os links para os artefatos e screenshots correspondentes.

### 🚀 Entrega 01

* **Histórias de Usuário**:
  📄 [Histórias de Usuário](./docs/historia-usuarios.md)
* **Protótipo Lo-Fi (Figma)**:
  🎨 [Protótipo Lo-Fi no Figma](https://brand-chisel-23480165.figma.site/)
* **Screencast de apresentação do protótipo**:
  🎥 [Vídeo no YouTube](https://youtu.be/CSTc0O8W5v8)

<!--
### 🚀 Entrega 02
* Artefato 1: [link]
* Artefato 2: [link]
-->

---

## ⚙️ Como Executar o Projeto

> A explicação detalhada de execução passa a ser necessária a partir da Entrega 02, quando houver código funcional integrado. A seção abaixo será expandida progressivamente a cada entrega.

### Pré-requisitos
* **Java JDK** (versão 17 ou superior)
* **Maven** (ou wrapper `./mvnw` do projeto)
* **Git**

### Instalação e Execução

```bash
# 1. Clonar o repositório
$ git clone https://github.com/CMartinsO3/projeto_integrador_cesar2026.2

# 2. Entrar na pasta do projeto
$ cd projeto_integrador_cesar2026.2

# 3. Compilar e executar com Maven
$ ./mvnw spring-boot:run
```

A aplicação estará disponível em:
- **API REST:** http://localhost:8080/api/v1
- **Documentação Swagger:** http://localhost:8080/docs
- **H2 Console:** http://localhost:8080/h2-console

---

## 📡 Documentação da API

### Base Path
Todos os endpoints da API seguem o padrão: `/api/v1`

### Recursos Disponíveis

#### 🏥 Hospitais (`/api/v1/hospitais`)
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `POST` | `/hospitais` | Cadastrar novo hospital |
| `GET` | `/hospitais` | Listar todos os hospitais |
| `GET` | `/hospitais/{id}` | Buscar hospital por ID |
| `PUT` | `/hospitais/{id}` | Atualizar hospital |
| `DELETE` | `/hospitais/{id}` | Inativar hospital (soft delete) |

#### 🩸 Doações (`/api/v1/doacoes`)
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `POST` | `/doacoes` | Registrar nova doação |
| `GET` | `/doacoes` | Listar todas as doações |
| `GET` | `/doacoes/{id}` | Buscar doação por ID |

#### 💉 Bolsas/Componentes (`/api/v1/bolsas`)
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `POST` | `/bolsas` | Criar nova bolsa/componente |
| `GET` | `/bolsas?status=` | Listar bolsas (filtro opcional por status) |
| `GET` | `/bolsas/{id}` | Buscar bolsa por ID |
| `PATCH` | `/bolsas/{id}/status` | Atualizar status da bolsa |
| `DELETE` | `/bolsas/{id}` | Remover bolsa |

#### 📋 Requisições (`/api/v1/requisicoes`)
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `POST` | `/requisicoes` | Criar requisição hospitalar |
| `GET` | `/requisicoes?status=` | Listar requisições (filtro opcional por status) |
| `GET` | `/requisicoes/{id}` | Buscar requisição por ID |
| `PATCH` | `/requisicoes/{id}/status` | Atualizar status da requisição |
| `DELETE` | `/requisicoes/{id}` | Cancelar requisição |

### Documentação Interativa

Acesse a documentação completa com exemplos e testes interativos em:
**http://localhost:8080/docs**

A documentação Swagger/OpenAPI permite:
- Visualizar todos os endpoints disponíveis
- Testar requisições diretamente no navegador
- Ver schemas de entrada e saída
- Consultar códigos de status HTTP e mensagens de erro

### Exemplos de Requisições

#### Criar Hospital
```bash
POST /api/v1/hospitais
Content-Type: application/json

{
  "nome": "Hospital Barão de Lucena",
  "localizacao": "Recife - PE"
}
```

#### Criar Bolsa de Sangue
```bash
POST /api/v1/bolsas
Content-Type: application/json

{
  "doacaoId": 1,
  "tipoComponente": "CONCENTRADO_HEMACIAS",
  "tipoAbo": "O",
  "fatorRh": "NEGATIVO",
  "volumeMl": 350,
  "dataProducao": "2026-09-10",
  "localizacaoAtualId": 2
}
```

#### Criar Requisição Hospitalar
```bash
POST /api/v1/requisicoes
Content-Type: application/json

{
  "hospitalId": 1,
  "tipoAbo": "A",
  "fatorRh": "POSITIVO",
  "tipoComponente": "CONCENTRADO_PLAQUETAS",
  "quantidade": 2,
  "urgencia": "URGENTE",
  "prazoLimite": "2026-09-15T14:00:00"
}
```

Para mais detalhes sobre os contratos de API, consulte: 📄 [Contrato de API](./docs/contrato-api.md)

---

## 📊 Arquitetura e Diagramas

### Diagrama de Arquitetura
O diagrama completo de arquitetura de rede, componentes e topologia está disponível em:
- **Fonte PlantUML:** [docs/diagrama-arquitetura.puml](./docs/diagrama-arquitetura.puml)
- **Instruções de Exportação:** [docs/instrucoes-diagrama.md](./docs/instrucoes-diagrama.md)

### Documentação Técnica Adicional
- 📄 [Modelo de Domínio](./docs/modelo-dominio.md) - Entidades, atributos e relacionamentos
- 📄 [Escopo do Grafo](./docs/escopo-grafo.md) - Topologia de roteirização e algoritmo Dijkstra
- 📄 [Histórias de Usuário](./docs/historia-usuarios.md) - Requisitos funcionais em formato BDD
