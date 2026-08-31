# Histórias de Usuário — Rota Vital

> Documento com as histórias de usuário que fundamentaram o **Protótipo Lo-Fi (Figma)** da Entrega 01, escritas no padrão **3 C's (Cartão / Conversa / Confirmação)**, com critérios de aceite em formato **BDD** (Dado / Quando / Então).

---

## 1. Gestão de Estoque

**Cartão**

Como gestor do hemocentro, quero cadastrar e consultar hemocomponentes no estoque, para controlar a quantidade disponível e identificar rapidamente a disponibilidade por tipo sanguíneo e componente.

**Conversa**
- O gestor informa tipo sanguíneo (ABO), fator Rh, componente, quantidade, lote e validade no cadastro.
- O sistema deve validar os dados antes de salvar (ex.: validade não pode ser anterior à data de coleta).
- Na consulta, o gestor pode filtrar por ABO, Rh e componente (concentrado de hemácias, plasma, plaquetas etc.).

**Confirmação (BDD)**
```
Cenário: Cadastro de hemocomponente válido
Dado que os dados informados sejam válidos
Quando o gestor cadastrar o hemocomponente
Então ele deve aparecer no estoque com todas as informações registradas

Cenário: Consulta filtrada de estoque
Dado que existam bolsas cadastradas
Quando o gestor aplicar um filtro (ABO, Rh ou componente)
Então o sistema deve apresentar somente os itens correspondentes ao filtro
```

---

## 2. Requisição Hospitalar

**Cartão**
Como hospital, quero registrar uma requisição de hemocomponentes, para solicitar bolsas ao hemocentro.

**Conversa**
- O hospital informa componente, tipo sanguíneo, quantidade necessária e janela de entrega.
- Todos os campos são obrigatórios para o envio da requisição.
- A requisição criada deve ficar vinculada ao hospital solicitante.

**Confirmação (BDD)**
```
Cenário: Envio de requisição válida
Dado que todos os campos obrigatórios estejam preenchidos
Quando o hospital enviar a requisição
Então ela deve ser registrada com status "Pendente"
```

---

## 3. Alocação de Hemocomponentes (Compatibilidade + FEFO)

**Cartão**
Como gestor do hemocentro, quero alocar bolsas compatíveis e priorizadas por validade para uma requisição, para garantir que a demanda hospitalar seja atendida com segurança e reduzir perdas por vencimento.

**Conversa**
- O sistema compara o tipo sanguíneo solicitado com os hemocomponentes disponíveis, seguindo a matriz de compatibilidade ABO/Rh didática definida pelo projeto.
- Entre as bolsas compatíveis, o sistema prioriza a alocação das mais próximas da validade (FEFO — *First Expired, First Out*).
- A quantidade em estoque deve ser atualizada após a confirmação da alocação.
- É preciso definir o que ocorre quando não há bolsa compatível disponível (requisição fica "Aguardando estoque").

**Confirmação (BDD)**
```
Cenário: Filtragem por compatibilidade
Dado que existam bolsas disponíveis
Quando o sistema avaliar a compatibilidade da requisição
Então somente bolsas compatíveis com o tipo sanguíneo solicitado devem ser consideradas

Cenário: Priorização por validade (FEFO)
Dado que existam várias bolsas compatíveis
Quando ocorrer a alocação
Então as bolsas com menor prazo de validade devem ser priorizadas

Cenário: Confirmação da alocação
Dado que exista quantidade suficiente de bolsas compatíveis
Quando o gestor confirmar a alocação
Então o estoque deve ser reduzido e a requisição marcada como "Alocada"
```

---

## 4. Cálculo de Rota de Distribuição

**Cartão**
Como responsável pela distribuição, quero calcular a melhor rota entre o hemocentro e o hospital, para realizar a entrega dentro da janela de tempo, respeitando a cadeia fria.

**Conversa**
- O sistema utiliza um grafo limitado de unidades e vias, considerando distância/tempo (peso das arestas) para calcular o caminho mínimo (algoritmo de Dijkstra).
- É necessário definir o limite máximo de tempo de viagem antes de comprometer a cadeia fria.
- Definir o comportamento do sistema caso nenhuma rota disponível respeite a janela de tempo.

**Confirmação (BDD)**
```
Cenário: Cálculo de rota válida
Dado que existam origem e destino válidos
Quando o sistema calcular a rota
Então deve apresentar o caminho selecionado e o tempo estimado de deslocamento

Cenário: Rota fora da janela da cadeia fria
Dado que o tempo estimado da rota calculada ultrapasse o limite da cadeia fria
Quando a rota for apresentada
Então o sistema deve sinalizar o risco antes da confirmação da entrega
```

---

## 5. Dashboard de Indicadores

**Cartão**
Como gestor, quero visualizar indicadores de estoque e demanda, para tomar decisões sobre distribuição e reposição.

**Conversa**
- O painel deve apresentar: quantidade disponível em estoque, demanda por tipo sanguíneo, bolsas próximas do vencimento e requisições pendentes.
- Definir a frequência/gatilho de atualização dos indicadores (a cada nova requisição/alocação registrada).

**Confirmação (BDD)**
```
Cenário: Exibição de indicadores atualizados
Dado que existam dados registrados no sistema
Quando o gestor acessar o dashboard
Então os indicadores devem ser apresentados de forma atualizada
```

---

## Rastreabilidade com o Protótipo Lo-Fi

| História | Tela no Figma |
|---|---|
| 1. Gestão de Estoque | Tela de Estoque |
| 2. Requisição Hospitalar | Tela de Nova Requisição |
| 3. Alocação (Compatibilidade + FEFO) | Tela de Alocação |
| 4. Cálculo de Rota | Tela de Rota de Distribuição |
| 5. Dashboard de Indicadores | Tela de Dashboard |

> Link do protótipo e do screencast disponíveis na seção **Entrega 01** do README principal do repositório.
