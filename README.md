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
| <img src="https://github.com/github.png" width="80" height="80" style="border-radius: 50%;"> | **Bruna Tiburtino** | bft@cesar.school | *Ex: Backend / Grafos* | [![LinkedIn](https://img.shields.io/badge/LinkedIn-0077B5?style=flat&logo=linkedin&logoColor=white)](https://linkedin.com) [![GitHub](https://img.shields.io/badge/GitHub-100000?style=flat&logo=github&logoColor=white)](https://github.com) |
| <img src="https://github.com/github.png" width="80" height="80" style="border-radius: 50%;"> | **Caio Martins Oliveira** | cmo3@cesar.school | *Ex: Frontend / Dashboards* | [![LinkedIn](https://img.shields.io/badge/LinkedIn-0077B5?style=flat&logo=linkedin&logoColor=white)](https://linkedin.com) [![GitHub](https://img.shields.io/badge/GitHub-100000?style=flat&logo=github&logoColor=white)](https://github.com) |
| <img src="https://github.com/github.png" width="80" height="80" style="border-radius: 50%;"> | **Cauã Henrique Melo Almeida** | chma@cesar.school | *Ex: Infraestrutura / Telemetria* | [![LinkedIn](https://img.shields.io/badge/LinkedIn-0077B5?style=flat&logo=linkedin&logoColor=white)](https://linkedin.com) [![GitHub](https://img.shields.io/badge/GitHub-100000?style=flat&logo=github&logoColor=white)](https://github.com) |
| <img src="https://github.com/github.png" width="80" height="80" style="border-radius: 50%;"> | **João Felipe Bonifácio Barros da Silva** | jfbbs@cesar.school | *Ex: Estatística & Regras de Negócio* | [![LinkedIn](https://img.shields.io/badge/LinkedIn-0077B5?style=flat&logo=linkedin&logoColor=white)](https://linkedin.com) [![GitHub](https://img.shields.io/badge/GitHub-100000?style=flat&logo=github&logoColor=white)](https://github.com) |
| <img src="https://github.com/github.png" width="80" height="80" style="border-radius: 50%;"> | **Luís Felipe Carneiro da Silva** | lfcs2@cesar.school | *Ex: RSD / API* | [![LinkedIn](https://img.shields.io/badge/LinkedIn-0077B5?style=flat&logo=linkedin&logoColor=white)](https://linkedin.com) [![GitHub](https://img.shields.io/badge/GitHub-100000?style=flat&logo=github&logoColor=white)](https://github.com) |
| <img src="https://github.com/github.png" width="80" height="80" style="border-radius: 50%;"> | **Luís Henrique Vilas Boas Silva de Sousa** | lhvbss@cesar.school | *Ex: RSD / Topologia* | [![LinkedIn](https://img.shields.io/badge/LinkedIn-0077B5?style=flat&logo=linkedin&logoColor=white)](https://linkedin.com) [![GitHub](https://img.shields.io/badge/GitHub-100000?style=flat&logo=github&logoColor=white)](https://github.com) |
| <img src="https://github.com/github.png" width="80" height="80" style="border-radius: 50%;"> | **Priscila Pontes Martins da Cunha** | rpmc@cesar.school | *Ex: SO / Pipeline* | [![LinkedIn](https://img.shields.io/badge/LinkedIn-0077B5?style=flat&logo=linkedin&logoColor=white)](https://linkedin.com) [![GitHub](https://img.shields.io/badge/GitHub-100000?style=flat&logo=github&logoColor=white)](https://github.com) |
| <img src="https://github.com/github.png" width="80" height="80" style="border-radius: 50%;"> | **Rafael Medeiros Machado Dias** | rmmd@cesar.school | *Ex: SO / Deploy* | [![LinkedIn](https://img.shields.io/badge/LinkedIn-0077B5?style=flat&logo=linkedin&logoColor=white)](https://linkedin.com) [![GitHub](https://img.shields.io/badge/GitHub-100000?style=flat&logo=github&logoColor=white)](https://github.com) |
| <img src="https://github.com/github.png" width="80" height="80" style="border-radius: 50%;"> | **Ruan Carlos Oliveira da Silva** | rcos3@cesar.school | *Ex: Estatística / Front* | [![LinkedIn](https://img.shields.io/badge/LinkedIn-0077B5?style=flat&logo=linkedin&logoColor=white)](https://linkedin.com) [![GitHub](https://img.shields.io/badge/GitHub-100000?style=flat&logo=github&logoColor=white)](https://github.com) |

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
$ cd hemoflow

# 3. Compilar e executar com Maven
$ ./mvnw spring-boot:run
```
