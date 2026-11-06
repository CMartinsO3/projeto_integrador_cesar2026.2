# Relatório de achados — Estatística e Probabilidade (HemoFlow)

Painel: `/estatisticas` · API: `/api/v1/estatisticas/*` · Fórmulas e premissas: [estatistica.md](./estatistica.md).

## 1. Dados utilizados

Todos os dados são **sintéticos** e reprodutíveis (`DadosIniciais`, semente fixa `Random(2026)`; entregas com `Random(77)`):

| Conjunto | Tamanho | Origem |
|---|---|---|
| Requisições | 40 (14 dias; 34 entregues, 5 canceladas, 1 pendente) | histórico gerado + 1 requisição de amostra |
| Bolsas | 24 (13 disponíveis, 3 em trânsito, 8 descartadas) | estoque inicial + 8 descartes históricos |
| Entregas | 34 (uma por requisição entregue) | histórico gerado |
| Leituras de temperatura | 24 (faixa fictícia 20–30 °C) | simulação Normal(25; 2,6) |

Os valores abaixo foram obtidos consultando a aplicação em execução e **conferidos por recálculo manual** a partir de `GET /api/v1/bolsas` e `GET /api/v1/requisicoes` (descarte, taxa de atendimento, demanda por hospital e prazo de atendimento bateram exatamente).

## 2. Resultados e interpretação

### 2.1 Taxa de atendimento (critério único)
Atendida = `ALOCADA`, `EM_TRANSITO` ou `ENTREGUE`. **34 de 40 = 85,0%** (IC 95%: 73,9% – 96,1%), idêntico em `/requisicoes`, `/probabilidade` e na página inicial. Interpretação: a maior parte da demanda é atendida; as perdas vêm de cancelamentos (5) e de 1 pedido pendente. Por tipo, A+ (60%), A− (67%) e O− (67%) atendem menos; B±, AB± têm 100% (amostras de 1 a 7 pedidos, pouco informativas).

### 2.2 Taxa de descarte por vencimento
**6 de 24 bolsas = 25,0%** (IC 95%: 7,7% – 42,3%). Outras 2 bolsas foram descartadas por motivo diferente de vencimento (não entram na taxa) e 1 bolsa vencida ainda consta como disponível no estoque. Por tipo: O+ 2/4 (50%), A+ 1/6 (16,7%), B− 1/3 (33,3%), AB+ 1/3 (33,3%), O− 1/4 (25%), demais 0%. Interpretação: a taxa é alta, mas o intervalo é largo (n = 24) e os intervalos por tipo (n de 1 a 6) são pouco conclusivos — não dá para afirmar que um tipo descarta mais que outro. O resultado reflete o histórico sintético criado de propósito para demonstrar o indicador.

### 2.3 Demanda por hospital
10 hospitais. Maior demanda: Hospital Getúlio Vargas (8 requisições, 19 unidades, média 2,38, dp 1,30), Hospital Esperança (6; 16 un.; 2,67; 0,82) e HEMOPE (5; 13 un.; 2,60; 1,14). Menor: Real Hospital Português (2; 3 un.). Interpretação: a demanda é concentrada em poucos hospitais (os 3 primeiros somam 48 das 97 unidades solicitadas, ≈ 49%), mas as médias por requisição ficam entre 1,5 e 3,7 unidades, com dispersão baixa.

### 2.4 Tempos
- **Duração estimada das entregas** (n = 34): média 26,3 min, mediana 23,0, dp 13,5 (CV 51%), IC 95% da média 21,7 – 30,8 min; mínimo 9 e máximo 46.
- **Prazo de atendimento** (`janelaEntrega − dataSolicitacao`, n = 40): média 12,4 h, mediana 12,5 h, dp 6,8 h, IC 95% 10,3 – 14,5 h; entre 4 h e 23 h.
Interpretação: as entregas são rápidas frente ao prazo concedido (mediana de 23 min contra 12,5 h), então a janela de entrega raramente é o gargalo; a variabilidade (CV > 50%) é grande nos dois indicadores.

### 2.5 Demanda diária e tendência
Demanda diária (n = 15 dias): média 6,47 un./dia, mediana 6, dp 3,83, IC 95% 4,34 – 8,59. Regressão linear: +0,35 un./dia por dia (r = 0,41; R² = 0,17); previsão do próximo dia ≈ 9,3 unidades. Interpretação: há tendência de alta, mas o R² baixo indica que o tempo explica apenas 17% da variação — usar como indicativo. A correlação entre quantidade solicitada e prazo é fraca (r = −0,17).

### 2.6 Risco de desabastecimento (Poisson, 7 dias)
Todos os 8 tipos saíram com nível **ALTO**: P(ruptura) de 28,7% (O−) a 99,9% (O+). Exemplos: O+ estoque 1, λ = 9,3 un. em 7 dias → 99,9%, estoque de segurança 15; B− estoque 2, λ = 9,3 → 99,5%; A+ estoque 2, λ = 4,7 → 84,4%. Interpretação: coerente com os dados — o estoque semeado tem 1 a 2 bolsas válidas por tipo contra uma demanda total de ≈ 6,5 un./dia (0,2 a 1,3 un./dia por tipo). Não representa um hemocentro real; serve para demonstrar o cálculo.

### 2.7 Probabilidade
Chegadas de requisições: λ = 2,67/dia (Poisson); P(nenhuma no dia) = 6,9%, P(≥1) = 93,1%, P(mais que o dobro da média) = 5,4%. Binomial (n = 10, p = 0,85): E[X] = 8,5, Var = 1,275, P(X ≥ 8) = 82,0%. Bayes: P(O+ | atendida) = 26,5% (O+ é o tipo mais pedido, 27,5% das requisições).

### 2.8 Cadeia fria
24 leituras: média 25,2 °C, dp 2,87; 2 fora da faixa (8,3%, IC 95% 0% – 19,4%). Teste de hipótese (H0: μ = 25 °C): z = 0,36, p = 0,72 → **não rejeita H0**. P(fora da faixa) pela Normal ajustada = 8,2%, coerente com os 8,3% observados.

## 3. Limitações
- Dados sintéticos, de 14 dias; amostras pequenas geram IC largos (especialmente por tipo sanguíneo).
- IC de proporção por Wald é impreciso com n pequeno ou p próximo de 0/1 (por isso há limites truncados em 0%).
- Poisson assume chegadas independentes e taxa constante; a demanda em unidades é aproximada como Poisson.
- A taxa de descarte usa como denominador **todas as bolsas registradas** (e não só as coletadas no período).
- O motivo "vencimento" é deduzido pela validade no descarte (ou informado explicitamente na saída da bolsa); descartes anteriores à coluna `motivoDescarte` ficam sem motivo até serem reclassificados.
- Tempos de entrega são estimativas (`tempoEstimadoMinutos`), não medições reais.
- Faixas de temperatura fictícias; sem validade clínica.

## 4. Conclusões
1. Os indicadores exigidos (descarte por vencimento, demanda por hospital, tempos, taxa de atendimento) estão implementados, expostos na API e no painel e coincidem com o recálculo manual.
2. No cenário sintético, o principal achado é o **risco ALTO de desabastecimento em todos os tipos**, causado por estoque pequeno frente à demanda, e uma **taxa de descarte por vencimento de 25%**, o que sugere priorizar FEFO e reposição.
3. A demanda tem leve tendência de alta e a cadeia fria está dentro do esperado (sem evidência de desvio da média).
