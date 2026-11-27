# 🩸 HemoFlow — Apresentação Interativa Docker Pitch Deck

> **"Docker: do código ao ambiente executável."**  
> Apresentação interativa web desenvolvida em HTML5, CSS3 e JavaScript moderno puro (sem dependências externas), documentando com rigor técnico e acabamento visual de alto padrão como o Docker foi empregado na conteinerização e orquestração do **HemoFlow** (Projeto Integrador CESAR School 2026.2).

---

## 🚀 Como Executar a Apresentação

A apresentação foi desenhada para funcionar **100% offline**, diretamente no navegador, sem exigir conexão com a internet ou instalação de pacotes adicionais.

### Opção 1 — Abrir Diretamente no Navegador (Recomendado)
Dê um duplo clique no arquivo [`index.html`](./index.html) ou abra-o em qualquer navegador moderno (Google Chrome, Microsoft Edge, Mozilla Firefox, Safari):

```bash
# No Windows PowerShell:
Start-Process "docker-presentation\index.html"
```

### Opção 2 — Executar via Servidor HTTP Local (Opcional)
Se preferir servir via HTTP local:

```bash
# Com Python 3:
python -m http.server 3000 --directory docker-presentation

# Ou com Node.js (npx serve):
npx serve docker-presentation
```
Em seguida, acesse: `http://localhost:3000`

---

## ⌨️ Navegação e Atalhos do Teclado

A experiência de navegação foi modelada como um deck de pitch profissional:

| Tecla / Controle | Ação |
| :--- | :--- |
| <kbd>→</kbd> ou <kbd>Espaço</kbd> ou <kbd>PageDown</kbd> | Avançar para o próximo slide |
| <kbd>←</kbd> ou <kbd>PageUp</kbd> | Voltar para o slide anterior |
| <kbd>Home</kbd> / <kbd>End</kbd> | Ir diretamente para o primeiro / último slide |
| <kbd>F</kbd> | Alternar modo **Tela Cheia** (*Fullscreen Presentation Mode*) |
| <kbd>O</kbd> | Abrir **Visão Geral dos Slides** (*Overview Grid*) |
| <kbd>?</kbd> | Abrir modal com atalhos de teclado |
| <kbd>Esc</kbd> | Fechar modais abertos |
| **Botões Flutuantes** | Controles no canto inferior direito para avançar/retroceder com o mouse |

---

## 🧭 Estrutura Narrativa dos 9 Slides

1. **Slide 1 — Abertura Cinematográfica**
   - Contextualização do HemoFlow (CESAR School 2026.2).
   - Micro-cards de infraestrutura: Multi-stage build, Docker Compose, resiliência de healthchecks e pipeline CI/CD.

2. **Slide 2 — O Problema Que Precisávamos Resolver**
   - Comparativo visual entre o ambiente frágil pré-Docker (drift de JDK, dependência de Postgres local, race conditions de boot) e o ambiente blindado com containers.

3. **Slide 3 — A Arquitetura do Projeto (Diagrama Interativo)**
   - Topologia completa: Host machine (`:8080`), rede bridge isolada (`hemoflow-net`), container `hemoflow-api`, container `hemoflow-db` (PostgreSQL 16.4 Alpine) e volume nomeado `hemoflow_pgdata`.
   - **Inspector Interativo:** Clique em qualquer componente para carregar em tempo real sua configuração extraída do `compose.yaml` e seu benefício arquitetural.

4. **Slide 4 — Por Dentro do Dockerfile**
   - Dissecção do `Dockerfile` multi-stage:
     - **Estágio 1 (Build):** `maven:3.9.9-eclipse-temurin-17`, cache de dependências com `./mvnw dependency:go-offline` e gate de qualidade com `./mvnw verify` (39 testes).
     - **Estágio 2 (Runtime):** `eclipse-temurin:17-jre-jammy`, segurança não-root com `hemoflow:hemoflow` (UID 1001), metadados OCI e `HEALTHCHECK` nativo.

5. **Slide 5 — Docker Compose e Orquestração Local**
   - Orquestração declarativa do `compose.yaml`:
     - Coordenação por prontidão: `depends_on.db.condition: service_healthy`.
     - Resolução DNS interna: `jdbc:postgresql://db:5432/hemoflow`.
     - Persistência e descarte controlado de dados via volume.

6. **Slide 6 — O Vídeo: Código e Infraestrutura Ganhando Vida**
   - **O Ponto Alto da Apresentação:** Cockpit simulado sincronizado em 4 painéis:
     - **Painel A (Código):** Trechos autênticos sincronizados com a fase atual.
     - **Painel B (Terminal):** Digitação de comandos, logs reais de build Maven, 39 testes aprovados, start do banco e status `healthy`.
     - **Painel C (Arquitetura Viva):** Caixas visuais transitando entre estados (*IDLE*, *BUILDING*, *STARTING*, *HEALTHY*).
     - **Painel D (Indicadores):** Badges de telemetria em tempo real (Build, DB Health, API Health, Rede, Porta, Volume).
   - Controles de reprodução: Play/Pause, Passo Anterior, Próximo Passo, Reiniciar, Seletor de Velocidade (0.75x, 1x, 1.5x, 2x) e barra de progresso interativa.

7. **Slide 7 — Como Executar o Projeto**
   - Guia de comandos reais (`docker compose up --build`, `up -d`, `ps`, `logs -f api`, `curl /actuator/health`, `down`, `down -v`) com **botões de cópia funcional com feedback visual**.

8. **Slide 8 — O Que Ganhamos Com Docker**
   - Ganhos comprovados do projeto (paridade dev/CI/prod, segurança com UID 1001, eliminação de race conditions, persistência isolada).
   - Diferenciação estrita entre o que foi efetivamente implementado e evoluções futuras (Kubernetes, réplicas de leitura, autoscaling).

9. **Slide 9 — Encerramento**
   - Conclusão com frase de impacto, créditos aos 9 integrantes da equipe (CESAR School) e botões de reinício e consulta.

---

## 📁 Arquivos do Módulo da Apresentação

```text
docker-presentation/
├── index.html       # Estrutura semântica, acessibilidade e conteúdo integral dos 9 slides
├── styles.css       # Design system dark premium (Docker Blue, Glassmorphism, 16:9 responsive)
├── script.js        # Engine de slides, cockpit de simulação, inspector e atalhos
└── README.md        # Documentação operacional da apresentação
```
