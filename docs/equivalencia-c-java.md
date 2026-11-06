# Equivalência entre as versões C/C++ e Java — Estruturas e algoritmos (Unidades 1 e 2)
---

## Quadro-resumo da ponte C/C++ → Java

| Conceito em C/C++ | Correspondência no Java do projeto | O que permanece equivalente | Diferença de linguagem |
|---|---|---|---|
| `struct` com campos e funções que recebem um ponteiro para a estrutura | Classe Java com campos e métodos de instância; por exemplo, `NoRede`, `Bolsa` e as classes de estrutura | Os campos representam o mesmo estado e os métodos executam as mesmas operações do domínio. | Classes Java têm métodos associados a instâncias e controle de acesso; não há cópia automática da semântica de `struct` C. |
| `malloc(sizeof(T))` para reservar um nó/entrada e `free(ptr)` para liberá-lo | `new No<>(...)` ou `new Entrada<>(...)`, com referências Java | Um novo elemento é criado e ligado à estrutura; ao remover, a ligação que o mantinha alcançável é desfeita. | Em C/C++, o programa é responsável por liberar a alocação exatamente uma vez. Java não expõe `free`: objetos sem referências alcançáveis podem ser recuperados pelo Garbage Collector, em momento não determinístico. |
| Ponteiro `T*`, `proximo`, `cabeca`, `topo`, `frente` e `cauda` | Referências de objeto, como `No<T> proximo` e os campos de cabeça/topo/frente/cauda | A navegação e a atualização dos encadeamentos seguem a mesma sequência lógica. | Referências Java são gerenciadas pela JVM e não permitem aritmética de ponteiros; referências nulas são verificadas em execução. |
| Vetor de ponteiros para buckets e cadeia de entradas (`Grupo*`) | Array de buckets (`Entrada<V>[]`) e cadeia por referência | Hash, módulo, colisão por encadeamento e percurso da cadeia são os mesmos passos do algoritmo. | A tabela Java guarda valores genéricos e o Garbage Collector administra nós; a implementação não delega a lógica a `HashMap`. |
| Retorno sentinela, por exemplo `-1` ou `NULL` | Retorno `null`, `boolean` ou exceção explícita (`IllegalStateException`) | Os dois lados sinalizam vazio/não encontrado e preservam a regra operacional. | Exceções e tipos de retorno tornam explícito o erro em Java; um sentinel pode ser ambíguo se também for um dado válido. |
| `int[]` / `std::vector<int>` e laços de índice | `int[]` e laços Java | Buscas e ordenações conservam comparações, limites e ordem de processamento. | Java verifica limites de array em execução; C++ não oferece essa verificação para `operator[]`. |
| `std::vector`, `std::unordered_map`/`unordered_set`, `std::sort` e `std::priority_queue` usados pelo algoritmo | `List`, `Map`/`Set`, `Comparator` e `PriorityQueue` do Java quando aplicáveis | As estruturas fornecem as mesmas abstrações de coleção e ordenação. | As APIs e garantias de implementação diferem. Neste projeto, o roteador guloso percorre listas de adjacência e conjuntos de visitados; não usa fila de prioridade. |
| `enum` e índices de matriz (`MATRIZ[doador][receptor]`) | `enum` Java e matriz `boolean[][]` indexada por `ordinal()` | A matriz de compatibilidade é consultada em tempo constante nas duas versões. | Em Java a ordem dos enums determina os índices; testes devem impedir reordenação acidental. |
| Função livre de C, por exemplo `vizinho_mais_proximo(...)` | Método estático Java `RoteadorVizinhoMaisProximo.calcular(...)` e serviço Spring `ProcessadorPedidoService` | Os parâmetros, as decisões do algoritmo e o resultado da rota refletem o mesmo fluxo. | O serviço Java recebe dependências por injeção e executa operações JPA/transacionais; isso não faz parte da função C++ do algoritmo. |

O princípio da tradução é preservar a **invariante e a sequência do algoritmo**, não imitar manualmente o gerenciamento de memória de C++. Na versão Java, cada nó encadeado continua sendo um objeto com referência ao próximo; ao removê-lo da lista, fila, pilha ou bucket, a aplicação atualiza as referências e deixa a coleta ao Garbage Collector. Não se chama `free` nem se tenta simular liberação manual.

