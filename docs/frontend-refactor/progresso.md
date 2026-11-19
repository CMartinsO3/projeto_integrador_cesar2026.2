# Progresso da refatoração

## Etapa 1 — Fundação

- Criado branch `refactor/frontend-ux`.
- Criados os arquivos CSS `tokens.css`, `base.css`, `components.css` e `pages.css`.
- Criados fragmentos Thymeleaf de head, navbar, sidebar, footer e feedback.
- Criados `api-client.js` e `ui.js` com formatos de erro, sessão, timeout, busy state, toasts e confirmação nativa.
- Corrigidos os marcadores de conflito em `index.html`.
- Pendente: aplicar os fragmentos e módulos nas 16 páginas.

## Etapa 2 — Autenticação

- Reescritas as quatro telas de autenticação/cadastro operacional.
- Adicionado `auth.js` para login de doador, hospital, motorista e administrador.
- Os formulários preservam os endpoints e redirects atuais e usam `JSESSIONID` via `same-origin`.

## Etapa 3 — Área pública/doador

- Reescritas cadastro, dashboard e mapa do doador.
- Criados `doador.js` e `mapa.js`, sem scripts inline ou dados dinâmicos em `innerHTML`.
- Campanhas, hospitais, agendamentos e histórico continuam consumindo os endpoints existentes.

## Etapa 4 — Estoque e requisições

- Criadas jornadas externas `estoque.js` e `requisicoes.js`.
- Incluídos filtros rápidos por status/tipo sanguíneo, tabelas por id e feedback com mensagens reais da API.
- Preservados FEFO, isolamentos hospitalares e transições fornecidas pelo back-end.

## Etapas 5 e 6 — Monitoramento e logística

- Criados `monitoramento.js`, `motorista.js` e `rotas.js`.
- Monitoramento usa polling sem sobreposição, pausa em aba ocultada e backoff em erro.
- Motorista usa atualização incremental por entrega e alvos móveis adequados ao toque.

## Etapas 7 e 8 — Estatísticas, administração e acabamento

- Criados `admin.js` e `estatisticas.js`.
- Painéis administrativos passaram a usar KPIs, tabelas por id e estados vazios.
- Scripts inline foram removidos das páginas migradas; resta completar a varredura estática e os testids consolidados.

## Acabamento

- Removidos `hemoflow.css` e `hemoflow.js` legados; os quatro CSS novos são carregados pelas páginas.
- Foram removidos handlers inline, scripts inline extensos, `location.reload()` e conflitos Git encontrados na varredura.
- Criado inventário de `data-testid` em `docs/frontend-refactor/data-testids.md`.

## Resultado final

- Testes Maven verdes em todas as execuções de etapa usando `-Dnet.bytebuddy.experimental=true` na JVM Java 26 deste ambiente.
- `node --check` aprovado para todos os módulos JavaScript.
- Varredura estática sem marcadores de conflito, handlers inline, scripts inline extensos, `innerHTML`, `outerHTML` ou `location.reload()`.
- Runtime parcial em servidor já ativo na porta 8080: rotas públicas e assets CSS/JS consultados retornaram 200.

## Continuidade aplicada

- Restaurado o estoque por hospital no mapa.
- Heartbeat do monitoramento pausado em aba oculta.
- Restaurada a tabela completa de bolsas e seus contadores no dashboard administrativo.
- Página inicial passou a utilizar o fragmento compartilhado de head.

## Correções de execução validadas no navegador

- Corrigida a inicialização do módulo de mapa e o escopo da ação de estoque.
- Adicionado fallback funcional de hospitais caso Leaflet não esteja disponível.
- Corrigido o agendamento para avançar automaticamente da data sem horários para o próximo dia útil.
- Validado ponta a ponta: login do doador, abertura do agendamento, escolha de horário e confirmação.
- Validado o painel administrativo e a confirmação de baixas.
- Validado o botão de alerta térmico no monitoramento; as três bolsas em trânsito passaram a destacar anomalia.

## Ajuste visual da navegação pública

- “Área do doador” agora usa fundo teal sólido, texto branco, ícone próprio e estado visível sem hover.
- Símbolo da marca ampliado para 44 px.
- Ícones de equipe clínica, onde doar e menu ampliados para 20 px em SVG.

## Correção responsiva da página inicial

- Em viewport mobile de 390 px, removido o overflow horizontal da área de estoque.
- Os cards de hemocomponente e tipo sanguíneo mantêm rolagem interna controlada.
- Hero e painel da rede usam uma única coluna em telas menores.
