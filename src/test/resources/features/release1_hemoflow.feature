# language: pt
Funcionalidade: Regras da Release 1 do HemoFlow (Compatibilidade ABO/Rh, FEFO e Caminho Mínimo)

  Cenário: História A - Bloquear alocação de bolsa com tipo sanguíneo incompatível
    Dado que o tipo sanguíneo da bolsa disponível é "AB_POS"
    E o tipo sanguíneo solicitado na requisição é "O_NEG"
    Quando verifico a compatibilidade para alocação
    Então a alocação deve ser considerada incompatível

  Cenário: História A - Permitir alocação de bolsa O negativo para receptor A positivo
    Dado que o tipo sanguíneo da bolsa disponível é "O_NEG"
    E o tipo sanguíneo solicitado na requisição é "A_POS"
    Quando verifico a compatibilidade para alocação
    Então a alocação deve ser considerada compatível

  Cenário: História B - Priorizar bolsa compatível com vencimento mais próximo (FEFO)
    Dado que existem bolsas compatíveis do tipo "O_NEG" com vencimentos em 20 dias e 3 dias
    Quando ordeno as bolsas para alocação FEFO
    Então a bolsa que vence em 3 dias deve ser selecionada primeiro

  Cenário: História C - Calcular rota de menor tempo via Dijkstra na rede
    Dado uma rede logística com aresta direta de 1 para 3 custando 180 minutos
    E caminho alternativo de 1 para 2 custando 70 minutos e de 2 para 3 custando 60 minutos
    Quando calculo o caminho mínimo de 1 até 3
    Então a rota deve existir com custo total de 130 minutos