## 1. Lista Encadeada (Estoque)

**C++:** `c_structures/lista.cpp` / `lista.hpp`
**Java:** `src/main/java/.../estruturas/ListaEncadeada.java`

Em C++, a lista é representada por dois `struct`s: `NoLista` (contendo o dado
`bolsa_id` e um ponteiro `struct NoLista* proximo`) e um descritor `Lista`
(contendo o ponteiro `cabeca` e o campo `tamanho`). Cada inserção aloca um nó
novo com `malloc(sizeof(NoLista))` e o encadeia manualmente ajustando os
ponteiros (`novo->proximo = l->cabeca; l->cabeca = novo;` para inserção no
início). Cada remoção desfaz esse encadeamento e libera a memória do nó
removido explicitamente com `free(remover)`.

Em Java, a classe interna privada `No<T>` cumpre exatamente o papel do
`struct NoLista`: o campo `T dado` corresponde a `bolsa_id` (generalizado para
qualquer tipo, já que em Java não precisamos de uma lista específica por tipo
de dado) e o campo `No<T> proximo` corresponde ao ponteiro `struct NoLista*
proximo` — a única diferença é que, em Java, esse "ponteiro" é uma referência
gerenciada pelo garbage collector, então não existe `malloc`/`free`
explícitos: alocar um nó é `new No<>(dado)`, e "liberar" um nó é simplesmente
deixar de referenciá-lo (`cabeca = cabeca.proximo`), o que o coletor de lixo
recicla depois. A lógica de navegação é idêntica nas duas versões: os métodos
`inserirNoInicio`, `inserirNoFim`, `removerDoInicio` e `contem` da classe Java
seguem o mesmo algoritmo, passo a passo, dos métodos `lista_inserir_inicio`,
`lista_inserir_fim`, `lista_remover_inicio` e `lista_contem` em C — inclusive
mantendo as mesmas complexidades (O(1) para inserção/remoção no início, O(n)
para inserção no fim e busca).

## 2. Fila (Requisições)

**C++:** `c_structures/fila.cpp` / `fila.hpp`
**Java:** `src/main/java/.../estruturas/FilaSimples.java`

A versão em C usa a mesma ideia de nó encadeado (`struct NoFila`), mas o
descritor `Fila` mantém **dois** ponteiros — `frente` e `cauda` — para que
tanto o enfileiramento quanto o desenfileiramento sejam O(1), sem precisar
percorrer a fila inteira a cada operação. `fila_enfileirar` aloca o novo nó
com `malloc` e encadeia na cauda; se a fila estava vazia, `frente` e `cauda`
passam a apontar para o mesmo nó recém-criado. `fila_desenfileirar` libera o
nó da frente com `free` e avança o ponteiro `frente`; se a fila ficou vazia,
`cauda` também é zerado.

A classe `FilaSimples<T>` em Java replica exatamente essa estrutura de dois
ponteiros: os campos privados `frente` e `cauda` (instâncias da classe interna
`No<T>`) desempenham o mesmo papel dos ponteiros `frente`/`cauda` do struct
`Fila` em C. O método `enfileirar` reproduz o mesmo tratamento de caso
especial da fila vazia que existe em `fila_enfileirar` (`if (cauda == null)
{ frente = novo; } else { cauda.proximo = novo; }`), e `desenfileirar` reflete
o mesmo ajuste de `cauda = null` quando a fila esvazia, que em C aparece como
`if (f->frente == NULL) { f->cauda = NULL; }`. A única diferença estrutural é
que em Java as exceções (`IllegalStateException`) substituem o valor
sentinela `-1` usado em C para sinalizar fila vazia — uma escolha de
idioma da linguagem, não uma mudança de lógica.

## 3. Pilha (Histórico de Operações)

**C++:** `c_structures/pilha.cpp` / `pilha.hpp`
**Java:** `src/main/java/.../estruturas/PilhaSimples.java`

