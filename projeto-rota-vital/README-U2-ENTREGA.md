# Complemento de entrega — Unidade 2

Este pacote contém as implementações da Unidade 2: buscas sequencial, binária e por hash; os quatro métodos de ordenação vistos no semestre (Bubble, Selection, Insertion e Merge Sort); FEFO por inserção ordenada; índice hash; compatibilidade ABO/Rh por matriz; e roteamento pelo algoritmo do vizinho mais próximo. Consulte `docs/equivalencia-c-java.md` para o quadro comparativo C/C++ → Java e `docs/algoritmos-u2.md` para a tabela consolidada de complexidade por classe, integração e comandos de teste.


## Integração U2 concluída no código

- Alocação consulta a tabela hash por tipo sanguíneo + hemocomponente.
- Para hemácias, aplica compatibilidade ABO/Rh e FEFO global entre os grupos compatíveis.
- Para os demais componentes, exige tipo idêntico enquanto não houver matrizes próprias validadas.
- `ProcessadorPedidoService` executa a cadeia integrada: recebe a requisição, consulta o hash, filtra por compatibilidade, mantém FEFO, calcula a rota para cada candidata e aloca as bolsas elegíveis.
- O processamento detalhado está disponível em `POST /api/v1/requisicoes/{id}/processar`; a resposta inclui as bolsas escolhidas e os dados de rota de cada uma. O endpoint legado `/alocar` permanece disponível.
- A rota é montada com nós e ligações persistidos e está disponível pela API `GET /api/v1/rotas/vizinho-mais-proximo?origemId={id}&destinoId={id}`.
- A retirada das bolsas do índice em memória é agendada para depois do commit do banco.

Limitações: o vizinho mais próximo é guloso, não garante o menor custo total e pode não encontrar uma rota existente; as ligações são direcionadas; a regra ABO/Rh específica está implementada para hemácias. A complexidade do roteamento é O(V+E) de tempo esperado, pois cada vértice e sua lista de vizinhos são processados no máximo uma vez. As ordenações implementadas correspondem aos quatro métodos informados pelo professor: Bubble, Selection, Insertion e Merge Sort.

O histórico da revisão dos arquivos C++ e Java e os testes de verificação estão em [Correções da revisão C++ e Java](./docs/correcoes-revisao-cpp.md).
