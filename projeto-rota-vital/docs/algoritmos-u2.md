# Unidade 2 — algoritmos, tradução comentada e análise

## Estado do código entregue

O projeto já continha implementações de FEFO por inserção ordenada (`FilaFEFO.java`, `fefo.cpp`), tabela hash com encadeamento (`TabelaHash.java`, `tabela_hash.cpp`) e matriz ABO/Rh (`CompatibilidadeAboRh.java`, `compat_abo_rh.cpp`). A matriz C++ foi compilada e seu teste executado: 27 combinações compatíveis, zero falhas.

Foram acrescentados nesta revisão:
- busca sequencial e binária em C++ e Java;
- Bubble, Selection, Insertion e Merge Sort em C++ e Java;
- roteamento pelo algoritmo do vizinho mais próximo em C++ e Java;
- testes automatizados Java e programas de teste C++ para os novos algoritmos.

**Ordenações contempladas:** conforme confirmado para esta disciplina, os quatro métodos vistos no semestre estão implementados em ambas as linguagens: Bubble Sort, Selection Sort, Insertion Sort e Merge Sort.

## Equivalência comentada

### Busca sequencial
C++ `busca_sequencial` e Java `BuscaOrdenacao.sequencial` percorrem o vetor do índice zero ao fim e retornam o índice na primeira igualdade; ambos retornam -1 se não encontrarem. O laço visita no máximo n posições: pior caso O(n), melhor O(1), espaço auxiliar O(1).

### Busca binária
C++ `busca_binaria` e Java `BuscaOrdenacao.binaria` mantêm limites `l` e `r`, calculam o meio e descartam metade do intervalo a cada comparação. Ambas exigem vetor previamente ordenado em ordem crescente. Pior caso O(log n), melhor O(1), espaço O(1) nas versões iterativas.

### Bubble Sort
Ambas comparam vizinhos e trocam quando estão invertidos. O indicador `trocou` permite encerrar cedo quando uma passagem não realiza trocas. Pior caso O(n²), melhor O(n) com vetor já ordenado, espaço O(1).

### Selection Sort
Ambas procuram o menor elemento da região ainda não ordenada e fazem uma troca ao final de cada passagem. O número de comparações é quadrático mesmo quando o vetor já está ordenado: O(n²) em todos os casos; espaço O(1).

### Insertion Sort
Ambas guardam a chave atual, deslocam para a direita os elementos maiores e inserem a chave no espaço aberto. Pior caso O(n²), melhor O(n) para entrada crescente, espaço O(1).

### Merge Sort
Ambas dividem recursivamente o vetor em metades, ordenam cada metade e intercalam os resultados em ordem. São O(n log n) em todos os casos; a implementação usa vetores auxiliares e consome O(n) de espaço adicional, além da pilha recursiva O(log n).

### FEFO por inserção ordenada
Em C++, `fefo_inserir_ordenado` percorre a lista ligada até a posição da validade; em Java, `FilaFEFO.adicionar` chama `ListaEncadeada.inserirOrdenado` com comparador por validade e desempate por id. A bolsa vencida é rejeitada. Inserção O(n), consulta/remoção da primeira O(1), remoção por id O(n), espaço O(n). As versões mantêm a mesma regra de desempate: validade crescente, menor identificador primeiro e ordem de chegada quando validade e identificador também empatam.

### Tabela hash
As duas versões usam a função polinomial `h = h*31 + caractere`, módulo pela capacidade e encadeamento separado para colisões. Inserção/busca/remoção são O(1) em média sob distribuição razoável e fator de carga controlado; O(n) no pior caso se todas as chaves caírem no mesmo bucket. Espaço O(m+n), m buckets e n chaves. O C++ usa `malloc/free`; Java cria objetos e deixa a memória para o Garbage Collector.

**Busca por hash:** as operações `hash_buscar` em C++ e `TabelaHash.buscar` em Java calculam o bucket diretamente e comparam somente as chaves encadeadas nele. Para uma chave de comprimento L e cadeia de comprimento k, o custo real é O(L+k), incluindo o cálculo do hash e as comparações; no índice de estoque as chaves têm tamanho limitado e fixo, então isso equivale a O(1) em média e O(n) no pior caso, se as n chaves colidirem. A tabela Java própria também permite colisões forçadas nos testes.