Em C++, a pilha usa um único ponteiro de topo (`struct NoPilha* topo` dentro do
descritor `Pilha`). `pilha_empilhar` aloca o novo nó com `malloc`, faz esse
nó apontar para o antigo topo (`novo->proximo = p->topo;`) e atualiza o topo
para o novo nó (`p->topo = novo;`) — a inversão clássica de ponteiros que
caracteriza a política LIFO. `pilha_desempilhar` faz o caminho inverso: lê o
dado do topo, avança o ponteiro `topo` para o nó anterior (`p->topo =
remover->proximo;`) e libera o nó removido com `free`.

A classe `PilhaSimples<T>` em Java replica esse mesmo mecanismo: o campo
`topo` (um `No<T>`) equivale ao ponteiro `topo` do struct `Pilha`, e o método
`empilhar` executa literalmente a mesma sequência de duas atribuições
(`novo.proximo = topo; topo = novo;`) que aparece em `pilha_empilhar`. O
método `desempilhar` faz o mesmo "recuo" do ponteiro de topo (`topo =
topo.proximo;`) que `pilha_desempilhar` faz em C++, apenas sem a chamada
explícita a `free`, pelo mesmo motivo já explicado na lista: em Java a
liberação de memória é responsabilidade do garbage collector, e o nó deixa
de ser alcançável assim que nada mais aponta para ele.

## 4. Resumo da equivalência

| Aspecto | C/C++ | Java |
|---|---|---|
| Nó da estrutura | `struct` com dado + ponteiro `proximo` | classe interna `No<T>` com campo `dado` + referência `proximo` |
| Alocação de um nó | `malloc(sizeof(NoX))` | `new No<>(dado)` |
| Liberação de um nó | `free(ponteiro)` explícito | implícita — nó sem referências é coletado pelo GC |
| Navegação/encadeamento | manipulação direta de ponteiros (`->proximo`) | manipulação direta de referências (`.proximo`) — mesma lógica |
| Sinalização de estrutura vazia | valor sentinela (`-1` ou `NULL`) retornado pela função | exceção (`IllegalStateException`) lançada pelo método |
| Genericidade | uma implementação por tipo de dado (`int bolsa_id`, `int requisicao_id`, `int operacao_id`) | uma única implementação genérica (`<T>`), reaproveitável para qualquer entidade do domínio |

A tradução entre as duas versões preserva, em todos os três casos, o mesmo
algoritmo e a mesma complexidade assintótica — a diferença está apenas em
como cada linguagem gerencia memória (manual em C++, automática em Java) e em
como cada uma sinaliza casos de borda (valor sentinela vs. exceção).

## 5. Compatibilidade ABO/Rh (matriz 8×8)

**C++:** `c_structures/compat_abo_rh.cpp` — `static const int MATRIZ[8][8]` e a função `int compat_compativel(int doador, int receptor)`.
**Java:** `estoque/CompatibilidadeAboRh.java` — `private static final boolean[][] MATRIZ` e o método `static boolean compativel(TipoSanguineo doador, TipoSanguineo receptorSolicitado)`.

As duas versões usam a mesma ideia: uma tabela de 8 linhas (tipo do doador) por 8 colunas (tipo do receptor), onde cada célula indica se a doação é compatível. Em C++, a matriz é um `int[8][8]` com 0 e 1, e os tipos são as constantes do `enum TipoSanguineo` (`TS_A_POS = 0` … `TS_O_NEG = 7`), que servem direto como índice. Em Java, a matriz é um `boolean[8][8]` e o índice vem de `TipoSanguineo.ordinal()`. Como as duas versões listam os tipos na mesma ordem (A+, A−, B+, B−, AB+, AB−, O+, O−), a linha `MATRIZ[doador][receptor]` é a mesma nas duas linguagens. A diferença está nas verificações: em C++, `compat_compativel` devolve 0 se um índice estiver fora de 0–7; em Java, `compativel` devolve `false` se algum dos tipos for `null`. Antes, a versão Java usava um `EnumMap<TipoSanguineo, Set<TipoSanguineo>>`; a troca pela matriz manteve a assinatura pública, então `EstoqueService` e os testes antigos continuam funcionando sem mudança.

