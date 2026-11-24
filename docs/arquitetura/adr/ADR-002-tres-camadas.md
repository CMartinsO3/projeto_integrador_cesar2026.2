# ADR-002 — Arquitetura em três camadas dentro de cada contexto

- **Status:** Aceito
- **Data:** 24/11/2026

## Contexto
Os controllers atuais às vezes acessam repositórios diretamente (por exemplo, `RequisicaoController` usa `RequisicaoRepository`). Isso mistura HTTP, regra e persistência, e dificulta testar as regras sem subir o Spring.

## Decisão
Cada bounded context (no monólito modular ou em um microsserviço) segue **três camadas** com dependência só para baixo:

1. **Apresentação:** controllers REST, telas Thymeleaf, DTOs, validação de entrada e tradução de erros (400, 404, 422).
2. **Negócio:** serviços de aplicação (casos de uso e transações) e modelo de domínio (agregados, value objects, serviços de domínio e eventos).
3. **Dados:** repositórios Spring Data JPA e acesso ao PostgreSQL.

Regras complementares:
- Controllers não acessam repositórios; tudo passa por um serviço de aplicação.
- A camada de negócio depende de **interfaces** de repositório, não do JPA diretamente.
- Entidades JPA não saem da camada de negócio; a apresentação trabalha só com DTOs.

## Consequências
- (+) As regras (ABO/Rh, FEFO, Dijkstra) ficam testáveis com testes unitários puros, que já existem.
- (+) A mesma estrutura serve ao monólito e a cada microsserviço futuro.
- (−) Há mais classes de mapeamento (DTO ↔ domínio).
- (−) É preciso refatorar os controllers que hoje acessam repositórios.

## Alternativas consideradas
- **Arquitetura hexagonal completa:** é melhor para isolar infraestrutura, mas exige mais cerimônia do que a equipe comporta agora. As interfaces de repositório já preparam o caminho.
- **Duas camadas (controller + repositório):** deixa a regra de negócio sem lugar próprio.