### Compatibilidade ABO/Rh
As duas versões consultam `MATRIZ[doador][receptor]`. Os índices são constantes de enumeração na mesma ordem. A consulta acessa uma única célula da matriz 8×8: O(1) tempo e O(1) espaço fixo. O teste C++ cobre as 64 células e encontrou 27 compatibilidades.

### Roteamento pelo vizinho mais próximo
Os roteadores C++ e Java iniciam na origem informada para o pedido e, em cada passo, percorrem as arestas de saída do vértice atual para escolher o vizinho ainda não visitado com menor tempo de viagem. Em caso de empate, escolhem o identificador do destino em ordem lexicográfica. O processo termina ao alcançar o destino ou quando o vértice atual não tem vizinhos elegíveis. Pesos negativos e destinos inexistentes são ignorados; a classe `Grafo` Java também rejeita pesos negativos ao cadastrar arestas. Para representar uma via de mão dupla, cadastre uma ligação em cada sentido.

**Complexidade:** sejam V o número de vértices e E o número de arestas. Cada vértice é visitado no máximo uma vez; ao visitá-lo, o algoritmo examina sua lista de vizinhos. Como as listas de adjacência de vértices distintos são percorridas no máximo uma vez, a soma das iterações é limitada por E. A inicialização dos vértices/visitados custa O(V), logo o tempo é O(V+E) (esperado para as consultas em tabelas hash) e o espaço auxiliar é O(V), sem contar o grafo de entrada.

Como a escolha é local e gulosa, o resultado não garante o menor custo total: a rota pode ser mais longa que outra disponível ou pode não alcançar o destino mesmo existindo outro caminho. A implementação reporta `encontrado=false` nesses casos e mantém a sequência parcial visitada; não faz retrocesso.

## Integração realizada nesta revisão

1. `IndiceEstoque.candidatasCompativeis` consulta os grupos da tabela hash para cada tipo doador compatível com o receptor. Cada grupo entrega seus itens pela fila FEFO; as listas são reunidas e ordenadas globalmente por validade crescente, sem retirar bolsas antes da confirmação da transação.
2. `EstoqueService.alocar` agora usa o índice hash + FEFO, aplica a matriz ABO/Rh para hemácias e seleciona apenas bolsas que tenham rota encontrada entre a localização atual e o hospital solicitante. Depois de salvar as bolsas como `ALOCADA`, a retirada do índice acontece após o commit.
3. `RedeService.calcularRotaVizinhoMaisProximo` monta o grafo com `NoRede` e `Ligacao` persistidos e aplica o algoritmo guloso do vizinho mais próximo, escolhendo o menor tempo entre os vizinhos não visitados a partir da origem do pedido. A distância em linha reta de cada ligação é calculada em quilômetros pela fórmula de Haversine a partir das coordenadas de origem e destino. A API `GET /api/v1/topologia` informa o tempo e a distância das ligações; `GET /api/v1/rotas/vizinho-mais-proximo?origemId=1&destinoId=2` informa o tempo e a distância total do percurso encontrado.
4. As ligações do banco são tratadas como direcionadas: o algoritmo só percorre o sentido origem → destino cadastrado. Para permitir retorno, cadastre também a ligação inversa.
5. A matriz ABO/Rh implementada é a matriz de compatibilidade para hemácias. Para evitar aplicar indevidamente essa matriz a plasma, plaquetas e crioprecipitado, a alocação desses componentes exige tipo sanguíneo idêntico. Essa é uma regra conservadora provisória, não substitui validação transfusional nem matrizes específicas aprovadas pelo professor/especialista.
6. Os roteadores C++ e Java usam vizinho mais próximo. Cada vértice é visitado no máximo uma vez e cada lista de adjacência é examinada no máximo uma vez: tempo O(V+E), esperado, e espaço auxiliar O(V). Por ser guloso, não garante o menor custo total nem encontrar um caminho existente.
7. A entrega inclui exatamente as quatro ordenações informadas pelo professor: Bubble, Selection, Insertion e Merge Sort.

