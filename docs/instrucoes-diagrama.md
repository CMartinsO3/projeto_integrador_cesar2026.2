# Instruções para Gerar o Diagrama de Arquitetura

Este documento fornece instruções para converter o arquivo PlantUML em um diagrama visual exportável (PDF ou PNG).

## Arquivo Fonte
- **Localização:** `docs/diagrama-arquitetura.puml`
- **Formato:** PlantUML
- **Conteúdo:** Arquitetura de rede, componentes REST e topologia de grafos do HemoFlow

---

## Opção 1: Usar PlantUML Online

1. Acesse: https://www.plantuml.com/plantuml/uml/
2. Copie o conteúdo completo de `diagrama-arquitetura.puml`
3. Cole na área de texto do editor online
4. Clique em "Submit" para gerar o diagrama
5. Exporte usando as opções:
   - **PNG**: Clique com botão direito → Salvar imagem
   - **PDF**: Use a opção de exportação do navegador (Ctrl+P → Salvar como PDF)

---

## Opção 2: Usar VS Code com Extensão PlantUML

### Instalação:
1. Instale a extensão "PlantUML" no VS Code (by jebbs)
2. Instale o Graphviz (dependência necessária):
   - **Windows (Chocolatey):** `choco install graphviz`
   - **Windows (Manual):** Baixe de https://graphviz.org/download/
   - **macOS:** `brew install graphviz`
   - **Linux:** `sudo apt-get install graphviz`

### Exportação:
1. Abra o arquivo `docs/diagrama-arquitetura.puml` no VS Code
2. Pressione `Alt+D` para preview
3. Para exportar:
   - Pressione `Ctrl+Shift+P`
   - Digite "PlantUML: Export Current Diagram"
   - Escolha o formato: PNG, SVG ou PDF
   - Salve em `docs/diagrama-arquitetura.pdf`

---

## Opção 3: Usar PlantUML CLI (Linha de Comando)

### Instalação:
```bash
# Instalar Java (requerido)
# Windows: https://www.oracle.com/java/technologies/downloads/

# Baixar PlantUML JAR
curl -L -o plantuml.jar https://github.com/plantuml/plantuml/releases/latest/download/plantuml.jar
```

### Exportação:
```bash
# Gerar PNG
java -jar plantuml.jar docs/diagrama-arquitetura.puml

# Gerar SVG
java -jar plantuml.jar -tsvg docs/diagrama-arquitetura.puml

# Gerar PDF (requer Apache Batik)
java -jar plantuml.jar -tpdf docs/diagrama-arquitetura.puml
```

---

## Opção 4: Usar Draw.io com Importação PlantUML

1. Acesse https://app.diagrams.net/
2. Vá em **Arrange → Insert → Advanced → PlantUML**
3. Cole o conteúdo do arquivo `.puml`
4. Ajuste o layout se necessário
5. Exporte como PDF:
   - **File → Export as → PDF**
   - Configure as opções de exportação
   - Salve em `docs/diagrama-arquitetura.pdf`

---

## Resultado Esperado

Após a exportação, você terá:
- `docs/diagrama-arquitetura.pdf` ou `.png`
- Diagrama visual completo mostrando:
  - Camadas da arquitetura (Apresentação, API, Negócio, Persistência)
  - Controllers REST e seus endpoints
  - Serviços e algoritmos (Dijkstra, FEFO)
  - Topologia de rede com nós (Hemocentro, Hospitais) e arestas (tempos de viagem)
  - Fluxos de dados principais (Criação de Requisição, Alocação, Cálculo de Rota)

---

## Observações

- O diagrama usa notação UML/C4 e inclui elementos específicos de AWS (opcional)
- Se os ícones AWS não renderizarem, remova as linhas `!include AWSPUML/...`
- O diagrama pode ser editado diretamente no arquivo `.puml` e regerado quantas vezes necessário
