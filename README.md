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
  * **Lista, Fila e Pilha (Unidade 1):** estoque de bolsas, requisições em ordem de chegada e histórico de operações — [detalhes](./docs/estruturas-u1.md)
  * **Unidade 2:** compatibilidade ABO/Rh, alocação FEFO e cálculo de caminhos mínimos com Dijkstra
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

### 🚀 Entrega 02

## 🎥 Vídeos Demonstrativos

### 📋 Histórias de Usuário Implementadas

> **História 1: Cálculo de Rota Otimizada (Caminho Mínimo)**
> * *Como* administrador logístico da Hemorrede
> * *Eu quero* calcular a rota mais rápida (Dijkstra) entre a origem e o destino
> * *Para que* as bolsas de sangue sejam entregues dentro do tempo limite da cadeia fria, garantindo a viabilidade do hemocomponente.

> **História 2: Alocação Inteligente de Estoque (FEFO)**
> * *Como* gestor de estoque do banco de sangue
> * *Eu quero* que o sistema aloque automaticamente as bolsas compatíveis mais próximas da data de vencimento (FEFO) para atender a uma requisição
> * *Para que* possamos minimizar o descarte de sangue por validade expirada e otimizar o uso do estoque.

### Demonstração de uso (histórias implementadas)

Screencast mostrando a aplicação Spring Boot em execução, demonstrando o funcionamento das histórias de usuário implementadas nesta etapa.

📺 **Assista no YouTube:** [Link do vídeo](https://youtu.be/-sY4YpXX600)

### Explicação do código

Screencast com a explicação do código da aplicação Spring Boot, cobrindo a implementação das histórias de usuário desenvolvidas.

📺 **Assista no YouTube:** [Link do vídeo](https://www.youtube.com/watch?v=TkPm2lFM_Es)

## 🚀 Entrega 3

### 📋 Histórias de Usuário Implementadas

> **História: Criação de Requisição Hospitalar**
> * *Como* responsável hospitalar
> * *Eu quero* criar uma requisição informando tipo sanguíneo, componente, quantidade necessária e janela de entrega
> * *Para que* o hemocentro saiba o que preciso e possa atendê-la.

> **História: Consulta de Status da Requisição**
> * *Como* responsável hospitalar
> * *Eu quero* consultar o status da minha requisição (pendente, alocada, em trânsito, entregue)
> * *Para que* eu saiba quando o componente vai chegar sem precisar ligar para o hemocentro.

### Demonstração de uso (histórias implementadas)

Screencast mostrando, pelo Swagger (`/docs`), a aplicação Spring Boot em execução: criação de uma requisição hospitalar, validação de campos obrigatórios, consulta por lista, por ID e por status, e a alocação de bolsas para o pedido.

📺 **Assista no YouTube:** [Link do vídeo](https://youtu.be/TbLK2uS_g5k)

### Explicação do código

Screencast com a explicação do código da aplicação Spring Boot, cobrindo a implementação das histórias de criação de requisição e consulta de status: Controller, DTOs, Service, entidade, Repository, tratamento de erros e regras de transição de status.

📺 **Assista no YouTube:** [Link do vídeo](https://youtu.be/O2WbMlvpl-Q)
---

## ⚙️ Como Executar o Projeto

### Pré-requisitos
* **Java JDK** 17 ou superior
* **Maven** (ou o wrapper `mvnw` / `mvnw.cmd` do projeto)
* **Git**
* **Docker** (opcional, para o deploy inicial)

### Instalação e execução local

```bash
git clone https://github.com/CMartinsO3/projeto_integrador_cesar2026.2
cd projeto_integrador_cesar2026.2
./mvnw spring-boot:run
```

No Windows: `.\mvnw.cmd spring-boot:run`

A aplicação sobe em [http://localhost:8080](http://localhost:8080) com dados sintéticos (topologia da malha, bolsas e uma requisição).

### Execução com Docker Compose

```bash
docker compose up --build
```

No Windows:

```powershell
docker compose up --build
```

A API fica disponível em [http://localhost:8080](http://localhost:8080) e o
PostgreSQL em um serviço interno do Compose. Para encerrar:

```bash
docker compose down
```

Para remover também o volume persistente do PostgreSQL:

```bash
docker compose down -v
```

### Pipeline e deploy

* **CI:** GitHub Actions em `.github/workflows/ci.yml` — `./mvnw -B verify` em push/PR.
* **CD:** GitHub Actions em `.github/workflows/deploy.yml`, com publicação no GHCR, deploy no Render e suporte a rollback via `workflow_dispatch`.

As evidências reproduzíveis de testes, containers, smoke tests e persistência
estão no [índice de evidências](./docs/evidencias/README.md).

### Endpoints da aplicação

| Recurso | Método | Rota |
|---|---|---|
| Painel | GET | `/` |
| Health check | GET | `/actuator/health` |
| Informações da aplicação | GET | `/actuator/info` |
| Topologia (nós + ligações) | GET | `/api/v1/topologia` |
| Nós da malha | GET/POST | `/api/v1/nos-rede` |
| Caminho mínimo (Dijkstra) | GET | `/api/v1/rotas/caminho-minimo?origemId=&destinoId=` |
| Bolsas | GET/POST | `/api/v1/bolsas` |
| Requisições | GET/POST | `/api/v1/requisicoes` |
| Alocação (ABO/Rh + FEFO) | POST | `/api/v1/requisicoes/{id}/alocar` |
| H2 Console | GET | `/h2-console` (JDBC: `jdbc:h2:mem:hemoflow`) |

Exemplo de rota Hemocentro → Hospital C (via nó intermediário, 35 min):

```bash
curl "http://localhost:8080/api/v1/rotas/caminho-minimo?origemId=1&destinoId=4"
```

## ✅ Estado atual da implementação

As regras principais da Unidade 2 estão implementadas e cobertas por testes:

- compatibilidade ABO/Rh por matriz 8x8;
- descarte lógico de bolsas vencidas;
- alocação FEFO, com desempate determinístico por ID;
- caminho mínimo em grafo ponderado com Dijkstra;
- integração Spring/JPA para persistência do status
  `AGUARDANDO_ESTOQUE`;
- cenários de aceitação Cucumber;
- pipeline CI/CD e evidências de execução local.

> A compatibilidade ABO/Rh deste projeto é didática e não substitui protocolos
> clínicos ou decisões transfusionais reais.