## Tabela consolidada de complexidade por classe

Notação: `n` é a quantidade de elementos; `V` e `E` são vértices e arestas do grafo; `d(v)` é o grau de saída do vértice; `k` é a quantidade de bolsas candidatas; `r` é a quantidade pedida; `m` é a capacidade de buckets; `L` é o comprimento da chave. O espaço indicado é auxiliar, exceto quando explicitamente descrito como armazenamento da própria estrutura. Custos de leitura/gravação no banco, rede e inicialização da JVM não são incluídos. Hashes têm tempo esperado O(1), salvo indicação do pior caso.

| Pacote / classe | Operações relevantes | Tempo | Espaço |
|---|---|---|---|
| `algoritmos.BuscaOrdenacao` | Busca sequencial; binária; Bubble/Selection/Insertion Sort; Merge Sort | Sequencial O(n); binária O(log n); Bubble melhor O(n), pior O(n²); Selection O(n²); Insertion melhor O(n), pior O(n²); Merge O(n log n) | Buscas e três ordenações in-place O(1); Merge O(n) auxiliar + O(log n) pilha recursiva |
| `algoritmos.RoteadorVizinhoMaisProximo` | Para cada vértice atual, examina seus vizinhos e escolhe o menor peso elegível | O(V+E) esperado: visitados limita cada vértice a uma expansão; as listas de adjacência somam no máximo E arestas | O(V) para visitados e caminho |
| `estruturas.ListaEncadeada` | Inserir início / remover início; inserir fim / ordenado; buscar / remover por valor ou condição; copiar para lista | O(1) nas operações de início; O(n) nas demais | Armazenamento O(n); auxiliar O(1), exceto cópia O(n) |
| `estruturas.FilaSimples` | Enfileirar, desenfileirar, espiar; `toString` | O(1) por operação principal; `toString` O(n) | Armazenamento O(n); auxiliar O(1) |
| `estruturas.PilhaSimples` | Empilhar, desempilhar, espiar; `toString` | O(1) por operação principal; `toString` O(n) | Armazenamento O(n); auxiliar O(1) |
| `estruturas.TabelaHash` | Inserir, buscar, consultar, remover; listar chaves / limpar / maior cadeia | Hash O(L); operações O(L+1) esperado e O(L+n) no pior caso; listar chaves e maior cadeia O(m+n); limpar O(m) | O(m+n), buckets mais entradas encadeadas |
| `estoque.CompatibilidadeAboRh` | Uma leitura da matriz 8×8 | O(1) | O(1) fixo (64 células) |
| `estoque.FilaFEFO` | Inserir em ordem; retirar próxima; remover por ID; listar válidas; descartar vencidas | Inserção O(n); próxima O(1) se nada venceu, ou O(q) para q vencidas descartadas; remover ID O(n); listar O(n) | Armazenamento O(n); listagem O(n); demais auxiliar O(1) |
| `estoque.IndiceEstoque` | Registrar/retirar; consultar candidatos; descartar vencidas; reconstruir | Hash O(1) esperado por grupo; compatibilidade ABO/Rh verifica 8 tipos (constante), reúne k bolsas e ordena em O(k log k); tipo exato O(k); descartar O(n); reconstruir O(n²) no pior caso se as inserções FEFO se concentrarem num único grupo | O(n) bolsas/grupos; candidatos O(k) |
| `estoque.IndiceEstoqueCarga` | Consultar N bolsas disponíveis e reconstruir o índice | O(N²) no pior caso pela inserção ordenada repetida numa fila FEFO do mesmo grupo; grupos distribuídos reduzem o percurso por grupo | O(N) para índice e filas |
| `estoque.ProcessadorPedidoService` | Buscar pedido, consultar hash, filtrar compatibilidade/FEFO, calcular rotas e alocar até r bolsas | Para k candidatas e até k tentativas de rota: O(k log k + k(V+E)) esperado; o trecho de vizinhos examinado por rota é O(V+E). Custos de banco ficam fora | O(k+V+E+rV) incluindo candidatas, grafo temporário e até r caminhos retornados |
| `estoque.EstoqueService` | CRUD e delegação do fluxo de alocação | Operações simples dependem de uma operação JPA; `alocar` tem a complexidade de `ProcessadorPedidoService` | O(k+V+E+rV) durante alocação, conforme acima; chamadas simples O(1) auxiliar |
| `estoque.EstruturasEstoqueService` | Operações de lista, fila e pilha em memória | Registrar/remover/consultar bolsa O(n); enfileirar/atender requisição O(1); empilhar/retirar histórico O(1); consultar lista completa O(n) | Armazenamento O(n); cópias de consulta O(n) |
| `estoque.Bolsa` | Registro simples de ID e validade para estruturas didáticas | O(1) por construção/acesso | O(1) por instância |
| `dominio.Bolsa`, `dominio.NoRede`, `dominio.Ligacao`, `dominio.Requisicao`, `dominio.Hospital` | Entidades JPA; getters/setters e relações já materializadas | O(1) por acesso a campo; consultas e persistência são dependentes do banco | O(1) auxiliar por acesso; armazenamento mantido pelo contexto JPA/banco |
| `grafo.Aresta` | Guarda destino, peso e distância | O(1) por construção/acesso | O(1) por aresta; armazenamento total O(E) |
| `grafo.DistanciaGeografica` | Fórmula de Haversine com número fixo de operações matemáticas | O(1) | O(1) |
| `grafo.Grafo` | Adicionar nó/aresta; copiar vizinhos; montar cópia do grafo e rotear | Adicionar nó/aresta O(1) esperado; `getVizinhos` O(d(v)) pela cópia; montar/rotear O(V+E), incluindo conversão do grafo e vizinho mais próximo | Estrutura O(V+E); cópia temporária de roteamento O(V+E) |
| `rede.RedeService` | Carregar nós/ligações persistidos, montar grafo, calcular rota e distância | O(V+E) por rota calculada, além do acesso ao banco; cálculo da distância é O(1) por aresta percorrida no caminho | O(V+E) para o grafo e mapas temporários |

