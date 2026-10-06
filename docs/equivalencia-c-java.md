# Equivalência entre as versões C/C++ e Java — Estruturas de Dados (Unidade 1)

> Documento exigido pela entrega do Projeto Integrador — Unidade 1 (AED). Explica,
> estrutura por estrutura, por que a reimplementação em Java (pacote
> `com.hemoflow.hemoflow.estruturas`) corresponde à lógica da versão em C
> (pasta `c_structures/`), apontando as equivalências reais entre os dois códigos.

---

## 1. Lista Encadeada (Estoque)

**C:** `c_structures/lista.cpp` / `lista.hpp`
**Java:** `src/main/java/.../estruturas/ListaEncadeada.java`

Em C, a lista é representada por dois `struct`s: `NoLista` (contendo o dado
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

**C:** `c_structures/fila.cpp` / `fila.hpp`
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

**C:** `c_structures/pilha.cpp` / `pilha.hpp`
**Java:** `src/main/java/.../estruturas/PilhaSimples.java`

Em C, a pilha usa um único ponteiro de topo (`struct NoPilha* topo` dentro do
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
topo.proximo;`) que `pilha_desempilhar` faz em C, apenas sem a chamada
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
como cada linguagem gerencia memória (manual em C, automática em Java) e em
como cada uma sinaliza casos de borda (valor sentinela vs. exceção).
