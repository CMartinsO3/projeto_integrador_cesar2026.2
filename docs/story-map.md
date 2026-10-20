# Story Map — HemoFlow (Unidade 2: Container, TDD e Deploy)

Referências do projeto: [`docs/historia-usuarios.md`](./historia-usuarios.md), [`docs/contrato-api.md`](./contrato-api.md) e [`docs/escopo-grafo.md`](./escopo-grafo.md).

## 1. Objetivo do Usuário
Permitir que hemocentros e hospitais de uma rede regional simulada cadastrem bolsas de hemocomponentes, abram requisições transfusionais, aloquem bolsas respeitando a compatibilidade ABO/Rh e a ordem de vencimento (FEFO), e calculem a rota de menor tempo entre unidades com monitoramento de cadeia fria.

## 2. Backbone (Jornada Principal)

| 1. Captação e Estoque | 2. Demanda Hospitalar | 3. Triagem e Alocação (Core) | 4. Roteamento Logístico (Grafo) | 5. Monitoramento e Governança |
| :--- | :--- | :--- | :--- | :--- |
| Cadastrar doador e agendar doação | Cadastrar hospitais e nós da rede | Validar compatibilidade ABO/Rh (8x8) | Cadastrar ligações (arestas em minutos) | Consultar estatísticas de estoque |
| Registrar bolsa coletada com validade | Abrir requisição de hemocomponentes | Priorizar bolsas por validade (FEFO) | Calcular caminho mínimo (Dijkstra) | Verificar saúde e versão (`/actuator`) |
| Consultar disponibilidade e vencimento | Acompanhar status da requisição | Reservar bolsas ou aguardar estoque | Sinalizar alerta de cadeia fria (> 120 min) | Auditar histórico de alocações |

---

## 3. Release 1 — Histórias Priorizadas, Tarefas e Critérios Gherkin

### História A — Compatibilidade ABO/Rh na Alocação de Bolsas
* **Como** gestor de estoque do hemocentro,
* **Quero** que a alocação selecione apenas bolsas compatíveis com o tipo sanguíneo da requisição,
* **Para** evitar alocações incompatíveis dentro das regras didáticas do sistema.
* **Limitação didática:** A matriz 8x8 é aplicada de forma uniforme para os hemocomponentes do sistema com finalidade acadêmica, não substituindo protocolos clínicos reais.

#### Tarefas Técnicas
1. **T-A1 (Unitário):** Integrar `CompatibilidadeAboRh` em `EstoqueService.alocar()`, filtrando bolsas incompatíveis e vencidas.
2. **T-A2 (Integração JPA):** Garantir que requisições sem bolsas compatíveis fiquem com status `AGUARDANDO_ESTOQUE` persistido no banco (`noRollbackFor = RegraNegocioException.class`).
3. **T-A3 (Aceitação BDD):** Validar cenários Gherkin de alocação compatível e bloqueio de incompatíveis.

---

### História B — Alocação por Validade Mais Próxima (FEFO)
* **Como** gestor de estoque,
* **Quero** que entre as bolsas disponíveis, compatíveis e não vencidas sejam alocadas primeiro as de vencimento mais próximo,
* **Para** reduzir o descarte por perda de validade.

#### Tarefas Técnicas
1. **T-B1 (Unitário):** Ordenar as bolsas candidatas em `EstoqueService.alocar()` por `dataValidade ASC` com desempate determinístico por `id ASC` (ou via `FilaFEFO`).
2. **T-B2 (Unitário — Limites):** Validar que bolsa que vence hoje (`LocalDate.now()`) é aceita e bolsa vencida ontem (`LocalDate.now().minusDays(1)`) é ignorada.
3. **T-B3 (Aceitação BDD):** Validar cenário Gherkin priorizando a bolsa de menor prazo de validade.

---

### História C — Cálculo de Rota de Caminho Mínimo (Dijkstra)
* **Como** administrador logístico,
* **Quero** calcular a rota de menor tempo em minutos entre dois nós da rede,
* **Para** otimizar a distribuição e identificar alertas de cadeia fria (> 120 minutos).

#### Tarefas Técnicas
1. **T-C1 (Unitário — `Grafo`):** Implementar Dijkstra para grafo não-direcionado com pesos positivos, cobrindo caminho indireto mais curto, `origem == destino` (custo 0) e nós desconexos (`existeRota = false`).
2. **T-C2 (Serviço e Controller):** Montar o grafo em `RedeService` a partir das `Ligacao` persistidas, retornar HTTP 404 para nós inexistentes e expor `GET /api/v1/rotas/caminho-minimo`.
3. **T-C3 (Aceitação BDD):** Validar cenário Gherkin do cálculo de rota ótima.