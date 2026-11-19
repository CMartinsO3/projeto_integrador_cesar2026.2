# Plano de refatoração front-end

## Problemas concretos

- `index.html:18-30` e `index.html:208-229`: marcadores de conflito Git quebram o HTML.
- `hemoflow.css:1-606` aproximadamente: CSS monolítico com tokens, componentes e regras de página misturados.
- `estatisticas.html:97-194`: CSS inline no head e lógica JS integralmente inline.
- Todos os templates possuem scripts inline ou handlers `onclick`/`onchange`.
- `monitoramento.html`: polling refaz o painel completo e não diferencia anomalias novas.
- `motorista.html`: polling substitui cards inteiros a cada 15 segundos.
- `painel-bolsas.html`, `dashboard-admin.html`, `dashboard-usuario.html`, `mapa-doacao.html` e `estatisticas.html`: `innerHTML` com dados dinâmicos.
- Autenticação dividida em três estruturas; áreas pública, operacional e administrativa não possuem fragmentos consistentes.
- Navegação, sidebar e footer são duplicados em cada página.
- Estados vazios, loading, erro e timeout não são uniformes.
- Não há `data-testid`; a cobertura atual não depende de classes.
- Não há `:focus-visible` explícito, nem foco controlado em diálogos/modais além dos usos nativos.
- Tabelas operacionais podem exigir rolagem horizontal ou cards em telas menores.

## Decisões

- Mantemos Spring Boot, Thymeleaf, APIs e Leaflet já existentes.
- Não adicionaremos npm, bundler, CDN novo, Layout Dialect ou dependências.
- Será mantido o cookie JSESSIONID e `fetch(credentials: "same-origin")`; não haverá criação de CSRF/token.
- O CSS será dividido em `tokens.css`, `base.css`, `components.css` e `pages.css`.
- Os fragmentos Thymeleaf serão extraídos sem alterar nenhum `@GetMapping`.
- `api-client.js` normalizará os formatos `ErroResposta` e `{erro}` já existentes.
- `ui.js` fornecerá toast, confirmação em `<dialog>`, busy state, skeleton e formatadores.
- Dados de API serão renderizados com `textContent` e nós DOM.
- Limites de vencimento, estoque, temperatura e urgência só serão usados quando fornecidos pela API/docs. Nenhuma compatibilidade ou limiar não documentado será inventado.
- Constantes front-end não clínicas ficarão nomeadas; os limites didáticos reais deverão continuar chegando do back-end.
- Toda funcionalidade atual permanecerá acessível.
- Não removeremos textos preservados por testes.

## Ordem das etapas

1. **Fundação:** resolver conflito de `index.html`, CSS dividido, fragmentos, `api-client.js`, `ui.js`, estado global de feedback e documentação de testes.
2. **Autenticação:** unificar login de doador, operacional e admin; cadastro operacional; navegação por perfil.
3. **Área pública/doador:** refatorar cadastro, dashboard, mapa, campanhas e agendamento sem alterar endpoints.
4. **Estoque/requisições:** FEFO/validade/filtros, badges, estados vazios e erros reais de regra de negócio.
5. **Monitoramento:** atualização incremental, polling controlado, backoff e anomalias assertivas.
6. **Logística:** rotas legíveis e motorista mobile-first com toques de 44px.
7. **Estatísticas/admin:** hierarquia KPI, visualizações, detalhamento e tratamento completo de estados.
8. **Acabamento:** a11y, `data-testid`, remoção de scripts/handlers/CSS legado, varredura estática e testabilidade.

## Validação por etapa

- `./mvnw.cmd test` ou `.\mvnw.cmd test`.
- Verificações estáticas de assets, fragmentos e scripts.
- Commit separado ao final de cada etapa.
- Atualização de `docs/frontend-refactor/progresso.md`.
