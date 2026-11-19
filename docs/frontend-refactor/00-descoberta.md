# Descoberta do front-end

## Estado do repositório

- Stack atual: Spring Boot 3.3.0, Java 17, Thymeleaf, HTML/CSS/JavaScript puro e H2/PostgreSQL.
- Não há Spring Security no projeto; a autenticação é feita por atributos da `HttpSession` (`role`, ids e nomes).
- Não há configuração de CSRF encontrada no front ou no back-end atual. O navegador envia automaticamente o cookie JSESSIONID com `credentials: same-origin`; o `fetch` atual já usa a sessão por cookie. O cliente futuro deve conservar esse mecanismo e não criar token ou header próprio.
- O interceptor valida origem para APIs mutáveis por `Origin`/`Referer` e `Host`, além dos perfis da sessão.
- A página inicial contém marcadores reais de conflito em `index.html`, nas linhas 18-30 e 208-229.
- O `HemoflowApplicationTests` preserva texto mínimo da página de rotas (`Consultar rota`, `/api/v1/topologia`, `/api/v1/rotas/vizinho-mais-proximo`).
- Os testes operacionais dependem de 401/403, redirects, JSONs e atributos de sessão; não devem haver alteração de contrato ou autorização.

## Inventário por template

| Template | Rota | Perfil | Endpoints principais | Scripts/duplicações | Acessibilidade encontrada |
|---|---|---|---|---|---|
| `index.html` | `/` | Público | Estatísticas e links técnicos | Dois blocos conflitantes; usa JS legado | Marca `aria-label`/skip parcial; conflitos HTML |
| `doe-agora.html` | `/doe-agora` | Público/doador | POST `/api/v1/doadores` | Script inline de cadastro | Campos com label; feedback sem `aria-live` |
| `mapa-doacao.html` | `/mapa` | Público/doador | Sessão, doadores, hospitais, bolsas, campanhas e agendamentos | Scripts inline extensos; Leaflet já existente | Modal nativo com labels; muitos textos dinâmicos via innerHTML |
| `dashboard-usuario.html` | `/dashboard-usuario` | Doador | Sessão, doador, doações, agendamentos, logout | Script inline; três tabelas carregadas separadamente | Sidebar repetida; logout por onclick; estado vazio básico |
| `login-usuario.html` | `/login-usuario` | Doador | POST login-doador, logout implícito | Script inline; Enter global | Campos sem form semântico em parte da tela |
| `login-operacional.html` | `/login-hospital`, `/login-motorista` | Hospital/Motorista | POST login-operacional | Script inline; perfil inferido da URL | Form principal usa labels; erro com role=alert |
| `login-admin.html` | `/login-admin` | Admin | POST login-admin | Script inline; Enter global | Campos, mas sem form semântico |
| `cadastro-operacional.html` | `/cadastro-operacional` | Público | Hospitais e cadastro operacional | Script inline extenso | Labels presentes; feedback com aria-live |
| `dashboard-admin.html` | `/dashboard-admin` | Admin | Sessão, bolsas, nós, entrada/saída, alertas | Script inline extenso; onclick/onchange | Labels incompletos em controles de registro |
| `contas-operacionais.html` | `/contas-operacionais` | Admin | Usuários, hospitais, entregas, aprovação e atribuição | Script inline; múltiplos onclick | Tabela sem wrapper no formulário de criação; feedback aria-live parcial |
| `painel-bolsas.html` | `/bolsas-painel` | Admin | Nós, bolsas, entrada e saída | Script inline; innerHTML; onclick | Form usa labels pouco associados em alguns campos |
| `requisicoes-hospital.html` | `/requisicoes-hospital` | Hospital/Admin | Sessão, hospitais, requisições | Script inline extenso | Labels presentes; status e tabela dinâmicos |
| `monitoramento.html` | `/monitoramento` | Admin | Monitoramento, leituras, heartbeat e status de rede | Script inline; polling completo | Tabelas extensas; atualiza células como render completo |
| `rotas.html` | `/rotas` | Público | Topologia e rota vizinho mais próximo | Script inline; Leaflet já existente | Mapa tem aria-label; form possui labels |
| `motorista.html` | `/motorista` | Motorista/Admin | Sessão, entregas e status | Script inline; polling de 15 s | Cards sem ids estáveis; ações dependem de render completo |
| `estatisticas.html` | `/estatisticas` | Público/Admin | Demanda, descarte, hospitais, tempos, risco, probabilidade e temperatura | `<style>` inline e scripts extenso; innerHTML | Visualizações sem alternativa textual completa em todos os casos |

## Funcionalidades existentes a preservar

- Sessão por cookie JSESSIONID e redirecionamento por perfil.
- Cadastro e login de doador.
- Hospitais, campanhas, mapa, disponibilidade e agendamento.
- Entrada/saída e filtros de bolsas.
- Alertas de vencimento, FEFO como regra já feita pelo back-end e estados de requisição.
- Solicitação/consulta de requisições com isolamento hospitalar.
- Telemetria simulada, heartbeat e status manual de conectividade.
- Roteamento vizinho mais próximo e avanço de entrega.
- Atribuição de motoristas e aprovação de contas hospitalares.
- Painéis estatísticos existentes e API técnica na página inicial.

## Contratos de erro

`ApiExceptionHandler` usa `ErroResposta(timestamp, status, erro, mensagem, detalhes)`.

| Status | Origem | Mensagem padrão |
|---|---|---|
| 400 | `MethodArgumentNotValidException` | `Erro de validação`: mostra itens `campo: mensagem` |
| 401 | Login/recursos protegidos | Autenticação necessária ou resposta própria de autenticação |
| 403 | Perfil sem permissão, origem ou regra explícita | Acesso negado/perfil sem permissão |
| 404 | Recurso não encontrado | Mensagem específica da exceção |
| 422 | `RegraNegocioException` | Mensagem específica da regra de negócio |
| 500 | Exceção inesperada | Mensagem da exceção; UI deve reduzir exposição técnica |

Autenticação administrativa, doador e agendamento também podem retornar objetos simples `{erro: "..."} / {ok, redirect}`. Esses formatos devem ser normalizados pelo `api-client.js` sem alterar a API.

## Acoplamentos com testes

- `HemoflowApplicationTests` exige que `/rotas` contenha literalmente `Consultar rota`, `/api/v1/topologia` e `/api/v1/rotas/vizinho-mais-proximo`.
- `OperationalStoriesIntegrationTest` testa redirects `/login-admin`, `/login-hospital`, `/login-motorista` e `/requisicoes-hospital`, além de respostas, status HTTP e isolamento por sessão.
- Nenhum teste atual exige classes CSS ou ids de templates. Os novos `data-testid` não podem substituir textos exigidos.
- A refatoração não deve alterar DTOs, campos JSON, métodos HTTP, rotas, perfis ou regras do interceptor.

## Conclusões

- O maior problema imediato é a integridade do HTML em `index.html`.
- O CSS atual já contém tokens parciais, mas está concentrado em um único arquivo e não divide base, componentes e páginas.
- Existem scripts inline em todos os templates exceto pela carga estática isolada em alguns lugares.
- O `innerHTML` recebe dados dinâmicos em várias páginas e deve ser substituído por criação de nós DOM.
- Não há fragmentos Thymeleaf reutilizáveis.
- Não há dependência de token/CSRF atual; preservaremos o cookie de sessão.