**Complexidade (justificada pelo código):**
- **Tempo O(1):** `compativel` faz duas comparações com `null` e uma única leitura `MATRIZ[i][j]`. Não há laço nem busca, então o tempo não depende do número de bolsas no estoque. (O `EnumMap` anterior também era O(1); a matriz deixa o acesso direto por índice, igual ao C.)
- **Espaço O(1):** a matriz tem 8 × 8 = 64 células fixas, que não crescem com o estoque.

**Testes:** `CompatibilidadeAboRhTest` (Java) e `teste_compat.cpp` (C++) conferem as 64 combinações contra a regra dos antígenos (A, B e Rh: o doador não pode ter nenhum antígeno que o receptor não tenha), verificam que existem exatamente 27 pares compatíveis, os casos de compatibilidade (O− doa para todos, AB+ recebe de todos, mesmo tipo) e de incompatibilidade (A+→B−, A+→A−, Rh+→Rh−), e protegem a ordem do enum.

## 6. FEFO — First Expired, First Out (lista ordenada por validade)

**C++:** `c_structures/fefo.cpp` / `fefo.hpp` — `fefo_inserir_ordenado` e `fefo_remover_primeira`.
**Java:** `estoque/FilaFEFO.java` (usa `estruturas/ListaEncadeada.java`, método `inserirOrdenado`) — `adicionar` e `proxima`.

As duas versões mantêm uma lista encadeada que está sempre ordenada por validade crescente. Em vez de procurar a menor validade a cada pedido, cada bolsa nova é inserida no lugar certo, e a primeira da lista é sempre a que vence antes. Em C++, `fefo_inserir_ordenado` aloca o nó com `malloc` e percorre a lista com um ponteiro para ponteiro (`NoFefo** p`, que começa em `&l->cabeca`) enquanto o nó atual vier antes ou empatar com a nova bolsa; depois faz `novo->proximo = *p; *p = novo;`. Em Java, `ListaEncadeada.inserirOrdenado` faz a mesma caminhada com uma referência ao nó `anterior` e um `Comparator`, tratando a inserção na cabeça como caso especial, e `FilaFEFO` fornece o comparador (validade, depois id). Retirar a próxima bolsa é tirar a cabeça: `fefo_remover_primeira` em C++ (devolve `-1` se não houver bolsa) e `proxima()` em Java (devolve `null`). A diferença está nos detalhes de cada linguagem: em C++ a validade é um inteiro `AAAAMMDD`, o `free` do nó é manual e a data de hoje é um parâmetro; em Java a validade é `LocalDate`, o nó retirado é coletado pelo GC e a data de hoje vem de um `Clock` (que os testes podem fixar). Antes, `FilaFEFO` usava `PriorityQueue` (inserção O(log n)), que escondia a lógica; agora a regra de ordenação aparece no código da lista, como o professor pediu.

**Regras de desempate e descarte:** com a mesma validade, sai primeiro a bolsa de menor id; se o id também for igual (ou ainda não existir), vale a ordem de chegada. Bolsa já vencida é descartada na entrada (`adicionar` devolve `false`, `fefo_inserir_ordenado` devolve `0`) e, se vencer enquanto espera na lista, é pulada na retirada. Como a lista é ordenada, as vencidas ficam sempre na frente.

**Complexidade (justificada pelo código):**
- **Inserção O(n):** no pior caso (bolsa que vence depois de todas) o laço percorre os n nós da lista.
- **Retirada do topo O(1):** só se mexe na cabeça; descartar bolsas que venceram só custa mais proporcionalmente ao número de bolsas descartadas, e cada bolsa é descartada uma única vez.
- **Espaço O(n):** um nó por bolsa guardada.

**Testes:** `FilaFEFOTest` e `ListaEncadeadaOrdenadaTest` (Java) e `teste_fefo.cpp` (C++) conferem a ordem de saída, o exemplo da inserção entre 15/10 e 25/10, o desempate por id e por ordem de chegada, a fila vazia, a bolsa vencida na entrada e a que vence dentro da lista, e comparam uma sequência aleatória (semente fixa) com a ordenação completa feita por `qsort` (C++) e `List.sort` (Java).

## 7. Índice de estoque por tabela hash (com encadeamento)

