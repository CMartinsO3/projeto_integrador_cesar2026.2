# 🎬 Roteiro do vídeo (~3 minutos)

## Antes de gravar
1. Abra `video/slides.html` no navegador e aperte **F11** (tela cheia). Para trocar de slide, use as setas ← →.
2. Abra um PowerShell **normal** (não como administrador) na pasta `entrega-guia-transporte`.
3. Deixe aberto no VS Code o arquivo `codigo/src/guia.js`.
4. Para gravar a tela com o microfone, use uma destas opções:
   - **Ferramenta de Captura** do Windows: `Win + Shift + R` → selecione a área → ative o microfone → Gravar
   - **Clipchamp** (já vem no Windows 11): Gravar → Tela (ou Tela e câmera)

> As falas são uma sugestão. Fale com as suas palavras, sem ler.

---

### 0:00 – 0:20 · Slide 1 (abertura)
> "Oi, professor. Sou o Ruan e esta é a minha atividade individual: uma função HTTP serverless para o HemoFlow, nosso sistema de distribuição de hemocomponentes. Ela recebe uma requisição do sistema e devolve a guia de transporte das bolsas. Usei a Cloudflare Workers."

### 0:20 – 0:45 · Slide 2 (onde a função entra)
> "No HemoFlow, o hospital pede um hemocomponente. O backend em Spring Boot aloca as bolsas por FEFO e calcula a rota com Dijkstra. Depois disso ele chama esta função, que valida tudo e emite a guia: em JSON para o sistema e em HTML para o motorista imprimir. Ela é stateless: tudo de que precisa chega no corpo da requisição."

### 0:45 – 1:10 · Slide 3 (regras)
> "Antes de emitir, a função confere seis regras: compatibilidade ABO/Rh, com a mesma matriz do backend Java; a ordem FEFO; se cada bolsa continua válida na chegada; se o tempo de rota cabe no limite de cadeia fria do componente; se a chegada fica dentro da janela do hospital; e se a quantidade bate com o pedido. Se alguma regra falha, a guia sai bloqueada e com a lista de pendências."

### 1:10 – 1:30 · Slide 4 (contrato)
> "A entrada tem três partes: requisição, bolsas e rota. A resposta traz o número da guia, o status, os dados de transporte, a temperatura exigida, as bolsas e um checklist. Os status são 201 quando a guia está apta, 422 quando está bloqueada e 400 quando a entrada é inválida."

### 1:30 – 1:45 · VS Code (código, rápido)
Mostre `codigo/src/guia.js` (a matriz ABO/Rh e a função `gerarGuia`) e depois `codigo/src/index.js`.
> "O código tem duas partes: o `guia.js`, com as regras em funções puras, fáceis de testar, e o `index.js`, que é o handler HTTP da Cloudflare."

### 1:45 – 2:45 · Terminal (demonstração)
Rode:
```powershell
powershell -ExecutionPolicy Bypass -File .\video\demo.ps1
```
O script para a cada etapa. Explique e aperte ENTER para seguir:
1. **Testes** → "Os oito testes unitários passam."
2. **Servidor** → "Aqui subo a função no runtime local da Cloudflare, com o wrangler."
3. **201** → "Com uma requisição válida, a guia sai apta. As bolsas aparecem em ordem FEFO: a O−, que vence antes, vem primeiro."
4. **422** → "Aqui mandei uma bolsa B+ para um receptor A+, e plaquetas numa rota de 110 minutos. A guia sai bloqueada e mostra exatamente o que está errado."
5. **400** → "Com uma entrada inválida, a função devolve 400 e diz qual campo está faltando."
6. **HTML** → abre no navegador: "Esta é a guia imprimível, com a rota, a temperatura, as bolsas, o checklist e os campos de assinatura."

### 2:45 – 3:00 · Slide 6 (encerramento)
Se já tiver feito o deploy, mostre a URL `*.workers.dev` respondendo antes deste slide.
> "Para publicar, uso `wrangler login` e `npm run deploy`, e a função fica disponível numa URL pública. Obrigado!"
