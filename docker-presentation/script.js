/**
 * HemoFlow — Docker Pitch Deck Interactive Script
 * High-performance, dependency-free vanilla JavaScript
 */

document.addEventListener('DOMContentLoaded', () => {
  /* ==========================================================================
     1. State Management & DOM References
     ========================================================================== */
  const slides = Array.from(document.querySelectorAll('.slide'));
  const totalSlides = slides.length;
  let currentSlideIndex = 0;

  // Header & Controls Elements
  const currentSlideDisplay = document.getElementById('currentSlideDisplay');
  const currentTopicCrumb = document.getElementById('currentTopicCrumb');
  const progressBar = document.getElementById('progressBar');
  const btnPrevSlide = document.getElementById('btnPrevSlide');
  const btnNextSlide = document.getElementById('btnNextSlide');
  const btnFullscreen = document.getElementById('btnFullscreen');
  const btnOverview = document.getElementById('btnOverview');
  const btnHelp = document.getElementById('btnHelp');

  // Modals
  const modalOverview = document.getElementById('modalOverview');
  const btnCloseOverview = document.getElementById('btnCloseOverview');
  const overviewGrid = document.getElementById('overviewGrid');
  const modalHelp = document.getElementById('modalHelp');
  const btnCloseHelp = document.getElementById('btnCloseHelp');
  const toastMsg = document.getElementById('toastMsg');

  /* ==========================================================================
     2. Slide Navigation Logic
     ========================================================================== */
  function updateSlide(newIndex) {
    if (newIndex < 0 || newIndex >= totalSlides) return;

    // Previous active slide cleanup
    slides[currentSlideIndex].classList.remove('active');
    slides[currentSlideIndex].classList.add(newIndex > currentSlideIndex ? 'previous' : '');

    // Pause simulation if leaving slide 6 (index 5)
    if (currentSlideIndex === 5 && newIndex !== 5) {
      pauseSimulation();
    }

    currentSlideIndex = newIndex;

    // Remove previous classes from other slides
    slides.forEach((s, idx) => {
      if (idx !== currentSlideIndex) {
        s.classList.remove('active');
        if (idx > currentSlideIndex) s.classList.remove('previous');
      }
    });

    const activeSlide = slides[currentSlideIndex];
    activeSlide.classList.add('active');
    activeSlide.classList.remove('previous');

    // Update Counter & Topic
    currentSlideDisplay.textContent = String(currentSlideIndex + 1).padStart(2, '0');
    currentTopicCrumb.textContent = activeSlide.dataset.title || `Slide ${currentSlideIndex + 1}`;

    // Update Progress Bar
    const progressPercent = ((currentSlideIndex + 1) / totalSlides) * 100;
    progressBar.style.width = `${progressPercent}%`;

    // Update Nav Buttons State
    btnPrevSlide.disabled = currentSlideIndex === 0;
    btnNextSlide.disabled = currentSlideIndex === totalSlides - 1;

    // Sync Hash
    window.location.hash = `slide-${currentSlideIndex + 1}`;

    // Auto-focus active slide for accessibility
    activeSlide.focus({ preventScroll: true });
  }

  function nextSlide() {
    if (currentSlideIndex < totalSlides - 1) {
      updateSlide(currentSlideIndex + 1);
    }
  }

  function prevSlide() {
    if (currentSlideIndex > 0) {
      updateSlide(currentSlideIndex - 1);
    }
  }

  btnPrevSlide.addEventListener('click', prevSlide);
  btnNextSlide.addEventListener('click', nextSlide);

  // Initialize from Hash
  function initFromHash() {
    const hash = window.location.hash;
    const match = hash.match(/^#slide-(\d+)$/);
    if (match) {
      const slideNum = parseInt(match[1], 10);
      if (slideNum >= 1 && slideNum <= totalSlides) {
        updateSlide(slideNum - 1);
        return;
      }
    }
    updateSlide(0);
  }

  /* ==========================================================================
     3. Keyboard Navigation
     ========================================================================== */
  document.addEventListener('keydown', (e) => {
    // If typing inside an input or textarea, don't trigger navigation
    if (['INPUT', 'TEXTAREA'].includes(e.target.tagName)) return;

    // Escape closes modals
    if (e.key === 'Escape') {
      closeAllModals();
      return;
    }

    // Modal navigation block
    if (modalOverview.classList.contains('open') || modalHelp.classList.contains('open')) {
      return;
    }

    switch (e.key) {
      case 'ArrowRight':
      case ' ':
      case 'PageDown':
      case 'l':
      case 'L':
        e.preventDefault();
        nextSlide();
        break;

      case 'ArrowLeft':
      case 'PageUp':
      case 'h':
      case 'H':
        e.preventDefault();
        prevSlide();
        break;

      case 'Home':
        e.preventDefault();
        updateSlide(0);
        break;

      case 'End':
        e.preventDefault();
        updateSlide(totalSlides - 1);
        break;

      case 'f':
      case 'F':
        e.preventDefault();
        toggleFullscreen();
        break;

      case 'o':
      case 'O':
        e.preventDefault();
        toggleOverview();
        break;

      case '?':
        e.preventDefault();
        toggleHelp();
        break;
    }
  });

  /* ==========================================================================
     4. Fullscreen & Modals
     ========================================================================== */
  function toggleFullscreen() {
    if (!document.fullscreenElement) {
      document.documentElement.requestFullscreen().catch(err => {
        console.warn(`Erro ao ativar modo tela cheia: ${err.message}`);
      });
    } else {
      if (document.exitFullscreen) {
        document.exitFullscreen();
      }
    }
  }

  btnFullscreen.addEventListener('click', toggleFullscreen);

  function closeAllModals() {
    modalOverview.classList.remove('open');
    modalHelp.classList.remove('open');
  }

  function toggleOverview() {
    const isOpen = modalOverview.classList.contains('open');
    closeAllModals();
    if (!isOpen) {
      populateOverviewGrid();
      modalOverview.classList.add('open');
    }
  }

  function toggleHelp() {
    const isOpen = modalHelp.classList.contains('open');
    closeAllModals();
    if (!isOpen) {
      modalHelp.classList.add('open');
    }
  }

  btnOverview.addEventListener('click', toggleOverview);
  btnCloseOverview.addEventListener('click', closeAllModals);
  btnHelp.addEventListener('click', toggleHelp);
  btnCloseHelp.addEventListener('click', closeAllModals);

  // Close modals on clicking overlay background
  [modalOverview, modalHelp].forEach(m => {
    m.addEventListener('click', (e) => {
      if (e.target === m) closeAllModals();
    });
  });

  function populateOverviewGrid() {
    overviewGrid.innerHTML = '';
    slides.forEach((slide, idx) => {
      const card = document.createElement('div');
      card.className = `overview-card ${idx === currentSlideIndex ? 'current' : ''}`;
      card.innerHTML = `
        <span class="overview-card-num">${String(idx + 1).padStart(2, '0')}</span>
        <span class="overview-card-title">${slide.dataset.title || `Slide ${idx + 1}`}</span>
      `;
      card.addEventListener('click', () => {
        updateSlide(idx);
        closeAllModals();
      });
      overviewGrid.appendChild(card);
    });
  }

  /* ==========================================================================
     5. Slide 3: Interactive Architecture Diagram Inspector
     ========================================================================== */
  const archNodes = document.querySelectorAll('.arch-node, .volume-node');
  const inspectorTitle = document.getElementById('inspectorTitle');
  const inspectorType = document.getElementById('inspectorType');
  const inspectorRole = document.getElementById('inspectorRole');
  const inspectorCode = document.getElementById('inspectorCode');
  const inspectorBenefit = document.getElementById('inspectorBenefit');

  const archDetails = {
    api: {
      title: 'hemoflow-api',
      type: 'Container de Aplicação (Spring Boot 3)',
      role: 'Hospeda a API REST do HemoFlow em Java 17. Implementa as regras de negócio de compatibilidade ABO/Rh (matriz 8x8), alocação de estoque por FEFO (First Expired, First Out) e o algoritmo de Dijkstra para caminhos mínimos na malha da Hemorrede.',
      code: `api:
  build:
    context: .
    dockerfile: Dockerfile
    args:
      GIT_SHA: \\\${GIT_SHA:-local-compose}
      APP_VERSION: \\\${APP_VERSION:-0.0.1-SNAPSHOT}
  container_name: hemoflow-api
  restart: unless-stopped
  depends_on:
    db:
      condition: service_healthy
  ports:
    - "\\\${PORT:-8080}:\\\${PORT:-8080}"
  environment:
    SPRING_PROFILES_ACTIVE: \\\${SPRING_PROFILES_ACTIVE:-postgres}
    PORT: \\\${PORT:-8080}
    SPRING_DATASOURCE_URL: jdbc:postgresql://db:5432/hemoflow
    SPRING_DATASOURCE_USERNAME: hemoflow
    SPRING_DATASOURCE_PASSWORD: hemoflow_local_dev
  networks:
    - hemoflow-net
  healthcheck:
    test: ["CMD-SHELL", "curl -fsS http://localhost:\\\$\\\${PORT:-8080}/actuator/health || exit 1"]
    interval: 15s
    timeout: 5s
    retries: 5
    start_period: 40s`,
      benefit: 'A dependência com condition service_healthy elimina race conditions: o Spring Boot só inicia após o PostgreSQL estar 100% pronto para aceitar conexões TCP.'
    },
    db: {
      title: 'hemoflow-db',
      type: 'Container de Banco de Dados (PostgreSQL 16)',
      role: 'Instância isolada de banco de dados relacional (postgres:16.4-alpine). Persiste entidades de bolsas de sangue, requisições hospitalares, nós da rede de saúde (HEMOPE, HR, IMIP, HUOC) e histórico de auditoria.',
      code: `db:
  image: postgres:16.4-alpine
  container_name: hemoflow-db
  restart: unless-stopped
  environment:
    POSTGRES_DB: \\\${POSTGRES_DB:-hemoflow}
    POSTGRES_USER: \\\${POSTGRES_USER:-hemoflow}
    POSTGRES_PASSWORD: \\\${POSTGRES_PASSWORD:-hemoflow_local_dev}
  volumes:
    - hemoflow_pgdata:/var/lib/postgresql/data
  networks:
    - hemoflow-net
  healthcheck:
    test: ["CMD-SHELL", "pg_isready -U \\\$\\\${POSTGRES_USER} -d \\\$\\\${POSTGRES_DB}"]
    interval: 10s
    timeout: 5s
    retries: 5
    start_period: 10s`,
      benefit: 'Porta 5432 isolada na rede hemoflow-net (não exposta no host). Protege o banco contra acessos externos e impede conflitos de porta na máquina de desenvolvimento.'
    },
    host: {
      title: 'Host Machine Port Mapping',
      type: 'Encaminhamento de Rede (Port Forwarding)',
      role: 'Mapeia a porta configurável :8080 do host diretamente para a porta :8080 do container hemoflow-api, permitindo que navegadores, Swagger e chamadas curl consumam a API.',
      code: `ports:
  - "\\\${PORT:-8080}:\\\${PORT:-8080}"

# Endpoints acessíveis no Host:
# - Painel: http://localhost:8080/
# - Healthcheck: http://localhost:8080/actuator/health
# - Info da Versão: http://localhost:8080/actuator/info
# - Topologia da Malha: http://localhost:8080/api/v1/topologia
# - Caminho Mínimo: http://localhost:8080/api/v1/rotas/caminho-minimo`,
      benefit: 'Acesso transparente sem necessidade de proxy reverso no ambiente de desenvolvimento local, mantendo a interoperabilidade com o frontend.'
    },
    volume: {
      title: 'hemoflow_pgdata',
      type: 'Volume Nomeado (Armazenamento Persistente)',
      role: 'Ponto de montagem persistente mapeado para /var/lib/postgresql/data dentro do container PostgreSQL. Garante a integridade dos dados durante reinicializações da stack.',
      code: `volumes:
  hemoflow_pgdata:
    name: hemoflow_pgdata

# Evidência de Persistência (docs/evidencias/e1/persistencia-volume.txt):
# - docker compose down: volume hemoflow_pgdata exists = True
# - docker compose up -d: API status = healthy
# - docker compose down -v: volume hemoflow_pgdata exists = False`,
      benefit: 'Os dados persistem após docker compose down normal, e só são resetados propositalmente quando o desenvolvedor executa docker compose down -v.'
    }
  };

  archNodes.forEach(node => {
    node.addEventListener('click', () => {
      archNodes.forEach(n => n.classList.remove('selected'));
      node.classList.add('selected');

      const type = node.dataset.type;
      const data = archDetails[type];
      if (data) {
        inspectorTitle.textContent = data.title;
        inspectorType.textContent = data.type;
        inspectorRole.textContent = data.role;
        inspectorCode.textContent = data.code;
        inspectorBenefit.textContent = data.benefit;
      }
    });
  });

  /* ==========================================================================
     6. Slide 4: Dockerfile Multi-Stage Explorer
     ========================================================================== */
  const btnStage1 = document.getElementById('btnStage1');
  const btnStage2 = document.getElementById('btnStage2');
  const dockerfileLinesContainer = document.getElementById('dockerfileLinesContainer');
  const dockerfileExplainer = document.getElementById('dockerfileExplainer');

  const stage1Lines = [
    { num: 1, text: '<span class="kw-docker">FROM</span> maven:3.9.9-eclipse-temurin-17 <span class="kw-docker">AS</span> build', step: 'build-base' },
    { num: 2, text: '<span class="kw-docker">WORKDIR</span> /app', step: 'workdir-1' },
    { num: 3, text: '', step: null },
    { num: 4, text: '<span class="kw-docker">COPY</span> pom.xml mvnw ./', step: 'cache-dep' },
    { num: 5, text: '<span class="kw-docker">COPY</span> .mvn .mvn', step: 'cache-dep' },
    { num: 6, text: '<span class="kw-docker">RUN</span> chmod +x mvnw && ./mvnw -B dependency:go-offline', step: 'cache-dep', highlight: true },
    { num: 7, text: '', step: null },
    { num: 8, text: '<span class="kw-docker">COPY</span> src src', step: 'copy-src' },
    { num: 9, text: '<span class="kw-docker">RUN</span> ./mvnw -B verify', step: 'verify-tests', highlight: true }
  ];

  const stage2Lines = [
    { num: 11, text: '<span class="kw-docker">FROM</span> eclipse-temurin:17-jre-jammy <span class="kw-docker">AS</span> runtime', step: 'runtime-base', highlight: true },
    { num: 12, text: '<span class="kw-docker">WORKDIR</span> /app', step: 'workdir-2' },
    { num: 13, text: '', step: null },
    { num: 14, text: '<span class="kw-docker">ARG</span> GIT_SHA=local-dev', step: 'args-oci' },
    { num: 15, text: '<span class="kw-docker">ARG</span> APP_VERSION=0.0.1-SNAPSHOT', step: 'args-oci' },
    { num: 16, text: '', step: null },
    { num: 17, text: '<span class="kw-docker">LABEL</span> org.opencontainers.image.title=<span class="str-docker">"HemoFlow API"</span> \\', step: 'args-oci' },
    { num: 18, text: '      org.opencontainers.image.version=<span class="str-docker">"${APP_VERSION}"</span> \\', step: 'args-oci' },
    { num: 19, text: '      org.opencontainers.image.revision=<span class="str-docker">"${GIT_SHA}"</span>', step: 'args-oci' },
    { num: 20, text: '', step: null },
    { num: 26, text: '<span class="kw-docker">RUN</span> groupadd --system --gid 1001 hemoflow && \\', step: 'non-root', highlight: true },
    { num: 27, text: '    useradd --system --uid 1001 --gid hemoflow --shell /sbin/nologin hemoflow', step: 'non-root', highlight: true },
    { num: 28, text: '', step: null },
    { num: 29, text: '<span class="kw-docker">COPY</span> --from=build --chown=hemoflow:hemoflow /app/target/*.jar /app/app.jar', step: 'copy-jar', highlight: true },
    { num: 30, text: '', step: null },
    { num: 31, text: '<span class="kw-docker">USER</span> hemoflow', step: 'user-hemoflow', highlight: true },
    { num: 32, text: '<span class="kw-docker">EXPOSE</span> 8080', step: 'expose' },
    { num: 33, text: '', step: null },
    { num: 34, text: '<span class="kw-docker">HEALTHCHECK</span> --interval=15s --timeout=5s --start-period=40s --retries=5 \\', step: 'healthcheck', highlight: true },
    { num: 35, text: '  <span class="kw-docker">CMD</span> curl -fsS http://localhost:${PORT}/actuator/health || exit 1', step: 'healthcheck', highlight: true },
    { num: 36, text: '', step: null },
    { num: 37, text: '<span class="kw-docker">ENTRYPOINT</span> [<span class="str-docker">"java"</span>, <span class="str-docker">"-jar"</span>, <span class="str-docker">"/app/app.jar"</span>]', step: 'entrypoint' }
  ];

  function renderDockerfileStage(lines) {
    dockerfileLinesContainer.innerHTML = '';
    lines.forEach(item => {
      const lineDiv = document.createElement('div');
      lineDiv.className = `code-line ${item.highlight ? 'highlight' : ''}`;
      lineDiv.innerHTML = `
        <span class="line-num">${item.num || ''}</span>
        <span class="line-text">${item.text}</span>
      `;
      dockerfileLinesContainer.appendChild(lineDiv);
    });
  }

  btnStage1.addEventListener('click', () => {
    btnStage1.classList.add('active');
    btnStage2.classList.remove('active');
    renderDockerfileStage(stage1Lines);
    dockerfileExplainer.innerHTML = `
      <div class="step-card active">
        <div class="step-title">
          <span class="badge-tag success">Estágio 1 • Linhas 4-6</span>
          Cache Otimizado de Dependências Maven
        </div>
        <div class="step-desc">
          O arquivo <code>pom.xml</code> e a pasta <code>.mvn</code> são copiados antes do código-fonte. O comando <code>./mvnw -B dependency:go-offline</code> baixa todas as dependências em uma camada isolada de cache.
        </div>
        <div class="step-why">
          <strong>Por que no HemoFlow:</strong> Alterações no código em <code>src/</code> não forçam o re-download de ~200MB de bibliotecas Maven a cada novo build.
        </div>
      </div>

      <div class="step-card">
        <div class="step-title">
          <span class="badge-tag success">Estágio 1 • Linha 9</span>
          Gate de Testes no Próprio Build
        </div>
        <div class="step-desc">
          O comando <code>./mvnw -B verify</code> compila e executa toda a suíte de testes (39 testes unitários, integração e cenários BDD Cucumber). Se qualquer teste falhar, a imagem sequer é gerada.
        </div>
        <div class="step-why">
          <strong>Por que no HemoFlow:</strong> Garante que nenhuma imagem com regras de compatibilidade ABO/Rh ou FEFO quebradas seja empacotada.
        </div>
      </div>
    `;
  });

  btnStage2.addEventListener('click', () => {
    btnStage2.classList.add('active');
    btnStage1.classList.remove('active');
    renderDockerfileStage(stage2Lines);
    dockerfileExplainer.innerHTML = `
      <div class="step-card active">
        <div class="step-title">
          <span class="badge-tag success">Estágio 2 • Linha 11</span>
          Runtime JRE Minimalista (eclipse-temurin:17-jre-jammy)
        </div>
        <div class="step-desc">
          Descarta o SDK pesado do Maven e mantém apenas o Java Runtime Environment (JRE). Reduz a imagem final para ~280MB e diminui drasticamente a superfície de vulnerabilidades (CVEs).
        </div>
        <div class="step-why">
          <strong>Por que no HemoFlow:</strong> Apenas os arquivos compilados do JAR são necessários para execução em produção.
        </div>
      </div>

      <div class="step-card">
        <div class="step-title">
          <span class="badge-tag success">Estágio 2 • Linhas 26-31</span>
          Segurança: Princípio do Menor Privilégio
        </div>
        <div class="step-desc">
          Criação de usuário dedicado de sistema <code>hemoflow</code> (UID 1001, GID 1001) com shell restrita <code>/sbin/nologin</code>. O artefato compilado é copiado com <code>--chown=hemoflow:hemoflow</code> e executado sob <code>USER hemoflow</code>.
        </div>
        <div class="step-why">
          <strong>Por que no HemoFlow:</strong> Elimina o risco de execução como <code>root</code>, protegendo o sistema host contra eventuais brechas de segurança no container.
        </div>
      </div>

      <div class="step-card">
        <div class="step-title">
          <span class="badge-tag success">Estágio 2 • Linhas 34-35</span>
          HEALTHCHECK Nativo do Container
        </div>
        <div class="step-desc">
          Instrução <code>HEALTHCHECK</code> com curl para <code>/actuator/health</code>. O Docker monitora a saúde interna do processo e reporta se a API está online e pronta para tráfego.
        </div>
        <div class="step-why">
          <strong>Por que no HemoFlow:</strong> Permite que orquestradores e o Compose identifiquem instâncias não-saudáveis e realizem auto-recuperação.
        </div>
      </div>
    `;
  });

  /* ==========================================================================
     7. Slide 6: Flagship Interactive Simulated Demo Engine
     ========================================================================== */
  const btnPlayPause = document.getElementById('btnPlayPause');
  const playIcon = document.getElementById('playIcon');
  const pauseIcon = document.getElementById('pauseIcon');
  const btnStepNext = document.getElementById('btnStepNext');
  const btnStepBack = document.getElementById('btnStepBack');
  const btnRestart = document.getElementById('btnRestart');
  const scrubberFill = document.getElementById('scrubberFill');
  const timeReadout = document.getElementById('timeReadout');
  const speedButtons = document.querySelectorAll('.btn-speed');

  // Cockpit Elements
  const panelCodeFile = document.getElementById('panelCodeFile');
  const panelCodeContent = document.getElementById('panelCodeContent');
  const panelTermContent = document.getElementById('panelTermContent');
  const simBoxDb = document.getElementById('simBoxDb');
  const simStateDb = document.getElementById('simStateDb');
  const simBoxApi = document.getElementById('simBoxApi');
  const simStateApi = document.getElementById('simStateApi');
  const simLink = document.getElementById('simLink');

  // Telemetry Badges
  const telBuild = document.getElementById('telBuild');
  const telDb = document.getElementById('telDb');
  const telApi = document.getElementById('telApi');
  const telNet = document.getElementById('telNet');
  const telPort = document.getElementById('telPort');
  const telVol = document.getElementById('telVol');

  let simStep = 0;
  let isSimPlaying = false;
  let simTimer = null;
  let simSpeed = 1.0;

  const simStepsData = [
    {
      time: '00:00 / 00:30',
      progress: 0,
      codeFile: 'compose.yaml',
      code: `# Etapa 1: Validando a configuração do ambiente
services:
  db:
    image: postgres:16.4-alpine
    container_name: hemoflow-db
  api:
    build: .
    depends_on:
      db:
        condition: service_healthy`,
      term: `<div class="term-line cmd">$ docker compose config -q</div>
<div class="term-line success">✔ Validação do compose.yaml aprovada (Exit code 0)</div>
<div class="term-line info">[+] Inicializando orquestração da stack HemoFlow...</div>
<div class="term-line dim">Aguardando comando de subida... <span class="cursor-blink"></span></div>`,
      dbState: { class: 'state-idle', text: 'IDLE', boxClass: '' },
      apiState: { class: 'state-idle', text: 'IDLE', boxClass: '' },
      linkActive: false,
      tel: { build: 'Validado', db: 'Inativo', api: 'Inativo', net: 'Pendente', port: 'Fechada', vol: 'Desconectado' }
    },
    {
      time: '00:06 / 00:30',
      progress: 20,
      codeFile: 'Dockerfile (Stage 1)',
      code: `# Etapa 2: Construindo imagem multi-stage
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml mvnw ./
COPY .mvn .mvn
RUN ./mvnw -B dependency:go-offline
COPY src src
RUN ./mvnw -B verify`,
      term: `<div class="term-line cmd">$ docker compose up --build</div>
<div class="term-line info">[+] Building 8.4s (14/14) FINISHED</div>
<div class="term-line dim"> => [internal] load build definition from Dockerfile</div>
<div class="term-line dim"> => [build 4/6] RUN ./mvnw -B dependency:go-offline (cache hit)</div>
<div class="term-line dim"> => [build 6/6] RUN ./mvnw -B verify</div>
<div class="term-line success">Tests run: 39, Failures: 0, Errors: 0, Skipped: 0</div>
<div class="term-line success">4 Scenarios (4 passed) | 15 Steps (15 passed)</div>
<div class="term-line success">✔ Imagem hemoflow:local-compose gerada com sucesso!</div>`,
      dbState: { class: 'state-idle', text: 'IDLE', boxClass: '' },
      apiState: { class: 'state-building', text: 'BUILDING', boxClass: 'building' },
      linkActive: false,
      tel: { build: 'Multi-Stage OK', db: 'Inativo', api: 'Compilando', net: 'Pendente', port: 'Fechada', vol: 'Desconectado' }
    },
    {
      time: '00:12 / 00:30',
      progress: 40,
      codeFile: 'compose.yaml (Infra)',
      code: `# Etapa 3: Criando rede isolada e volume de persistência
networks:
  hemoflow-net:
    driver: bridge
volumes:
  hemoflow_pgdata:
    name: hemoflow_pgdata`,
      term: `<div class="term-line info">[+] Running 2/2</div>
<div class="term-line success"> ✔ Network hemoflow-net      Created</div>
<div class="term-line success"> ✔ Volume hemoflow_pgdata    Created</div>
<div class="term-line info">Iniciando dependência hemoflow-db em hemoflow-net...</div>`,
      dbState: { class: 'state-starting', text: 'STARTING', boxClass: 'starting' },
      apiState: { class: 'state-idle', text: 'WAITING DB', boxClass: '' },
      linkActive: false,
      tel: { build: 'Concluído', db: 'Iniciando', api: 'Em Espera', net: 'Ativa (Bridge)', port: 'Fechada', vol: 'Montado' }
    },
    {
      time: '00:18 / 00:30',
      progress: 60,
      codeFile: 'compose.yaml (Database Health)',
      code: `# Etapa 4: PostgreSQL executando healthcheck pg_isready
db:
  image: postgres:16.4-alpine
  healthcheck:
    test: ["CMD-SHELL", "pg_isready -U \\\${POSTGRES_USER} -d \\\${POSTGRES_DB}"]
    interval: 10s`,
      term: `<div class="term-line info">[+] Container hemoflow-db  Created</div>
<div class="term-line info">[+] Container hemoflow-db  Starting</div>
<div class="term-line dim">PostgreSQL Database directory appears to contain a database; Skipping initialization</div>
<div class="term-line info">2026-10-20 19:40:02 UTC [1] LOG:  database system is ready to accept connections</div>
<div class="term-line dim">Executando teste: pg_isready -U hemoflow -d hemoflow...</div>
<div class="term-line success">/var/run/postgresql:5432 - accepting connections</div>
<div class="term-line success">✔ Container hemoflow-db status: HEALTHY</div>`,
      dbState: { class: 'state-healthy', text: 'HEALTHY', boxClass: 'running' },
      apiState: { class: 'state-starting', text: 'STARTING', boxClass: 'starting' },
      linkActive: true,
      tel: { build: 'Concluído', db: 'Healthy (5432)', api: 'Iniciando', net: 'Ativa (Bridge)', port: 'Fechada', vol: 'Montado' }
    },
    {
      time: '00:24 / 00:30',
      progress: 80,
      codeFile: 'compose.yaml (API Startup)',
      code: `# Etapa 5: Spring Boot conectando no PostgreSQL via DNS 'db'
api:
  depends_on:
    db:
      condition: service_healthy
  environment:
    SPRING_DATASOURCE_URL: jdbc:postgresql://db:5432/hemoflow`,
      term: `<div class="term-line info">[+] Container hemoflow-api  Created</div>
<div class="term-line info">[+] Container hemoflow-api  Starting</div>
<div class="term-line dim">  .   ____          _            __ _ _</div>
<div class="term-line dim"> /\\\\ / ___'_ __ _ _(_)_ __  __ _ \\ \\ \\ \\</div>
<div class="term-line dim">( ( )\\___ | '_ | '_| | '_ \\/ _\` | \\ \\ \\ \\</div>
<div class="term-line info">Starting HemoFlowApplication with Java 17.0.12 (UID 1001)...</div>
<div class="term-line info">HikariPool-1 - Starting... Connected to jdbc:postgresql://db:5432/hemoflow</div>
<div class="term-line info">Tomcat started on port 8080 (http) with context path ''</div>
<div class="term-line success">Started HemoFlowApplication in 4.128 seconds (process running for 4.89)</div>`,
      dbState: { class: 'state-healthy', text: 'HEALTHY', boxClass: 'running' },
      apiState: { class: 'state-starting', text: 'BOOTING', boxClass: 'starting' },
      linkActive: true,
      tel: { build: 'Concluído', db: 'Healthy', api: 'Booting', net: 'Ativa (Bridge)', port: '8080 Aberta', vol: 'Montado' }
    },
    {
      time: '00:30 / 00:30',
      progress: 100,
      codeFile: 'Evidência Real (Smoke Test)',
      code: `# Etapa 6: Validação de Smoke Test e Prontidão
curl -fsS http://localhost:8080/actuator/health
# {"status":"UP"}

curl -fsS http://localhost:8080/api/v1/topologia
# Topologia da Hemorrede com 12 nós (HEMOPE, HR, IMIP...)`,
      term: `<div class="term-line cmd">$ docker compose ps</div>
<div class="term-line info">NAME           SERVICE   STATUS                    PORTS</div>
<div class="term-line success">hemoflow-api   api       Up 30 seconds (healthy)   0.0.0.0:8080->8080/tcp</div>
<div class="term-line success">hemoflow-db    db        Up 36 seconds (healthy)   5432/tcp</div>
<div class="term-line cmd">$ curl -fsS http://localhost:8080/actuator/health</div>
<div class="term-line success">{"status":"UP"}</div>
<div class="term-line success">✔ Stack HemoFlow em execução com integridade comprovada! <span class="cursor-blink"></span></div>`,
      dbState: { class: 'state-healthy', text: 'HEALTHY', boxClass: 'running' },
      apiState: { class: 'state-healthy', text: 'HEALTHY', boxClass: 'running' },
      linkActive: true,
      tel: { build: 'Aprovado (39/39)', db: 'Healthy (Up 36s)', api: 'Healthy (Up 30s)', net: 'Ativa', port: ':8080 Publicada', vol: 'Preservado' }
    }
  ];

  function renderSimStep(stepIdx) {
    simStep = Math.max(0, Math.min(stepIdx, simStepsData.length - 1));
    const step = simStepsData[simStep];

    // Update Scrubber
    scrubberFill.style.width = `${step.progress}%`;
    timeReadout.textContent = step.time;

    // Update Code & Terminal Panels
    panelCodeFile.textContent = step.codeFile;
    panelCodeContent.textContent = step.code;
    panelTermContent.innerHTML = step.term;
    panelTermContent.scrollTop = panelTermContent.scrollHeight;

    // Update Architecture Simulation
    simBoxDb.className = `sim-container-box ${step.dbState.boxClass}`;
    simStateDb.className = `sim-box-state ${step.dbState.class}`;
    simStateDb.textContent = step.dbState.text;

    simBoxApi.className = `sim-container-box ${step.apiState.boxClass}`;
    simStateApi.className = `sim-box-state ${step.apiState.class}`;
    simStateApi.textContent = step.apiState.text;

    if (step.linkActive) {
      simLink.classList.add('active');
    } else {
      simLink.classList.remove('active');
    }

    // Update Telemetry Badges
    telBuild.textContent = step.tel.build;
    telDb.textContent = step.tel.db;
    telApi.textContent = step.tel.api;
    telNet.textContent = step.tel.net;
    telPort.textContent = step.tel.port;
    telVol.textContent = step.tel.vol;

    telDb.className = `telemetry-value ${step.tel.db.includes('Healthy') ? 'ok' : ''}`;
    telApi.className = `telemetry-value ${step.tel.api.includes('Healthy') ? 'ok' : ''}`;

    // Highlight Milestones
    const markers = document.querySelectorAll('.milestone-marker');
    markers.forEach((m, idx) => {
      if (idx <= simStep) {
        m.classList.add('passed');
      } else {
        m.classList.remove('passed');
      }
    });
  }

  function playSimulation() {
    isSimPlaying = true;
    playIcon.style.display = 'none';
    pauseIcon.style.display = 'block';

    if (simStep >= simStepsData.length - 1) {
      simStep = 0;
    }

    clearInterval(simTimer);
    simTimer = setInterval(() => {
      if (simStep < simStepsData.length - 1) {
        renderSimStep(simStep + 1);
      } else {
        pauseSimulation();
      }
    }, 4000 / simSpeed);
  }

  function pauseSimulation() {
    isSimPlaying = false;
    playIcon.style.display = 'block';
    pauseIcon.style.display = 'none';
    clearInterval(simTimer);
  }

  btnPlayPause.addEventListener('click', () => {
    if (isSimPlaying) {
      pauseSimulation();
    } else {
      playSimulation();
    }
  });

  btnStepNext.addEventListener('click', () => {
    pauseSimulation();
    if (simStep < simStepsData.length - 1) {
      renderSimStep(simStep + 1);
    }
  });

  btnStepBack.addEventListener('click', () => {
    pauseSimulation();
    if (simStep > 0) {
      renderSimStep(simStep - 1);
    }
  });

  btnRestart.addEventListener('click', () => {
    pauseSimulation();
    renderSimStep(0);
  });

  // Speed Buttons
  speedButtons.forEach(btn => {
    btn.addEventListener('click', () => {
      speedButtons.forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      simSpeed = parseFloat(btn.dataset.speed);
      if (isSimPlaying) {
        playSimulation();
      }
    });
  });

  // Scrubber Track Click
  const scrubberTrack = document.getElementById('scrubberTrack');
  scrubberTrack.addEventListener('click', (e) => {
    const rect = scrubberTrack.getBoundingClientRect();
    const clickX = e.clientX - rect.left;
    const ratio = Math.max(0, Math.min(1, clickX / rect.width));
    const targetStep = Math.round(ratio * (simStepsData.length - 1));
    pauseSimulation();
    renderSimStep(targetStep);
  });

  /* ==========================================================================
     8. Slide 7: Clipboard Copy Feedback
     ========================================================================== */
  const copyButtons = document.querySelectorAll('.btn-copy');

  copyButtons.forEach(btn => {
    btn.addEventListener('click', () => {
      const cmd = btn.dataset.cmd;
      if (cmd) {
        navigator.clipboard.writeText(cmd).then(() => {
          showToast(`Copiado: "${cmd}"`);
        }).catch(() => {
          // Fallback
          showToast(`Comando copiado!`);
        });
      }
    });
  });

  function showToast(text) {
    toastMsg.textContent = text;
    toastMsg.classList.add('show');
    setTimeout(() => {
      toastMsg.classList.remove('show');
    }, 2200);
  }

  /* ==========================================================================
     9. Slide 9: Closing Slide Actions
     ========================================================================== */
  const btnRestartPitch = document.getElementById('btnRestartPitch');
  const btnOpenDocs = document.getElementById('btnOpenDocs');

  if (btnRestartPitch) {
    btnRestartPitch.addEventListener('click', () => {
      updateSlide(0);
    });
  }

  if (btnOpenDocs) {
    btnOpenDocs.addEventListener('click', () => {
      // In standalone environment, jump to slide 3 or prompt user
      updateSlide(2);
      showToast('Visualizando Arquitetura e Evidências');
    });
  }

  /* ==========================================================================
     10. Kickoff
     ========================================================================== */
  initFromHash();
  renderSimStep(0);
});
