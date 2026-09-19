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

### Pipeline / deploy inicial

* **CI:** GitHub Actions em `.github/workflows/ci.yml` — `./mvnw -B verify` em push/PR.
* **Container:**

```bash
docker compose up --build
```

### Endpoints da etapa 3–4

| Recurso | Método | Rota |
|---|---|---|
| Painel | GET | `/` |
| Topologia (nós + ligações) | GET | `/api/v1/topologia` |
| Nós da malha | GET/POST | `/api/v1/nos-rede` |
| Caminho mínimo (Dijkstra) | GET | `/api/v1/rotas/caminho-minimo?origemId=&destinoId=` |
| Bolsas | GET/POST | `/api/v1/bolsas` |
| Próxima bolsa (FEFO) | GET | `/api/v1/estoque/proxima-bolsa` |
| Requisições | GET/POST | `/api/v1/requisicoes` |
| Alocação FEFO + ABO/Rh | POST | `/api/v1/requisicoes/{id}/alocar` |
| H2 Console | GET | `/h2-console` (JDBC: `jdbc:h2:mem:hemoflow`) |

Exemplo de rota Hemocentro → Hospital C (via nó intermediário, 35 min):

```bash
curl "http://localhost:8080/api/v1/rotas/caminho-minimo?origemId=1&destinoId=4"
```
