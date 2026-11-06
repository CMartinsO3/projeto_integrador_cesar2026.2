# Estatística e Probabilidade no HemoFlow

Módulo: `com.hemoflow.hemoflow.estatistica` (`EstatisticaMath`, `AnaliseAvancadaService`, `EstatisticaService`).
Painel: `/estatisticas`. API: `/api/v1/estatisticas/*`. Testes: `EstatisticaMathTest`, `AnaliseAvancadaServiceTest`.

## Mapa tópico da disciplina → implementação

| Tópico | Onde | Aplicação no HemoFlow |
|---|---|---|
| Distribuição de frequências, proporções | `EstatisticaService`, `/estoque`, `/requisicoes` | Bolsas e requisições por status/tipo/componente |
| Medidas de posição (média, mediana, moda, quartis, percentis) | `EstatisticaMath.media/mediana/moda/percentil` | Unidades por requisição, temperatura |
| Medidas de dispersão (variância, desvio, amplitude, CV) | `EstatisticaMath.variancia/desvioPadrao/amplitude/coeficienteVariacao` | Variabilidade da demanda e da temperatura |
| Histograma e boxplot | `histograma()` (Sturges) + painel | Perfil das requisições |
| Séries temporais, média móvel | `mediaMovel`, `serieDiaria` | Demanda diária de hemocomponentes |
| Correlação (Pearson) e regressão linear | `correlacao`, `regressaoLinear` | Tendência da demanda; quantidade × prazo |
| Probabilidade condicional e Teorema de Bayes | `probCondicional`, `bayes` | P(atendida \| tipo), P(tipo \| atendida) |
| Distribuição de Poisson | `poissonPmf/Cdf/Cauda` | Chegada de requisições/dia; risco de ruptura de estoque |
| Distribuição Binomial | `binomialPmf/CaudaSuperior` | P(≥8 atendimentos em 10 requisições) |
| Distribuição Normal | `normalCdf` | P(temperatura fora da faixa) |
| Esperança e variância de v.a. | `ModeloBinomial` | E[X]=np, Var[X]=np(1−p) |
| Intervalo de confiança (média e proporção) | `icMedia95`, `icProporcao95` | Demanda média, taxa de atendimento, % fora da faixa |
| Teste de hipótese (média, bilateral) | `estatisticaTeste`, `pValorBilateral` | H0: temperatura média = ponto médio da faixa |

## Fórmulas

- Média: x̄ = Σx / n. Variância amostral: s² = Σ(x − x̄)² / (n − 1). Desvio: s = √s². CV = s / |x̄| · 100.
- Percentil p: interpolação linear na posição (p/100)(n − 1) (equivale a `PERCENTIL.INC` do Excel). Q1 = P25, Q3 = P75.
- Histograma: k = ⌈1 + log₂ n⌉ classes de largura (máx − mín) / k (regra de Sturges).
- Pearson: r = Σ(x − x̄)(y − ȳ) / √[Σ(x − x̄)² Σ(y − ȳ)²]. Regressão: b = Σ(x − x̄)(y − ȳ) / Σ(x − x̄)², a = ȳ − b·x̄, R² = r².
- IC 95% da média: x̄ ± t(0,975; n−1) · s/√n (t de Student tabelado para n ≤ 31; z = 1,96 acima).
- IC 95% da proporção (Wald): p̂ ± 1,96 √[p̂(1 − p̂)/n].
- Teste z da média: z = (x̄ − μ₀) / (s/√n); p-valor = 2(1 − Φ(|z|)); rejeita H0 se p < 0,05.
- Bayes: P(A\|B) = P(B\|A)P(A) / [P(B\|A)P(A) + P(B\|¬A)P(¬A)].
- Poisson: P(X = k) = e^−λ λ^k / k!. Binomial: P(X = k) = C(n,k) p^k (1 − p)^(n−k).
- Normal: Φ calculada pela aproximação de Abramowitz & Stegun 7.1.26 (erro < 1,5·10⁻⁷).

## Risco de desabastecimento (US 9)

Para cada tipo sanguíneo: λ = (unidades solicitadas, exceto canceladas ÷ dias observados) × 7.
`P(ruptura) = P(D > estoque válido)` com D ~ Poisson(λ). Estoque válido = bolsas `DISPONIVEL` não vencidas.
Nível: BAIXO (< 5%), MEDIO (< 20%), ALTO (≥ 20%). Estoque de segurança = menor s com P(D ≤ s) ≥ 95%.

## Indicadores adicionais (descarte, hospitais, tempos, atendimento)

- **Taxa de atendimento (critério único):** `EstatisticaService.atendida` considera atendida a requisição `ALOCADA`, `EM_TRANSITO` ou `ENTREGUE`. Taxa = atendidas / total × 100 (1 casa decimal). `PENDENTE`, `AGUARDANDO_ESTOQUE` e `CANCELADA` não contam. É o mesmo critério em `/requisicoes`, `/probabilidade` e nos painéis (`TaxaAtendimentoTest`).
- **Taxa de descarte por vencimento:** `descartadas por vencimento / bolsas coletadas` (todas as bolsas registradas), no total e por tipo, com IC 95% de Wald truncado em [0, 100]%. O motivo fica em `Bolsa.motivoDescarte` (`VENCIMENTO` ou `OUTRO`): é `VENCIMENTO` quando a saída é registrada com esse motivo ou quando a bolsa já estava vencida ao ser descartada. Endpoint `/descarte`.
- **Demanda por hospital:** contagem de requisições, total de unidades, média e desvio padrão amostral (n−1) das unidades por requisição, ordenado por unidades. Requisição sem hospital aparece como "Sem hospital". Endpoint `/hospitais`.
- **Tempos:** estatística descritiva de `Entrega.tempoEstimadoMinutos` e do prazo de atendimento (`janelaEntrega − dataSolicitacao`, em horas). Requisições com data ausente são ignoradas e contadas em `requisicoesSemDados`; sem dados, as medidas voltam `null`. Endpoint `/tempos`.

Resultados do conjunto sintético e interpretação: [relatorio-achados-estatistica.md](./relatorio-achados-estatistica.md).

## Premissas e limitações

- Requisição "atendida" = `ALOCADA`, `EM_TRANSITO` ou `ENTREGUE` (critério único em todo o módulo).
- Poisson assume chegadas independentes e taxa constante; a demanda em unidades é aproximada como Poisson (ignora o agrupamento por requisição).
- Binomial assume requisições independentes com a mesma probabilidade de atendimento.
- Teste de hipótese e IC usam normalidade aproximada; com poucas leituras o resultado é indicativo.
- Medidas indefinidas (amostra pequena) retornam `null` e aparecem como "—" no painel.
- Faixas de temperatura são fictícias (ver `application.properties`), apenas demonstração.

