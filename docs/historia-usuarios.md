# Histórias de Usuário — HemoFlow

> Documento com as 10 histórias de usuário do projeto, escritas no padrão **3 C's (Cartão / Conversa / Confirmação)**, com critérios de aceite em formato **BDD** (Dado / Quando / Então).

---

## 1. Cadastro de Bolsa no Estoque

**Cartão**

Como operador do hemocentro, quero registrar uma nova bolsa de sangue no estoque (tipo sanguíneo, componente, data de coleta e validade), para que ela fique disponível para alocação assim que processada.

**Conversa**
- O operador informa tipo sanguíneo (ABO), fator Rh, componente, quantidade, lote e validade.
- O sistema deve validar os dados antes de salvar (ex.: validade não pode ser anterior à data de coleta).
- O status inicial da bolsa deve ser definido automaticamente como "Disponível".

**Confirmação (BDD)**
```
Cenário: Cadastro de bolsa válida
Dado que os dados informados sejam válidos
Quando o operador cadastrar a bolsa
Então ela deve aparecer no estoque com status "Disponível" e todas as informações registradas

Cenário: Cadastro com dados inconsistentes
Dado que a validade informada seja anterior à data de coleta
Quando o operador tentar cadastrar a bolsa
Então o sistema deve rejeitar o cadastro e indicar o erro
```

---

## 2. Criação de Requisição Hospitalar

**Cartão**

Como responsável hospitalar, quero criar uma requisição informando tipo sanguíneo, componente e quantidade necessária, para que o hemocentro saiba o que preciso e possa atendê-la.

**Conversa**
- O hospital informa componente, tipo sanguíneo, quantidade necessária e janela de entrega.
- Todos os campos são obrigatórios para o envio.
- A requisição criada deve ficar vinculada ao hospital solicitante.

**Confirmação (BDD)**
```
Cenário: Envio de requisição válida
Dado que todos os campos obrigatórios estejam preenchidos
Quando o hospital enviar a requisição
Então ela deve ser registrada com status "Pendente" e vinculada ao hospital solicitante
```

---

## 3. Alocação por Compatibilidade ABO/Rh

**Cartão**

Como operador do hemocentro, quero que o sistema sugira automaticamente bolsas compatíveis com a requisição (ABO/Rh), para que eu não aloque um componente incompatível por engano.

**Conversa**
- O sistema compara o tipo sanguíneo solicitado com os hemocomponentes disponíveis, seguindo a matriz de compatibilidade ABO/Rh didática definida pelo projeto.
- Definir o comportamento quando não há nenhuma bolsa compatível em estoque (requisição marcada como "Aguardando estoque").
- Após a seleção, a quantidade em estoque deve ser atualizada e a requisição marcada como "Alocada".

**Confirmação (BDD)**
```
Cenário: Filtragem por compatibilidade
Dado que existam bolsas disponíveis
Quando o sistema avaliar a compatibilidade da requisição
Então somente bolsas compatíveis com o tipo sanguíneo solicitado devem ser consideradas

Cenário: Confirmação da alocação
Dado que exista quantidade suficiente de bolsas compatíveis
Quando o operador confirmar a alocação
Então o estoque deve ser reduzido e a requisição marcada como "Alocada"
```

---

## 4. Priorização por Validade (FEFO)

**Cartão**

Como operador do hemocentro, quero que, entre as bolsas compatíveis, o sistema priorize as mais próximas do vencimento, para reduzir o descarte por validade expirada.

**Conversa**
- Entre as bolsas compatíveis, a ordenação segue a data de validade mais próxima primeiro.
- Definir o critério de desempate quando duas bolsas têm a mesma validade (ex.: ordem de cadastro).

**Confirmação (BDD)**
```
Cenário: Priorização por validade
Dado que existam várias bolsas compatíveis
Quando ocorrer a alocação
Então as bolsas com menor prazo de validade devem ser priorizadas primeiro
```

---

## 5. Cálculo de Rota de Distribuição

**Cartão**

Como responsável pela logística, quero que o sistema calcule a rota mais rápida entre o hemocentro e o hospital solicitante, para que a entrega respeite a janela de tempo da cadeia fria.

**Conversa**
- O sistema utiliza um grafo limitado de unidades e vias, considerando distância/tempo (peso das arestas) para calcular o caminho mínimo (algoritmo de Dijkstra).
- É necessário definir o limite máximo de tempo de viagem antes de comprometer a cadeia fria.

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

## 6. Acompanhamento da Entrega pelo Motorista

**Cartão**

Como motorista responsável pelo transporte, quero visualizar a rota calculada e o tempo estimado de chegada, para seguir o trajeto definido e não comprometer a temperatura da carga.

**Conversa**
- A tela do motorista exibe rota, tempo estimado e próximo destino.
- O motorista pode atualizar o status da entrega (em trânsito, entregue).

**Confirmação (BDD)**
```
Cenário: Visualização da rota pelo motorista
Dado que uma rota tenha sido calculada para a entrega
Quando o motorista acessar a tela de acompanhamento
Então o sistema deve exibir a rota, o tempo estimado e o próximo destino

Cenário: Atualização de status da entrega
Dado que a entrega esteja em andamento
Quando o motorista atualizar o status (em trânsito ou entregue)
Então o sistema deve refletir essa mudança para os demais usuários
```

---

## 7. Monitoramento de Temperatura

**Cartão**

Como gestor do hemocentro, quero visualizar em um painel a temperatura da bolsa durante o transporte (simulada), para identificar rapidamente qualquer risco de rompimento da cadeia fria.

**Conversa**
- A temperatura é simulada por telemetria, com valores registrados periodicamente.
- Definir a faixa de temperatura aceitável por tipo de hemocomponente.

**Confirmação (BDD)**
```
Cenário: Alerta de temperatura fora da faixa
Dado que o transporte esteja em andamento
Quando a temperatura simulada ultrapassar o limite configurado para o componente
Então o sistema deve gerar um alerta visível no painel
```

---

## 8. Monitoramento de Rede

**Cartão**

Como gestor de operações, quero visualizar o status de conectividade dos dispositivos de telemetria em um painel, para saber se estou recebendo dados confiáveis durante o trajeto.

**Conversa**
- O sistema representa as unidades/dispositivos e indica seu status (Online, Offline, Instável), com dados simulados.
- Definir o tempo de timeout que caracteriza um dispositivo como "Offline".

**Confirmação (BDD)**
```
Cenário: Atualização de status de conectividade
Dado que as unidades/dispositivos estejam cadastrados
Quando houver alteração no status de conectividade de um dispositivo
Então o painel deve atualizar a situação e sinalizar o dispositivo afetado
```

---

## 9. Indicadores de Estoque e Demanda

**Cartão**

Como gestor, quero visualizar indicadores estatísticos de estoque e demanda por tipo sanguíneo, para antecipar riscos de desabastecimento antes que aconteçam.

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

## 10. Consulta de Status da Requisição

**Cartão**

Como responsável hospitalar, quero consultar o status da minha requisição (pendente, alocada, em trânsito, entregue), para saber quando o componente vai chegar sem precisar ligar para o hemocentro.

**Conversa**
- O hospital pode consultar individualmente uma requisição ou listar todas as suas requisições.
- Os status possíveis devem ser padronizados: Pendente, Alocada, Em trânsito, Entregue.

**Confirmação (BDD)**
```
Cenário: Consulta de status atualizado
Dado que uma requisição tenha sido registrada
Quando o hospital consultar seu status
Então o sistema deve exibir o status atual (Pendente, Alocada, Em trânsito ou Entregue)
```

---