**C++:** `c_structures/tabela_hash.cpp` / `tabela_hash.hpp` — `hash_funcao`, `hash_posicao`, `hash_inserir`, `hash_buscar`, `hash_remover`, e as operações de estoque `indice_registrar` / `indice_proxima`.
**Java:** `estruturas/TabelaHash.java` (tabela própria, sem `HashMap`) e `estoque/IndiceEstoque.java` (o índice do estoque, sincronizado com o `EstoqueService`).

**Chave e tamanho.** A chave é o tipo sanguíneo mais o hemocomponente, no formato `A_POS|HEMACIAS`, então existem 8 × 4 = 32 chaves possíveis. Cada chave leva a um grupo que guarda a lista FEFO (seção 6) das bolsas daquele tipo. A tabela tem 67 posições (`HASH_TAM` em C++, `CAPACIDADE_PADRAO` em Java), um número primo maior que o dobro das chaves possíveis: no pior caso o fator de carga é 32 / 67 ≈ 0,48. Com essa tabela, as 32 chaves ocupam 28 posições e a maior cadeia tem 2 grupos (4 colisões reais), o que exercita o encadeamento sem deixar as cadeias longas.

**Função hash e posição.** As duas versões usam `h = h * 31 + caractere` sobre os caracteres da chave, com aritmética de 32 bits sem sinal, e a posição é `h % 67`. Em C++, `hash_funcao` usa `uint32_t`; em Java, `TabelaHash.hash` usa `int` (o estouro do `int` dá o mesmo resultado, e a posição é calculada com `Integer.remainderUnsigned`). Para chaves ASCII, as duas linguagens chegam à mesma posição: `A_POS|HEMACIAS` cai na posição 21, `O_NEG|PLASMA` na 31 e `AB_NEG|PLAQUETAS` na 65, valores conferidos nos testes das duas versões.

**Colisão.** Quando duas chaves caem na mesma posição, elas ficam encadeadas no mesmo bucket. Em C++, o bucket é um ponteiro para o primeiro `Grupo`, e cada `Grupo` tem o ponteiro `proximo` para o seguinte; `hash_inserir` coloca o novo grupo no início do bucket, e `hash_remover` usa um ponteiro para ponteiro (`Grupo** p`) para desencadear o grupo, seja ele a cabeça ou um nó do meio. Em Java, o bucket é uma referência para uma `Entrada`, com o campo `proximo`, e `TabelaHash.remover` guarda o nó `anterior` pelo mesmo motivo. Buscar uma chave calcula a posição (uma conta), vai direto ao bucket e compara só as poucas chaves dali, sem olhar as bolsas dos outros grupos.

**Sincronização com o `EstoqueService`.** O banco continua sendo a fonte da verdade; o índice fica em memória. Ele é montado quando a aplicação termina de subir (`IndiceEstoqueCarga`, que lê as bolsas `DISPONIVEL`) e é atualizado pelo `EstoqueService`: registrar entrada ou criar bolsa coloca a bolsa no grupo; alocar, registrar saída e mudar o status para fora de `DISPONIVEL` tiram a bolsa do índice; e `alocar` também descarta do índice as bolsas que já venceram. Essas mudanças só acontecem depois do commit da transação (`registrarAposCommit` / `retirarAposCommit`), para que um rollback não deixe bolsas inexistentes no índice. Como o índice é compartilhado entre requisições, seus métodos são `synchronized`. Durante a alocação, `IndiceEstoque.candidatasCompativeis` consulta a hash pelos grupos ABO/Rh compatíveis para hemácias e reúne as bolsas em ordem FEFO global. Para outros componentes, `candidatasCompativeisExatas` aplica o tipo idêntico como regra conservadora. O `EstoqueService` então filtra as bolsas sem localização ou sem rota até o hospital, seleciona as primeiras em FEFO, persiste o status e agenda a retirada do índice após o commit.

