# ADR-001 — Organizar o HemoFlow por DDD (subdomínios e bounded contexts)

- **Status:** Aceito
- **Data:** 24/11/2026

## Contexto
O HemoFlow nasceu como um monólito Spring Boot organizado por camada técnica (`api/`, `dominio/`, `estoque/`, `grafo/`, `persistencia/`). Com a entrada de captação de doadores, telemetria e indicadores, termos como *Bolsa*, *Hospital* e *Status* passaram a significar coisas diferentes em partes diferentes do código. Por exemplo, `Hospital` é ao mesmo tempo o solicitante de uma requisição e um nó do grafo de rotas. Uma mudança em uma regra afeta classes de outras áreas.

## Decisão
Modelar o sistema com **Domain-Driven Design**:
- 2 subdomínios **core**: Alocação de Hemocomponentes e Logística com Cadeia Fria;
- 5 de **suporte**: Captação, Estoque, Demanda Hospitalar, Telemetria e Indicadores;
- 4 **genéricos**: Identidade, Notificações, Mapas e Observabilidade;
- 8 **bounded contexts**, cada um com a sua linguagem ubíqua, registrada no glossário;
- integração entre contextos documentada no **mapa de contextos** (Customer-Supplier, Open Host Service, Published Language, ACL e Conformist).

## Consequências
- (+) Cada contexto evolui no próprio ritmo, e as fronteiras viram candidatas naturais a módulos ou serviços.
- (+) As regras críticas (ABO/Rh, FEFO, cadeia fria) ficam concentradas nos contextos core.
- (−) Há duplicação deliberada de alguns dados, como o `Hospital` do contexto Requisições e o `NoRede` do contexto Logística.
- (−) A equipe precisa manter o glossário atualizado.

## Alternativas consideradas
- **Manter a divisão por camada técnica:** é simples, mas espalha as regras e não indica onde cortar o sistema.
- **Dividir por entidade (um módulo por tabela):** gera serviços anêmicos e muito acoplamento por chamadas.
