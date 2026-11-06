# Correções da revisão C++ e Java

## Comportamento atual do roteamento

O roteamento atual em C++ e Java usa o algoritmo guloso do vizinho mais próximo: em cada nó, escolhe a ligação de menor tempo para um vizinho ainda não visitado. Não é Dijkstra, não garante o menor custo total e pode parar em um beco sem saída mesmo havendo outro caminho. A complexidade esperada é O(V+E), considerando a visita dos vértices e das listas de vizinhos. As ligações são direcionadas.

O endpoint público continua com o nome histórico `GET /api/v1/rotas/vizinho-mais-proximo?origemId={id}&destinoId={id}`. A aplicação também oferece a tela [Rotas da rede](../src/main/resources/templates/rotas.html), acessível em `/rotas`; ela consulta a topologia e esse endpoint, exibe a sequência de pontos, o tempo e a distância e destaca a rota no mapa. A linha representa a sequência de pontos conectados, não o traçado real das ruas.

## Erros encontrados nesta revisão

### 1. Documentação dizia que a implementação era Dijkstra

**Arquivo:** este documento.

**Trecho incorreto:**

```text
Correção atual: a busca com retrocesso foi substituída por Dijkstra com fila de prioridade.
A rota tem o menor custo total para pesos não negativos.
```

Esse trecho era incompatível com o código efetivamente adotado, que voltou ao vizinho mais próximo. Também estava incorreta a complexidade O((V+E) log V) atribuída ao código atual.

**Correção:** o texto foi substituído pela descrição real do algoritmo guloso e sua complexidade O(V+E). O professor confirmou que o vizinho mais próximo é o algoritmo oficial; o código e a documentação refletem essa decisão sem afirmar que a heurística encontra o menor caminho global.

**Decisão confirmada pelo professor:** o algoritmo oficial desta entrega é o vizinho mais próximo. Portanto, a implementação gulosa e seus testes são a referência esperada; Dijkstra não é um requisito pendente para esta versão.

### 2. Teste Java chamava a heurística de Dijkstra

**Arquivo:** [RedeServiceTest.java](../src/test/java/com/hemoflow/hemoflow/rede/RedeServiceTest.java).

**Trecho anterior:**

```java
@DisplayName("Calcula menor tempo por Dijkstra e informa a distância total")
void calcularRotaDijkstraDeveUsarGrafoPersistidoEInformarDistancia() {
    // O teste chama redeService.calcularRotaVizinhoMaisProximo(...)
}
```

O teste cobria apenas uma ligação direta e, portanto, não verificava Dijkstra nem comparava caminhos alternativos.

**Correção:**

```java
@DisplayName("Calcula rota pelo vizinho mais próximo e informa tempo e distância")
void calcularRotaVizinhoMaisProximoDeveUsarGrafoPersistidoEInformarDistancia() {
    // O teste chama redeService.calcularRotaVizinhoMaisProximo(...)
}
```

O cenário continua verificando o caminho direto persistido, o tempo, a distância e a ordem dos pontos, sem alegar que valida um algoritmo que não é executado.

### 3. Faltava uma tela para consultar e exibir as rotas

**Problema:** existiam endpoints para consultar a topologia e calcular uma rota, mas nenhuma tela consumia esses dados. Assim, a rota não era visível na aplicação.

**Correção:** foi criada a página `/rotas`, com seletores de origem e destino alimentados pela topologia, consulta ao endpoint de roteamento, resumo do percurso e visualização no mapa. A página está ligada à navegação pública e ao painel administrativo.

### 4. Thymeleaf interpretava coordenadas JavaScript como expressão de template

**Arquivo:** [rotas.html](../src/main/resources/templates/rotas.html).

**Trecho que causava falha ao renderizar:**

```javascript
L.polyline([[from.latitude, from.longitude], [to.latitude, to.longitude]]);
```

O Thymeleaf trata a sequência `[[...]]` como expressão inline e tentava interpretá-la, causando erro HTTP ao abrir a tela.

**Correção:** foi desativado o processamento inline do Thymeleaf para o bloco JavaScript da página:

```html
<script th:inline="none">
```

O teste de renderização da rota verifica a resposta HTTP e a presença dos elementos principais da tela.

## Histórico da revisão de C++ e Java

A revisão anterior registrou problemas no roteador guloso que podia parar em um beco sem saída. Houve uma etapa intermediária com retrocesso e outra com Dijkstra; nenhuma delas representa o algoritmo atual. O professor confirmou que o algoritmo esperado é o vizinho mais próximo, e esse é o comportamento implementado e documentado para a entrega de U2. Esses registros históricos não devem ser usados para caracterizar o código atual.

Também foram registrados na revisão anterior ajustes no script de compilação C++ para verificar o código de saída do compilador. Consulte os comentários do próprio script e os testes do módulo C++ para o comportamento vigente.

## Verificações

Na validação direcionada da página `/rotas` e do serviço de rede, foram executados 10 testes, sem falhas nem erros. Em seguida, as classes Java direcionadas para algoritmos, compatibilidade ABO/Rh, FEFO, hash, integração de estoque e rede executaram 65 testes, também sem falhas nem erros. Isso não representa a execução de toda a suíte Maven.
