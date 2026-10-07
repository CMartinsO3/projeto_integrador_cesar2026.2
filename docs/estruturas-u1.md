# Estruturas de Dados da Unidade 1 — Modelagem e Como Usar

> Complementa o [Modelo de Domínio](./modelo-dominio.md) e a [Equivalência C/Java](./equivalencia-c-java.md). Mostra qual estrutura foi escolhida para cada entidade, por quê, e como outro módulo (POO/API) consome o código.

---

## 1. Entidade → estrutura escolhida

| Entidade | Estrutura | Classe Java | Versão C/C++ | Por quê |
|---|---|---|---|---|
| Estoque de **Bolsas** | Lista encadeada | `ListaEncadeada<Bolsa>` | `c_structures/lista.cpp` | O estoque cresce e encolhe o tempo todo (entrada de bolsas, descarte, baixa) e é percorrido em ordem. A lista permite inserir e remover sem reorganizar um vetor. |
| **Requisições** hospitalares | Fila (FIFO) | `FilaSimples<Requisicao>` | `c_structures/fila.cpp` | Quem pediu primeiro deve ser atendido primeiro. Enfileirar e desenfileirar são O(1). |
| **Histórico de operações** | Pilha (LIFO) | `PilhaSimples<String>` | `c_structures/pilha.cpp` | A operação mais recente é a primeira a ser consultada ou descartada. |

Fora desta entrega (Unidade 2): FEFO (fila de prioridade), compatibilidade ABO/Rh, tabela hash e grafo/Dijkstra.

---

## 2. Como as estruturas chegam ao restante do sistema

Classe de serviço: `com.hemoflow.hemoflow.estoque.EstruturasEstoqueService` (bean Spring, funciona em memória e não depende de banco).

| Operação | Método | Estrutura usada |
|---|---|---|
| Inserir bolsa no estoque | `registrarBolsa(bolsa)` | Lista (`inserirNoFim`) |
| Remover bolsa | `removerBolsa(bolsa)` | Lista (`remover`) |
| Consultar estoque | `consultarEstoque()`, `estoqueContem(bolsa)`, `quantidadeEstoque()` | Lista (`paraLista`, `contem`, `tamanho`) |
| Colocar requisição na fila | `enfileirarRequisicao(req)` | Fila (`enfileirar`) |
| Ver próxima requisição | `proximaRequisicao()` | Fila (`espiar`) |
| Atender requisição | `atenderProximaRequisicao()` | Fila (`desenfileirar`) |
| Ver última operação | `ultimaOperacao()` | Pilha (`espiar`) |
| Descartar última operação | `descartarUltimaOperacao()` | Pilha (`desempilhar`) |

Cada operação de escrita também registra uma linha no histórico (por exemplo, `BOLSA_REGISTRADA lote=L-001`).

---

## 3. Como usar / integrar

### Injetando o serviço em outra classe

```java
@Service
public class MeuServico {

    private final EstruturasEstoqueService estruturas;

    public MeuServico(EstruturasEstoqueService estruturas) {
        this.estruturas = estruturas;
    }

    public void exemplo(Bolsa bolsa, Requisicao requisicao) {
        estruturas.registrarBolsa(bolsa);                  // lista
        estruturas.enfileirarRequisicao(requisicao);       // fila

        Requisicao proxima = estruturas.atenderProximaRequisicao();
        String ultima = estruturas.ultimaOperacao();       // "REQUISICAO_ATENDIDA id=..."
    }
}
```

### Usando uma estrutura diretamente

As três classes são genéricas e não dependem do Spring nem do banco:

```java
FilaSimples<String> fila = new FilaSimples<>();
fila.enfileirar("A");
fila.enfileirar("B");
fila.desenfileirar();   // "A"
fila.espiar();          // "B"
```

### Regras de uso

- Consultar ou remover de uma estrutura vazia lança `IllegalStateException` (a lista lança `IndexOutOfBoundsException` para índice inválido).
- `descartarUltimaOperacao()` só remove o registro do histórico; não desfaz a alteração no estoque nem na fila.
- Nenhuma classe usa `java.util.LinkedList`, `Queue`, `Stack` ou `Deque` no núcleo da estrutura.

### Plano de integração na Unidade 2

O fluxo persistido (JPA) continua em `EstoqueService`. Na Unidade 2, `EstoqueService` pode chamar `EstruturasEstoqueService` ao cadastrar bolsas e ao receber requisições, sem mudar a assinatura dos métodos que já existem.

---

## 4. Testes

- `EstruturasTest`: comportamento de cada estrutura isoladamente.
- `EstruturasEstoqueServiceTest`: estoque, requisições e histórico com `Bolsa` e `Requisicao` reais do domínio.
- `c_structures/main.cpp`: mesmos cenários nas versões C/C++ (`compile.sh`).