O custo `O(k log k + k(V+E))` do processador é um limite superior: ele pode interromper o cálculo das rotas quando já reuniu `r` bolsas válidas. A etapa FEFO ordena as candidatas antes de testar as rotas, para preservar a seleção por validade. Como o roteador é guloso, “rota encontrada” não significa que seja o menor custo global.

## Execução dos testes

C++:
```bash
g++ -std=c++17 c_structures/busca_ordenacao.cpp c_structures/teste_busca_ordenacao.cpp -o teste_busca
./teste_busca

g++ -std=c++17 c_structures/roteador_vizinho.cpp c_structures/teste_roteador.cpp -o teste_rota
./teste_rota

g++ -std=c++17 c_structures/fefo.cpp c_structures/tabela_hash.cpp c_structures/compat_abo_rh.cpp c_structures/teste_hash.cpp -o teste_hash
./teste_hash
```

Java: a execução direcionada desta revisão executou 20 testes sem falhas nem erros, incluindo um cenário integrado de hemácias que consulta grupos do índice hash, rejeita uma bolsa ABO/Rh incompatível com validade mais curta, escolhe por FEFO a bolsa compatível de outro grupo sanguíneo e confirma a rota calculada:
```bash
mvnw.cmd -Dtest=AlgoritmosU2Test,RedeServiceTest,EstoqueServiceTest test
```
Esta não foi uma execução da suíte Maven completa. Os testes de integração também cobrem alocação persistida, status da requisição/bolsa, validade e cálculo da rota. Os testes de busca cobrem elementos encontrados/não encontrados, vetor vazio, elemento único e repetidos. Os quatro métodos de ordenação são verificados com entradas variadas, incluindo vazia, unitária, já ordenada, inversamente ordenada, valores negativos e duplicados. Na busca binária, a entrada precisa estar previamente ordenada; quando há repetidos, qualquer índice que contenha a chave é um resultado válido.