**Complexidade (justificada pelo código):**
- **Inserir, buscar e remover: O(1) em média.** O custo é uma conta (a função hash, proporcional ao tamanho da chave, que é pequeno e fixo) mais a comparação das chaves do bucket. Com fator de carga ≈ 0,48 e maior cadeia de 2, são poucas comparações.
- **Pior caso: O(n).** Se todas as n chaves caírem no mesmo bucket, a busca percorre uma cadeia com n grupos.
- **Espaço: O(n).** A tabela tem 67 posições fixas mais um grupo por chave existente. Um grupo vazio é removido da tabela.
- Dentro de um grupo valem as complexidades da lista FEFO (seção 6): inserir uma bolsa O(m) e retirar a primeira O(1), onde m é o número de bolsas do grupo.

**Testes:** `teste_hash.cpp` (C++), `TabelaHashTest`, `IndiceEstoqueTest` e `EstoqueServiceIndiceTest` (Java) cobrem inserir, buscar, remover, chave inexistente e inválida, colisões forçadas, as chaves do estoque e atualização do índice após as transações.

## 8. Buscas e ordenações da Unidade 2

**C++:** `c_structures/busca_ordenacao.cpp` / `busca_ordenacao.hpp`.
**Java:** `algoritmos/BuscaOrdenacao.java`.

As buscas sequencial e binária seguem a mesma lógica nas duas linguagens: varredura da esquerda para a direita (O(n), O(1) de espaço) e redução pela metade de um intervalo ordenado (O(log n), O(1) de espaço), respectivamente. A busca binária requer entrada previamente ordenada.

As quatro ordenações do semestre também têm implementações correspondentes em C++ e Java:

| Algoritmo | Como a implementação ordena | Tempo | Espaço auxiliar |
|---|---|---|---|
| Bubble Sort | Compara e troca vizinhos; encerra cedo se uma passagem não troca nenhum elemento. | Pior O(n²); melhor O(n) com entrada ordenada. | O(1) |
| Selection Sort | Procura o menor elemento do trecho ainda não ordenado e o troca para a próxima posição. | O(n²) em todos os casos, pois sempre percorre o trecho restante. | O(1) |
| Insertion Sort | Guarda cada chave e desloca elementos maiores para abrir sua posição. | Pior O(n²); melhor O(n) com entrada ordenada. | O(1) |
| Merge Sort | Divide recursivamente e intercala trechos ordenados usando vetores auxiliares. | O(n log n) em todos os casos. | O(n), mais O(log n) de pilha recursiva. |

Os testes Java e C++ exercitam vetores vazios, unitários, ordenados, inversos, negativos e com duplicatas, comparando o resultado com uma ordenação de referência. Os quatro métodos acima correspondem aos métodos vistos no semestre, conforme confirmado.

## 9. Roteamento pelo vizinho mais próximo

**C++:** `c_structures/roteador_vizinho.cpp` / `roteador_vizinho.hpp`.
**Java:** `algoritmos/RoteadorVizinhoMaisProximo.java`, usada por `grafo/Grafo.java` e `rede/RedeService.java`.

As duas implementações partem da origem do pedido e, em cada vértice, percorrem suas arestas de saída para escolher o vizinho ainda não visitado com menor tempo. Em empate, usam ordem lexicográfica do identificador. O processo para quando chega ao destino ou quando não há vizinho elegível; pesos negativos e destinos inexistentes são ignorados. A exploração de arestas é direcionada e custos acumulados usam 64 bits.

**Complexidade (justificada pelas iterações dos vizinhos):** cada vértice é visitado no máximo uma vez. Ao visitá-lo, o algoritmo examina uma vez sua lista de adjacência; somando os graus de saída dos vértices visitados, examina no máximo E arestas. A inicialização dos vértices e o conjunto de visitados custam O(V), então o tempo esperado é O(V+E) e o espaço auxiliar é O(V), além do grafo de entrada.

Por ser uma heurística gulosa, o resultado não garante o menor custo total e pode não alcançar o destino mesmo existindo outro caminho. Não há retrocesso. As funções públicas `vizinho_mais_proximo` / `RoteadorVizinhoMaisProximo` e o endpoint `/rotas/vizinho-mais-proximo` seguem com esses nomes para compatibilidade.

**Testes:** `teste_roteador.cpp` (C++) e `AlgoritmosU2Test`/`RedeServiceTest` (Java) cobrem origem igual ao destino, destinos ausentes, escolha local de menor tempo, empates, becos sem saída e custos acumulados acima do limite de `int`.
